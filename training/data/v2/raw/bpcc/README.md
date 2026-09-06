# AI4Bharat BPCC Raw Ingestion Status

## Authentication Barrier (Gated Repository)
- **Repository:** `https://huggingface.co/datasets/ai4bharat/BPCC`
- **Files Required:**
  - `bpcc-seed-latest/sat_Olck.tsv` (31.96 MB)
  - `bpcc-seed-latest/hin_Deva.tsv` (34.37 MB)
- **Technical Status:** Acquisition halted due to `HTTP Error 401: Unauthorized`.
- **Root Cause:** `ai4bharat/BPCC` is a gated dataset on Hugging Face requiring users to accept the dataset license terms and authenticate.

## Environment & Command Requirements to Acquire
To download and ingest BPCC data in Phase 2B/2C:
1. Accept terms on the Hugging Face dataset page: https://huggingface.co/datasets/ai4bharat/BPCC
2. Run in terminal:
   ```bash
   huggingface-cli login
   ```
   Or set the environment variable:
   ```bash
   # Windows PowerShell:
   $env:HF_TOKEN = "your_huggingface_token"
   # Linux/macOS:
   export HF_TOKEN="your_huggingface_token"
   ```
3. Run the automated acquisition and pivot script:
   ```bash
   python training/scripts/extract_bpcc.py
   ```

*Security Compliance: Per Phase 2B instructions, no tokens were requested, printed, stored, or exposed.*
