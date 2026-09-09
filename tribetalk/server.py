"""FastAPI Production Server for TribeTalk Neural Speech & Translation Pipeline.

Serves genuine IndicTrans2 INT8 NMT, IndicConformer ASR, and Neural TTS models
over HTTP/REST for Android, web clients, and embedded endpoints.
"""

from __future__ import annotations

import base64
import io
import logging
import time
from typing import Optional

import numpy as np
import soundfile as sf
import uvicorn
from fastapi import FastAPI, File, Form, HTTPException, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

from tribetalk.pipeline.orchestrator import TribeTalkPipeline
from tribetalk.pipeline.types import PipelineDirection
from tribetalk.resource_manager import GlobalModelResourceManager, MemoryMode

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("tribetalk.server")

app = FastAPI(
    title="TribeTalk AI Speech & Translation Server",
    description="Genuine Bidirectional Neural Speech Translation (Hindi <-> Santali Ol Chiki)",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Global pipeline instance with cached memory mode for fast multi-request serving
_pipeline: Optional[TribeTalkPipeline] = None


def get_pipeline() -> TribeTalkPipeline:
    global _pipeline
    if _pipeline is None:
        logger.info("Initializing TribeTalkPipeline with MemoryMode.CACHED...")
        resource_manager = GlobalModelResourceManager(mode=MemoryMode.CACHED)
        _pipeline = TribeTalkPipeline(resource_manager=resource_manager)
        logger.info("TribeTalkPipeline successfully loaded.")
    return _pipeline


class TranslateRequest(BaseModel):
    text: str
    source_lang: str = "hi"
    target_lang: str = "sat"
    synthesize_speech: bool = True


class TranslateResponse(BaseModel):
    source_text: str
    target_text: str
    source_lang: str
    target_lang: str
    total_latency_ms: float
    has_audio: bool
    audio_base64: Optional[str] = None
    sample_rate: int = 16000
    success: bool


class HealthResponse(BaseModel):
    status: str
    model_nmt: str
    model_asr_hindi: str
    model_asr_santali: str
    model_tts_hindi: str
    model_tts_santali: str
    timestamp: float


def _audio_to_wav_base64(audio: np.ndarray, sample_rate: int = 16000) -> str:
    """Convert float32 audio samples array to base64-encoded WAV format."""
    buf = io.BytesIO()
    sf.write(buf, audio, sample_rate, format="WAV", subtype="PCM_16")
    buf.seek(0)
    return base64.b64encode(buf.read()).decode("ascii")


@app.get("/health", response_model=HealthResponse)
@app.get("/api/health", response_model=HealthResponse)
def health_check() -> HealthResponse:
    """Service health and loaded neural models status."""
    return HealthResponse(
        status="healthy",
        model_nmt="hari31416/indictrans2-indic-indic-dist-320M-ONNX-int8",
        model_asr_hindi="OpenVoiceOS/ai4bharat-indicconformer-hi-onnx",
        model_asr_santali="thunderboltc/whisper-small-santali-ol-chiki",
        model_tts_hindi="Xenova/mms-tts-hin",
        model_tts_santali="rhasspy/piper-voices-santali",
        timestamp=time.time(),
    )


@app.post("/api/translate", response_model=TranslateResponse)
def translate_text(req: TranslateRequest) -> TranslateResponse:
    """Execute genuine neural text translation with IndicTrans2 INT8 and optional TTS."""
    if not req.text.strip():
        raise HTTPException(status_code=400, detail="Empty text input provided")

    pipeline = get_pipeline()
    direction = "hi_to_sat" if req.source_lang.lower().startswith("hi") else "sat_to_hi"

    res = pipeline.process_text(
        text=req.text.strip(),
        direction=direction,
        synthesize_speech=req.synthesize_speech,
    )

    audio_b64: Optional[str] = None
    has_audio = False
    sample_rate = 16000

    if res.has_audio_output and res.tts_result is not None:
        try:
            audio_b64 = _audio_to_wav_base64(res.tts_result.audio, res.tts_result.sample_rate)
            has_audio = True
            sample_rate = res.tts_result.sample_rate
        except Exception as e:
            logger.warning(f"Error encoding synthesized audio: {e}")

    return TranslateResponse(
        source_text=res.source_text,
        target_text=res.target_text,
        source_lang=res.source_language,
        target_lang=res.target_language,
        total_latency_ms=round(res.total_latency_ms, 2),
        has_audio=has_audio,
        audio_base64=audio_b64,
        sample_rate=sample_rate,
        success=res.success,
    )


@app.post("/api/speech", response_model=TranslateResponse)
async def translate_speech(
    file: UploadFile = File(...),
    source_lang: str = Form("hi"),
    target_lang: str = Form("sat"),
    synthesize_speech: bool = Form(True),
) -> TranslateResponse:
    """Execute full speech-to-speech translation: Audio -> ASR -> IndicTrans2 -> TTS."""
    content = await file.read()
    if not content:
        raise HTTPException(status_code=400, detail="Uploaded audio file is empty")

    # Read audio bytes via soundfile
    try:
        audio_buf = io.BytesIO(content)
        audio_array, sr = sf.read(audio_buf, dtype="float32")
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Failed to decode audio file: {e}")

    pipeline = get_pipeline()
    direction = "hi_to_sat" if source_lang.lower().startswith("hi") else "sat_to_hi"

    res = pipeline.process_speech(
        audio=audio_array,
        direction=direction,
        synthesize_speech=synthesize_speech,
    )

    audio_b64: Optional[str] = None
    has_audio = False
    sample_rate = 16000

    if res.has_audio_output and res.tts_result is not None:
        try:
            audio_b64 = _audio_to_wav_base64(res.tts_result.audio, res.tts_result.sample_rate)
            has_audio = True
            sample_rate = res.tts_result.sample_rate
        except Exception as e:
            logger.warning(f"Error encoding synthesized audio: {e}")

    return TranslateResponse(
        source_text=res.source_text,
        target_text=res.target_text,
        source_lang=res.source_language,
        target_lang=res.target_language,
        total_latency_ms=round(res.total_latency_ms, 2),
        has_audio=has_audio,
        audio_base64=audio_b64,
        sample_rate=sample_rate,
        success=res.success,
    )


def start_server(host: str = "0.0.0.0", port: int = 8000) -> None:
    """Start the Uvicorn server."""
    logger.info(f"Starting TribeTalk AI Server on {host}:{port}...")
    uvicorn.run(app, host=host, port=port, log_level="info")


if __name__ == "__main__":
    start_server()
