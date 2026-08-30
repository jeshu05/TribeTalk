"""
SPRING_F5 Unified ONNX Pipeline API (Phase 11)
Exposes simple, high-level synthesis API:
  tts = SpringF5ONNX(models_dir="spring-f5-android/models/int8")
  audio_pcm, sr = tts.synthesize(text="...", reference_audio="ref.wav")
"""

import os
import time
import numpy as np
import soundfile as sf
import torch
import librosa

from .config import SpringF5Config
from .tokenizer import SpringF5Tokenizer
from .preprocess import load_reference_audio
from .transformer import SpringF5TransformerONNX
from .decoder import SpringF5DecoderONNX

class SpringF5ONNX:
    def __init__(self, models_dir: str, vocab_path: str = None, num_threads: int = 4):
        self.models_dir = models_dir
        self.config = SpringF5Config()

        if vocab_path is None:
            vocab_path = os.path.join(models_dir, "vocab.txt")
            if not os.path.exists(vocab_path):
                vocab_path = "spring-f5-android/checkpoints/checkpoints/vocab.txt"

        self.tokenizer = SpringF5Tokenizer(vocab_path)

        trans_path = os.path.join(models_dir, "spring_f5_transformer.onnx")
        dec_path = os.path.join(models_dir, "spring_f5_decoder.onnx")

        self.transformer = SpringF5TransformerONNX(trans_path, num_threads=num_threads)
        self.decoder = SpringF5DecoderONNX(dec_path, num_threads=num_threads)

    def synthesize(self, text: str, reference_audio: str = None, reference_text: str = "Hello welcome to class", n_steps: int = 16) -> tuple[np.ndarray, int]:
        t0 = time.time()
        # 1. Encode text tokens
        text_tokens = self.tokenizer.encode(text)
        text_array = np.array([text_tokens], dtype=np.int64)

        # 2. Reference audio prompt setup
        ref_audio_wave = load_reference_audio(reference_audio, target_sr=self.config.sample_rate)
        
        # Compute Mel Spectrogram using librosa
        mel_spec = librosa.feature.melspectrogram(
            y=ref_audio_wave,
            sr=self.config.sample_rate,
            n_fft=1024,
            hop_length=256,
            n_mels=100
        )
        mel_spec = np.log(np.clip(mel_spec, a_min=1e-5, a_max=None)) # [100, mel_len]
        ref_mel_len = mel_spec.shape[1]
        
        # Estimate total mel length
        target_mel_len = ref_mel_len + int(len(text_tokens) * 4)
        cond = np.zeros((1, target_mel_len, 100), dtype=np.float32)
        cond[0, :ref_mel_len, :] = mel_spec.T

        # 3. Flow Matching Euler ODE Loop using ONNX Transformer
        x_t = np.random.randn(1, target_mel_len, 100).astype(np.float32)
        mask = np.ones((1, target_mel_len), dtype=bool)

        dt = 1.0 / n_steps
        for step in range(n_steps):
            t_val = step / float(n_steps)
            vt = self.transformer.predict_velocity(x_t, cond, text_array, t_val, mask)
            x_t = x_t + dt * vt

        # 4. Mel-Spectrogram Decode using ONNX Vocoder
        generated_mel = x_t[0].T[np.newaxis, :, :] # [1, 100, target_mel_len]
        audio_pcm = self.decoder.decode_mel(generated_mel).squeeze()

        return audio_pcm, self.config.sample_rate
