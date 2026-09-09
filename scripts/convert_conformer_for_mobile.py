"""Converts IndicConformer INT8 ONNX models to Mobile-compatible ONNX.

Replaces the 54 `ConvInteger` sub-graphs:
  DynamicQuantizeLinear -> ConvInteger -> Cast -> Mul -> Add
with standard `Conv` nodes:
  W_float = (W_quantized - W_zp) * W_scale
  Conv(X_float, W_float, B_float)

Fixes ONNX Runtime Mobile error:
  ORT_NOT_IMPLEMENTED: Could not find an implementation for ConvInteger(10)
"""

import numpy as np
import onnx
from onnx import helper, numpy_helper
from pathlib import Path


def convert_model(input_path: Path, output_path: Path):
    print(f"Loading {input_path}...")
    model = onnx.load(str(input_path))
    graph = model.graph

    # Map initializers by name
    initializers = {init.name: init for init in graph.initializer}
    nodes_by_output = {}
    for node in graph.node:
        for out in node.output:
            nodes_by_output[out] = node

    nodes_to_remove = set()
    new_nodes = []
    new_initializers = []

    conv_int_count = 0

    for node in list(graph.node):
        if node.op_type != "ConvInteger":
            continue

        conv_int_count += 1
        # ConvInteger inputs: [X_quantized, W_quantized, X_zp, W_zp]
        x_quant_name = node.input[0]
        w_quant_name = node.input[1]
        w_zp_name = node.input[3] if len(node.input) > 3 else None

        # 1. Find producer of X_quantized (DynamicQuantizeLinear)
        prod = nodes_by_output.get(x_quant_name)
        if not prod or prod.op_type != "DynamicQuantizeLinear":
            print(f"Skipping {node.name}, unexpected producer: {prod}")
            continue

        x_float_name = prod.input[0]
        x_scale_name = prod.output[1]
        nodes_to_remove.add(prod.name)

        # 2. Extract W_quantized tensor
        w_init = initializers.get(w_quant_name)
        w_data = numpy_helper.to_array(w_init).astype(np.float32)

        # Extract W_zp
        if w_zp_name and w_zp_name in initializers:
            w_zp_data = numpy_helper.to_array(initializers[w_zp_name]).astype(np.float32)
            w_data = w_data - w_zp_data

        # 3. Find Consumer 1: Cast
        cast_node = None
        for c in graph.node:
            if node.output[0] in c.input and c.op_type == "Cast":
                cast_node = c
                break
        if not cast_node:
            print(f"Skipping {node.name}, Cast not found")
            continue
        nodes_to_remove.add(cast_node.name)

        # 4. Find Consumer 2: Mul
        mul_node = None
        for c in graph.node:
            if cast_node.output[0] in c.input and c.op_type == "Mul":
                mul_node = c
                break
        if not mul_node:
            print(f"Skipping {node.name}, Mul not found")
            continue
        nodes_to_remove.add(mul_node.name)

        # Find W_scale in Mul inputs
        # Mul has inputs: cast_output and scale_input
        # The scale_input is either a constant or a Mul of (x_scale * w_scale)
        scale_in = [inp for inp in mul_node.input if inp != cast_node.output[0]][0]
        w_scale = None
        scale_prod = nodes_by_output.get(scale_in)
        if scale_prod and scale_prod.op_type == "Mul":
            # Mul(x_scale, w_scale)
            for s_in in scale_prod.input:
                if s_in != x_scale_name and s_in in initializers:
                    w_scale = numpy_helper.to_array(initializers[s_in]).astype(np.float32)
            nodes_to_remove.add(scale_prod.name)
        elif scale_in in initializers:
            w_scale = numpy_helper.to_array(initializers[scale_in]).astype(np.float32)

        if w_scale is not None:
            # Reshape w_scale to broadcast with w_data
            while w_scale.ndim < w_data.ndim:
                w_scale = np.expand_dims(w_scale, axis=-1)
            w_data = w_data * w_scale

        # 5. Find Consumer 3: Add (Bias add)
        add_node = None
        for c in graph.node:
            if mul_node.output[0] in c.input and c.op_type == "Add":
                add_node = c
                break
        if not add_node:
            print(f"Skipping {node.name}, Add not found")
            continue
        nodes_to_remove.add(add_node.name)

        # Extract bias
        bias_in = [inp for inp in add_node.input if inp != mul_node.output[0]][0]
        final_output = add_node.output[0]

        # 6. Create Float W initializer
        float_w_name = node.name + "_float_w"
        float_w_init = numpy_helper.from_array(w_data.astype(np.float32), name=float_w_name)
        new_initializers.append(float_w_init)

        # 7. Create standard Conv node
        conv_inputs = [x_float_name, float_w_name, bias_in]
        conv_node = helper.make_node(
            "Conv",
            inputs=conv_inputs,
            outputs=[final_output],
            name=node.name + "_standard_conv",
        )
        # Copy all attributes (strides, pads, dilations, group, kernel_shape)
        for attr in node.attribute:
            conv_node.attribute.append(attr)

        new_nodes.append(conv_node)
        nodes_to_remove.add(node.name)

    print(f"Converted {conv_int_count} ConvInteger nodes to standard Conv.")

    # Reconstruct graph nodes
    remaining_nodes = [n for n in graph.node if n.name not in nodes_to_remove]
    graph.ClearField("node")
    graph.node.extend(remaining_nodes + new_nodes)
    graph.initializer.extend(new_initializers)

    print(f"Saving to {output_path}...")
    onnx.save(model, str(output_path))
    print(f"Saved {output_path.stat().st_size / 1e6:.1f} MB.")


if __name__ == "__main__":
    convert_model(Path("staged_models/asr/hindi_conformer.onnx"), Path("staged_models/asr/hindi_conformer.onnx"))
    convert_model(Path("staged_models/asr/santali_conformer.onnx"), Path("staged_models/asr/santali_conformer.onnx"))
