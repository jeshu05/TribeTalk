"""
SPRING_F5 ONNX Configuration Module
"""

import os
from dataclasses import dataclass

@dataclass
class SpringF5Config:
    dim: int = 1024
    depth: int = 22
    heads: int = 16
    text_dim: int = 512
    conv_layers: int = 4
    sample_rate: int = 24000
    n_mel_channels: int = 100
    nfe_step: int = 16
    cfg_strength: float = 2.0
    sway_sampling_coef: float = -1.0
    precision: str = "int8" # "fp32", "fp16", "int8"
