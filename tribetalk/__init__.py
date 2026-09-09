"""TribeTalk: Production-grade offline-capable real-time speech translation system.

Enables bidirectional speech and text translation between Hindi and Santali (Ol Chiki).
"""

from tribetalk.pipeline import (
    PipelineDirection,
    PipelineResult,
    PipelineState,
    TribeTalkPipeline,
)
from tribetalk.resource_manager import (
    GlobalModelResourceManager,
    MemoryMode,
)

__version__ = "0.1.0"

__all__ = [
    "TribeTalkPipeline",
    "PipelineDirection",
    "PipelineResult",
    "PipelineState",
    "GlobalModelResourceManager",
    "MemoryMode",
    "__version__",
]
