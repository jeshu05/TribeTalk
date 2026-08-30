"""
SPRING_F5 Vocoder Decoder ONNX Adapter
Wraps ONNX Runtime session execution for spring_f5_decoder.onnx.
"""

import numpy as np
import onnxruntime as ort

class SpringF5DecoderONNX:
    def __init__(self, model_path: str, num_threads: int = 4):
        self.model_path = model_path
        opts = ort.SessionOptions()
        opts.intra_op_num_threads = num_threads
        opts.graph_optimization_level = ort.GraphOptimizationLevel.ORT_ENABLE_ALL
        self.session = ort.InferenceSession(model_path, opts, providers=["CPUExecutionProvider"])

    def decode_mel(self, mel: np.ndarray) -> np.ndarray:
        # mel: [batch, 100, mel_len]
        inputs = {"mel": mel.astype(np.float32)}
        outputs = self.session.run(None, inputs)
        return outputs[0]
