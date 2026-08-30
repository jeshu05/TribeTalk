"""
PyTorch / ONNX Reference Transcriber Script (Phase 2)
Transcribes any Hindi WAV audio into Devanagari Hindi text using AI4Bharat IndicConformer.
"""

import os
import sys
import time
import argparse
import torch
import numpy as np
import soundfile as sf
import librosa
import onnxruntime as ort

sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

class NeMoMelPreprocessor:
    def __init__(self, sample_rate=16000, n_fft=512, hop_length=160, win_length=400, n_mels=80, preemph=0.97):
        self.sample_rate = sample_rate
        self.n_fft = n_fft
        self.hop_length = hop_length
        self.win_length = win_length
        self.n_mels = n_mels
        self.preemph = preemph
        
        mel_fb = librosa.filters.mel(sr=sample_rate, n_fft=n_fft, n_mels=n_mels, fmin=0.0, fmax=8000.0)
        self.mel_fb = torch.from_numpy(mel_fb).float()

    def process(self, waveform: torch.Tensor) -> torch.Tensor:
        if waveform.ndim == 1:
            waveform = waveform.unsqueeze(0)

        if self.preemph > 0:
            waveform = torch.cat([waveform[:, :1], waveform[:, 1:] - self.preemph * waveform[:, :-1]], dim=1)

        window = torch.hann_window(self.win_length, periodic=False, device=waveform.device)
        spec = torch.stft(
            waveform,
            n_fft=self.n_fft,
            hop_length=self.hop_length,
            win_length=self.win_length,
            window=window,
            center=True,
            pad_mode="reflect",
            return_complex=True
        )
        power_spec = torch.abs(spec) ** 2

        mel_spec = torch.matmul(self.mel_fb.to(waveform.device), power_spec)
        log_mel = torch.log(mel_spec + 1e-5)

        mean = log_mel.mean(dim=2, keepdim=True)
        std = log_mel.std(dim=2, keepdim=True)
        norm_mel = (log_mel - mean) / (std + 1e-5)

        return norm_mel

class IndicConformerASR:
    def __init__(self, model_path: str = "asr/models/onnx/model.int8.onnx", vocab_path: str = "asr/models/onnx/vocab.txt"):
        self.preprocessor = NeMoMelPreprocessor()
        opts = ort.SessionOptions()
        opts.intra_op_num_threads = 4
        opts.graph_optimization_level = ort.GraphOptimizationLevel.ORT_ENABLE_ALL
        self.session = ort.InferenceSession(model_path, opts, providers=["CPUExecutionProvider"])
        
        vocab_lines = open(vocab_path, encoding="utf-8").readlines()
        self.vocab = [line.strip().split()[0] if line.strip() else "" for line in vocab_lines]
        self.blank_id = len(self.vocab) - 1 # <blk> token is index 256

    def transcribe(self, audio_path: str) -> tuple[str, float, float]:
        t0 = time.time()
        y, sr = sf.read(audio_path)
        if y.ndim > 1:
            y = y.mean(axis=1)
        if sr != 16000:
            y = librosa.resample(y, orig_sr=sr, target_sr=16000)

        duration = len(y) / 16000.0

        features = self.preprocessor.process(torch.from_numpy(y).float()).numpy()
        length = np.array([features.shape[2]], dtype=np.int64)

        t_infer = time.time()
        outputs = self.session.run(None, {"audio_signal": features, "length": length})
        logprobs = outputs[0]
        latency_ms = (time.time() - t_infer) * 1000.0

        tokens = np.argmax(logprobs[0], axis=-1)
        collapsed = []
        prev = None
        for t in tokens:
            if t != prev:
                collapsed.append(t)
                prev = t

        subwords = []
        for t in collapsed:
            if t > 0 and t < self.blank_id:
                subwords.append(self.vocab[t])

        transcription = "".join(subwords).replace("▁", " ").strip()
        rtf = (latency_ms / 1000.0) / duration

        return transcription, latency_ms, rtf

def parse_args():
    parser = argparse.ArgumentParser(description="IndicConformer Hindi ASR Transcriber")
    parser.add_argument("--audio", type=str, default="asr/tests/audio/001.wav", help="Input WAV file")
    parser.add_argument("--output", type=str, default="asr/outputs/test01.txt", help="Output text file")
    parser.add_argument("--model", type=str, default="asr/models/onnx/model.int8.onnx", help="ONNX model path")
    return parser.parse_args()

def main():
    args = parse_args()
    if not os.path.exists(args.audio):
        print(f"[ERROR] Audio file not found: {args.audio}")
        return

    asr = IndicConformerASR(model_path=args.model)
    text, latency_ms, rtf = asr.transcribe(args.audio)

    os.makedirs(os.path.dirname(args.output), exist_ok=True)
    with open(args.output, "w", encoding="utf-8") as f:
        f.write(text + "\n")

    print(f"Audio: {args.audio}")
    print(f"Transcription: {text}")
    print(f"Inference time: {latency_ms:.2f} ms")
    print(f"RTF: {rtf:.3f}")
    print(f"[OK] Transcription saved to {args.output}")

if __name__ == "__main__":
    main()
