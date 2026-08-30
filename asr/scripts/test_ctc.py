"""
Test NeMo AudioPreprocessor for IndicConformer CTC.
"""

import sys
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
        
        # Create Mel Filterbank using librosa
        mel_fb = librosa.filters.mel(sr=sample_rate, n_fft=n_fft, n_mels=n_mels, fmin=0.0, fmax=8000.0)
        self.mel_fb = torch.from_numpy(mel_fb).float()

    def process(self, waveform: torch.Tensor) -> torch.Tensor:
        if waveform.ndim == 1:
            waveform = waveform.unsqueeze(0)

        # 1. Pre-emphasis
        if self.preemph > 0:
            waveform = torch.cat([waveform[:, :1], waveform[:, 1:] - self.preemph * waveform[:, :-1]], dim=1)

        # 2. STFT with periodic=False Hann Window
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
        power_spec = torch.abs(spec) ** 2  # [1, 257, T]

        # 3. Apply Mel Filterbank
        mel_spec = torch.matmul(self.mel_fb.to(waveform.device), power_spec) # [1, 80, T]

        # 4. Log with 1e-5 guard
        log_mel = torch.log(mel_spec + 1e-5) # [1, 80, T]

        # 5. Per-feature normalization
        mean = log_mel.mean(dim=2, keepdim=True)
        std = log_mel.std(dim=2, keepdim=True)
        norm_mel = (log_mel - mean) / (std + 1e-5)

        return norm_mel

def ctc_decode(logprobs, vocab):
    tokens = np.argmax(logprobs[0], axis=-1)
    collapsed = []
    prev = None
    for t in tokens:
        if t != prev:
            collapsed.append(t)
            prev = t

    decoded_subwords = []
    for t in collapsed:
        if t > 0 and t < len(vocab):
            tok = vocab[t]
            decoded_subwords.append(tok)

    text = "".join(decoded_subwords).replace("▁", " ").strip()
    return text

def main():
    audio_path = "asr/tests/audio/001.wav"
    y, sr = sf.read(audio_path)
    if y.ndim > 1:
        y = y.mean(axis=1)
    if sr != 16000:
        y = librosa.resample(y, orig_sr=sr, target_sr=16000)

    preprocessor = NeMoMelPreprocessor()
    wave_tensor = torch.from_numpy(y).float()
    features = preprocessor.process(wave_tensor).numpy() # [1, 80, T]
    length = np.array([features.shape[2]], dtype=np.int64)

    vocab_lines = open("asr/models/onnx/vocab.txt", encoding="utf-8").readlines()
    vocab = [line.strip().split()[0] if line.strip() else "" for line in vocab_lines]

    print(f"[*] Audio: {audio_path} (Duration: {len(y)/16000:.2f}s)")
    print(f"[*] NeMo Mel feature shape: {features.shape}")

    sess = ort.InferenceSession("asr/models/onnx/model.int8.onnx", providers=["CPUExecutionProvider"])
    outputs = sess.run(None, {"audio_signal": features, "length": length})
    logprobs = outputs[0]

    transcription = ctc_decode(logprobs, vocab)
    print(f"\n[OK] Decoded Hindi Transcription: \"{transcription}\"")

if __name__ == "__main__":
    main()
