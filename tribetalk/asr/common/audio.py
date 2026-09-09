"""Shared audio loading, preprocessing, downmixing, and resampling pipeline."""

from pathlib import Path
from typing import Union, Optional, Tuple
import os
import tempfile
import numpy as np
import soundfile as sf
import torch
import torchaudio.functional as F_audio

from tribetalk.asr.common.types import AudioData

TARGET_SAMPLE_RATE: int = 16000


class AudioLoadingError(Exception):
    """Raised when an audio file cannot be loaded, decoded, or is corrupted."""
    pass


def load_and_preprocess_audio(
    audio_path: Union[str, Path],
    target_sample_rate: int = TARGET_SAMPLE_RATE,
) -> AudioData:
    """Safely load, validate, convert to mono, and resample an audio file to 16 kHz.

    Pipeline:
    1. Validates existence and non-zero size.
    2. Reads audio samples via soundfile (falling back to torchaudio if needed).
    3. Mixes multichannel audio down to mono by channel averaging.
    4. Normalizes amplitude to float32 range [-1.0, 1.0].
    5. Resamples to target_sample_rate (default: 16,000 Hz) using bandlimited sinc interpolation.
    6. Measures exact duration.

    Args:
        audio_path: Path to the audio file.
        target_sample_rate: Target sampling rate in Hz (default: 16000).

    Returns:
        AudioData dataclass containing 1D float32 waveform, sample rate, and duration.

    Raises:
        FileNotFoundError: If the file does not exist.
        AudioLoadingError: If the file is empty, corrupted, or unsupported.
    """
    path = Path(audio_path).resolve()

    if not path.exists():
        raise FileNotFoundError(f"Audio file does not exist: {path}")

    if not path.is_file():
        raise AudioLoadingError(f"Audio path is not a file: {path}")

    if path.stat().st_size == 0:
        raise AudioLoadingError(f"Audio file is empty (0 bytes): {path}")

    # Step 1: Read audio
    samples, original_sr, original_channels = _read_audio_safely(path)

    if len(samples) == 0:
        raise AudioLoadingError(f"Audio file contains 0 audio frames: {path}")

    # Step 2: Convert to mono if multichannel
    if original_channels > 1:
        # samples shape is (N, C), average across channels
        samples = np.mean(samples, axis=-1, dtype=np.float32)
    elif samples.ndim > 1:
        samples = samples.flatten()

    samples = samples.astype(np.float32)

    # Step 3: Normalize if needed (prevent extreme clipping/dc offset)
    max_val = np.max(np.abs(samples)) if len(samples) > 0 else 0.0
    if max_val > 1.0:
        samples = samples / max_val

    # Step 4: Resample if sample rate doesn't match target
    if original_sr != target_sample_rate:
        samples = _resample_audio(samples, original_sr, target_sample_rate)

    # Step 5: Compute duration
    duration_ms = (len(samples) / target_sample_rate) * 1000.0

    return AudioData(
        samples=samples,
        sample_rate=target_sample_rate,
        duration_ms=duration_ms,
        original_channels=original_channels,
        original_sample_rate=original_sr,
    )


def _read_audio_safely(path: Path) -> Tuple[np.ndarray, int, int]:
    """Read audio data with soundfile, falling back to torchaudio."""
    try:
        data, sr = sf.read(str(path), dtype="float32", always_2d=True)
        channels = data.shape[1]
        return data, sr, channels
    except Exception as sf_err:
        # Fallback to torchaudio
        try:
            import torchaudio
            tensor, sr = torchaudio.load(str(path))
            # torchaudio returns shape (channels, frames)
            channels = tensor.shape[0]
            data = tensor.transpose(0, 1).cpu().numpy().astype(np.float32)
            return data, sr, channels
        except Exception as ta_err:
            raise AudioLoadingError(
                f"Failed to read or decode audio file '{path}'. "
                f"Soundfile error: {sf_err}; Torchaudio error: {ta_err}"
            ) from sf_err


def _resample_audio(samples: np.ndarray, orig_sr: int, target_sr: int) -> np.ndarray:
    """Resample 1D float32 audio to target sample rate using bandlimited sinc interpolation."""
    if len(samples) == 0:
        return samples

    tensor = torch.from_numpy(samples).unsqueeze(0)  # Shape (1, N)
    resampled_tensor = F_audio.resample(tensor, orig_sr, target_sr)
    return resampled_tensor.squeeze(0).cpu().numpy().astype(np.float32)


def save_temp_wav(audio_data: AudioData, output_path: Optional[Union[str, Path]] = None) -> Path:
    """Save AudioData to a 16 kHz mono WAV file on disk.

    Useful for engines (such as NeMo) whose standard inference APIs expect audio file paths.

    Args:
        audio_data: AudioData object to persist.
        output_path: Destination path. If None, a temporary file is created.

    Returns:
        Path to the written WAV file.
    """
    if output_path is None:
        fd, temp_filename = tempfile.mkstemp(prefix="tribetalk_asr_", suffix=".wav")
        os.close(fd)
        target = Path(temp_filename)
    else:
        target = Path(output_path).resolve()
        target.parent.mkdir(parents=True, exist_ok=True)

    sf.write(str(target), audio_data.samples, audio_data.sample_rate, subtype="PCM_16")
    return target
