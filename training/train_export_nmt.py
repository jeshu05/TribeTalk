"""
TribeTalk: Neural Machine Translation (NMT) Fine-Tuning & ONNX INT8 Export Pipeline
Language Pair: Hindi (hin_Deva) <-> Santali in Ol Chiki (sat_Olck)
Base Architecture: Meta NLLB-200 (distilled 600M)
Target Runtime: Android 8 GB RAM (ONNX Runtime Mobile)

Run this script on Google Colab (T4/A100 GPU) or a Linux/Windows workstation with an NVIDIA GPU.
"""

import os
import sys
import torch
from datasets import load_dataset
from transformers import (
    AutoModelForSeq2SeqLM,
    AutoTokenizer,
    Seq2SeqTrainer,
    Seq2SeqTrainingArguments,
    DataCollatorForSeq2Seq
)
import onnx
from onnxruntime.quantization import quantize_dynamic, QuantType

# 1. Configuration & Model Setup
MODEL_NAME = "facebook/nllb-200-distilled-600M"
SRC_LANG = "hin_Deva"   # Hindi in Devanagari script
TGT_LANG = "sat_Olck"   # Santali in Ol Chiki script
OUTPUT_DIR = "./trained_santali_nmt"
ONNX_EXPORT_PATH = "./santali_nmt_fp32.onnx"
QUANTIZED_ONNX_PATH = "./santali_nmt_micro_int8.onnx"

print(f"[*] Initializing tokenizer and base model: {MODEL_NAME}")
tokenizer = AutoTokenizer.from_pretrained(
    MODEL_NAME,
    src_lang=SRC_LANG,
    tgt_lang=TGT_LANG
)
model = AutoModelForSeq2SeqLM.from_pretrained(MODEL_NAME)

# Enable gradient checkpointing to reduce VRAM consumption during training
if hasattr(model, "gradient_checkpointing_enable"):
    model.gradient_checkpointing_enable()

# 2. Dataset Loading (BPCC or Curated Parallel Data)
print("[*] Loading Hindi-Santali parallel dataset...")
try:
    # Attempt to load AI4Bharat BPCC Hindi-Santali subset
    raw_dataset = load_dataset("ai4bharat/BPCC", "hin_Deva-sat_Olck", split="train[:50000]")
except Exception as e:
    print(f"[!] Warning: Could not load BPCC directly ({e}). Using sample synthetic curriculum pairs for demonstration.")
    from datasets import Dataset
    sample_data = {
        "translation": [
            {"hin_Deva": "किताब खोलो", "sat_Olck": "ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ"},
            {"hin_Deva": "चलो पढ़ते हैं", "sat_Olck": "ᱫᱮᱞᱟ ᱵᱚᱱ ᱯᱟᱲᱦᱟᱣ-ᱟ"},
            {"hin_Deva": "लिखना शुरू करो", "sat_Olck": "ᱚᱞ ᱮᱦᱚᱵ ᱢᱮ"},
            {"hin_Deva": "शांत रहो", "sat_Olck": "ᱛᱷᱤᱨ ᱛᱟᱦᱮᱸᱱ ᱯᱮ"},
            {"hin_Deva": "मुझे समझ नहीं आया", "sat_Olck": "ᱵᱟᱹᱧ ᱵᱩᱡᱷᱟᱹᱣ ᱫᱟᱲᱮᱭᱟᱫᱟ"},
            {"hin_Deva": "पानी पीना है", "sat_Olck": "ᱫᱟᱜ ᱧᱩ ᱥᱟᱱᱟᱹᱧ ᱠᱟᱱᱟ"},
            {"hin_Deva": "एक दो तीन चार", "sat_Olck": "ᱢᱤᱫ ᱵᱟᱨ ᱯᱮ ᱯᱩᱱ"},
            {"hin_Deva": "स्कूल जाओ", "sat_Olck": "ᱤᱛᱩᱱ ᱚᱲᱟᱜ ᱪᱟᱞᱟᱜ ᱢᱮ"},
            {"hin_Deva": "कल बारिश होगी", "sat_Olck": "ᱜᱟᱯᱟ ᱫᱟᱜ ᱡᱟᱹᱲᱤ ᱦᱩᱭᱩᱜ-ᱟ"},
            {"hin_Deva": "आपका नाम क्या है?", "sat_Olck": "ᱟᱢᱟᱜ ᱧᱩᱛᱩᱢ ᱫᱚ ᱪᱮᱫ?"}
        ]
    }
    raw_dataset = Dataset.from_dict(sample_data)

def preprocess_function(examples):
    inputs = [ex[SRC_LANG] for ex in examples["translation"]]
    targets = [ex[TGT_LANG] for ex in examples["translation"]]
    
    model_inputs = tokenizer(inputs, max_length=128, truncation=True, padding="max_length")
    labels = tokenizer(text_target=targets, max_length=128, truncation=True, padding="max_length")
    
    # Replace pad token id with -100 so it is ignored by the loss function
    labels["input_ids"] = [
        [(label if label != tokenizer.pad_token_id else -100) for label in sequence]
        for sequence in labels["input_ids"]
    ]
    model_inputs["labels"] = labels["input_ids"]
    return model_inputs

print("[*] Tokenizing parallel sentences...")
tokenized_dataset = raw_dataset.map(preprocess_function, batched=True, remove_columns=raw_dataset.column_names)

# 3. Fine-Tuning Execution
training_args = Seq2SeqTrainingArguments(
    output_dir=OUTPUT_DIR,
    evaluation_strategy="no",
    learning_rate=5e-5,
    per_device_train_batch_size=8,
    weight_decay=0.01,
    save_total_limit=1,
    num_train_epochs=3,
    predict_with_generate=True,
    fp16=torch.cuda.is_available(),
    logging_steps=50,
    save_strategy="epoch"
)

trainer = Seq2SeqTrainer(
    model=model,
    args=training_args,
    train_dataset=tokenized_dataset,
    tokenizer=tokenizer,
    data_collator=DataCollatorForSeq2Seq(tokenizer, model=model)
)

print("[*] Commencing model fine-tuning on GPU...")
trainer.train()

print(f"[*] Saving fine-tuned checkpoint to: {OUTPUT_DIR}")
model.save_pretrained(OUTPUT_DIR)
tokenizer.save_pretrained(OUTPUT_DIR)

# 4. ONNX Export with Dynamic Axes
print("[*] Exporting model to ONNX format...")
model.eval()
model.cpu()

dummy_input_ids = torch.tensor([[tokenizer.bos_token_id, 100, 200, tokenizer.eos_token_id]], dtype=torch.long)
dummy_attention_mask = torch.tensor([[1, 1, 1, 1]], dtype=torch.long)
dummy_decoder_input_ids = torch.tensor([[tokenizer.bos_token_id, 150]], dtype=torch.long)

torch.onnx.export(
    model,
    (dummy_input_ids, dummy_attention_mask, dummy_decoder_input_ids),
    ONNX_EXPORT_PATH,
    input_names=["input_ids", "attention_mask", "decoder_input_ids"],
    output_names=["logits"],
    dynamic_axes={
        "input_ids": {0: "batch_size", 1: "sequence_length"},
        "attention_mask": {0: "batch_size", 1: "sequence_length"},
        "decoder_input_ids": {0: "batch_size", 1: "decoder_sequence_length"},
        "logits": {0: "batch_size", 1: "decoder_sequence_length"}
    },
    opset_version=14
)
print(f"[✓] Successfully exported FP32 ONNX model to: {ONNX_EXPORT_PATH}")

# 5. Dynamic INT8 Quantization (Optimized for Mobile ARM NEON Execution)
print("[*] Applying Dynamic INT8 Quantization...")
quantize_dynamic(
    model_input=ONNX_EXPORT_PATH,
    model_output=QUANTIZED_ONNX_PATH,
    weight_type=QuantType.QInt8,
    per_channel=True,
    reduce_range=True
)
print(f"[✓] Quantized model saved: {QUANTIZED_ONNX_PATH}")

size_mb = os.path.getsize(QUANTIZED_ONNX_PATH) / (1024 * 1024)
print(f"[✓] Final model size: {size_mb:.2f} MB")
print("\n--> Copy 'santali_nmt_micro_int8.onnx' to your Android project:")
print("    Location: TribeTalk/app/src/main/assets/models/santali_nmt_micro_int8.onnx")
