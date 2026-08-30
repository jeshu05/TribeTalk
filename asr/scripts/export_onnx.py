"""
IndicConformer CTC ONNX Exporter Script (Phase 7)
Exports / validates FP32 ONNX model graph for IndicConformer Hindi CTC.
"""

import os
import sys
import shutil
import onnx

sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

def main():
    src_onnx = "asr/models/onnx/model.onnx"
    src_data = "asr/models/onnx/model.onnx_data"
    out_dir = "asr/models/onnx"
    out_fp32 = os.path.join(out_dir, "indicconformer_hi_ctc_fp32.onnx")

    print("============================================================")
    print("INDICCONFORMER CTC ONNX EXPORTER & VALIDATOR (Phase 7)")
    print("============================================================")

    if not os.path.exists(src_onnx):
        print(f"[ERROR] Source ONNX file not found: {src_onnx}")
        return

    print(f"[*] Validating source ONNX graph: {src_onnx}...")
    model_proto = onnx.load(src_onnx)
    
    print("Graph Inputs:")
    for inp in model_proto.graph.input:
        print(f"  {inp.name}: {[d.dim_value or d.dim_param for d in inp.type.tensor_type.shape.dim]}")

    print("Graph Outputs:")
    for out in model_proto.graph.output:
        print(f"  {out.name}: {[d.dim_value or d.dim_param for d in out.type.tensor_type.shape.dim]}")

    # Copy to standalone filename
    shutil.copy(src_onnx, out_fp32)
    if os.path.exists(src_data):
        out_data = os.path.join(out_dir, "indicconformer_hi_ctc_fp32.onnx_data")
        shutil.copy(src_data, out_data)

    fp32_mb = os.path.getsize(out_fp32) / 1024 / 1024
    if os.path.exists(src_data):
        fp32_mb += os.path.getsize(src_data) / 1024 / 1024

    print(f"\n[OK] Exported standalone FP32 model: {out_fp32} ({fp32_mb:.2f} MB)")
    print("=" * 60)

if __name__ == "__main__":
    main()
