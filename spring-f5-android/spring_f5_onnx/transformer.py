"""
SPRING_F5 Transformer ONNX Adapter
Wraps ONNX Runtime session execution for spring_f5_transformer.onnx.
"""

import numpy as np
import onnxruntime as ort

class SpringF5TransformerONNX:
    def __init__(self, model_path: str, num_threads: int = 4):
        self.model_path = model_path
        opts = ort.SessionOptions()
        opts.intra_op_num_threads = num_threads
        opts.graph_optimization_level = ort.GraphOptimizationLevel.ORT_ENABLE_ALL
        self.session = ort.InferenceSession(model_path, opts, providers=["CPUExecutionProvider"])

    def predict_velocity(self, x: np.ndarray, cond: np.ndarray, text: np.ndarray, time_step: float, mask: np.ndarray) -> np.ndarray:
        time_tensor = np.array([time_step], dtype=np.float32)
        inputs = {
            "x": x.astype(np.float32),
            "cond": cond.astype(np.float32),
            "text": text.astype(np.int64),
            "time": time_tensor,
            "mask": mask.astype(bool)
        }
        outputs = self.session.run(None, inputs)
        return outputs[0]
