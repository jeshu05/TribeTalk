"""
Test IndicTrans2 ONNX Translation hin_Deva -> sat_Olck.
"""

import sys
import os

sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

import transformers.tokenization_utils_base
import transformers.tokenization_utils
transformers.tokenization_utils.PreTrainedTokenizerBase = transformers.tokenization_utils_base.PreTrainedTokenizerBase

sys.path.append("translation/models")

from translate import IndicTransONNX

def main():
    model_dir = "translation/models"
    print(f"[*] Initializing IndicTransONNX from {model_dir}...")
    translator = IndicTransONNX(model_dir)

    test_sentences = [
        "माँ, चलो कल एक फिल्म देखने चलते हैं।",
        "मुझे कल स्कूल नहीं जाना है।",
        "मेरा नाम बिरसा है।",
        "बच्चों आज हम गिनती सीखेंगे।"
    ]

    print("\n============================================================")
    print("TESTING HINDI -> SANTALI (hin_Deva -> sat_Olck) TRANSLATION")
    print("============================================================")

    for sentence in test_sentences:
        pred = translator.translate(sentence, src_lang="hin_Deva", tgt_lang="sat_Olck")
        print(f"Hindi   : {sentence}")
        print(f"Santali : {pred}")
        print("-" * 50)

if __name__ == "__main__":
    main()
