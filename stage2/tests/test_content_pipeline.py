"""
Stage 2 Python Automated Test Suite for Content Pack Generator & Audio Verification
"""

import sys
import os
import json
import wave
import unittest

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "scripts")))
from build_content_pack import translate_hindi_to_santali, generate_24khz_pcm_wav, validate_content_pack

class TestStage2ContentPipeline(unittest.TestCase):

    def setUp(self):
        self.root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
        self.test_output_dir = os.path.join(self.root_dir, "stage2", "tests", "temp_test_pack")

    def test_translation_pipeline(self):
        hindi_input = "गिनती 1 से 10 तक"
        santali_output = translate_hindi_to_santali(hindi_input)
        self.assertTrue(len(santali_output) > 0, "Translation output must not be empty")
        self.assertIn("ᱞᱮᱠᱷᱟ", santali_output, "Translation output should contain expected Ol Chiki characters")

    def test_24khz_wav_audio_generation(self):
        test_wav = os.path.join(self.test_output_dir, "test_audio.wav")
        generate_24khz_pcm_wav(test_wav, "1 ᱠᱷᱚᱱ 10 ᱦᱟᱹᱵᱤᱡ ᱞᱮᱠᱷᱟ")
        
        self.assertTrue(os.path.exists(test_wav), "Generated WAV file must exist")
        with wave.open(test_wav, 'rb') as wf:
            self.assertEqual(wf.getframerate(), 24000, "Sample rate must be exactly 24000 Hz")
            self.assertEqual(wf.getnchannels(), 1, "Channels must be mono (1)")
            self.assertEqual(wf.getsampwidth(), 2, "Sample width must be 16-bit PCM (2 bytes)")

    def test_android_assets_content_pack_integrity(self):
        android_assets_pack = os.path.join(self.root_dir, "app", "src", "main", "assets", "content_pack")
        self.assertTrue(os.path.exists(android_assets_pack), "Android assets content_pack folder must exist")
        
        manifest_file = os.path.join(android_assets_pack, "manifest.json")
        self.assertTrue(os.path.exists(manifest_file), "manifest.json must exist in Android assets content_pack")
        
        is_valid = validate_content_pack(android_assets_pack)
        self.assertTrue(is_valid, "Content pack validation must pass")

    def tearDown(self):
        if os.path.exists(self.test_output_dir):
            import shutil
            shutil.rmtree(self.test_output_dir)

if __name__ == '__main__':
    unittest.main()
