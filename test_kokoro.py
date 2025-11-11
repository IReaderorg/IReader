#!/usr/bin/env python3
"""
Simple Kokoro TTS Test Script

Usage:
    python test_kokoro.py "Hello, this is a test"
    python test_kokoro.py "Hello, this is a test" af_sarah
"""

import sys
import subprocess
import os

def test_kokoro(text="Hello, this is a test.", voice="af_bella", speed=1.0):
    """Test Kokoro TTS synthesis"""
    
    print("=" * 50)
    print("Kokoro TTS Test")
    print("=" * 50)
    print(f"Text: {text}")
    print(f"Voice: {voice}")
    print(f"Speed: {speed}")
    print()
    
    # Check Python version
    print(f"Python version: {sys.version}")
    print()
    
    # Check if we're in the kokoro directory
    kokoro_dir = r"C:\Users\PC\AppData\Roaming\IReader\cache\kokoro\kokoro-tts"
    if not os.path.exists(kokoro_dir):
        print(f"ERROR: Kokoro directory not found: {kokoro_dir}")
        return False
    
    print(f"Kokoro directory: {kokoro_dir}")
    print()
    
    # Check dependencies
    print("Checking dependencies...")
    try:
        import loguru
        print("✓ loguru installed")
    except ImportError:
        print("✗ loguru NOT installed")
        print("  Install with: pip install loguru")
        return False
    
    try:
        import torch
        print(f"✓ torch installed (version {torch.__version__})")
    except ImportError:
        print("✗ torch NOT installed")
        print("  Install with: pip install torch")
        return False
    
    try:
        import transformers
        print(f"✓ transformers installed")
    except ImportError:
        print("✗ transformers NOT installed")
        print("  Install with: pip install transformers")
        return False
    
    print()
    
    # Output file
    output_file = "kokoro_test_output.wav"
    
    # Build command
    command = [
        sys.executable,
        "-m", "kokoro",
        "--text", text,
        "--voice", voice,
        "--speed", str(speed),
        "--output-file", output_file
    ]
    
    print("Running command:")
    print(" ".join(command))
    print()
    
    # Run synthesis
    try:
        result = subprocess.run(
            command,
            cwd=kokoro_dir,
            capture_output=True,
            text=True,
            timeout=60
        )
        
        if result.returncode == 0:
            print("✓ Synthesis successful!")
            print(f"✓ Output saved to: {output_file}")
            
            # Check if file exists
            if os.path.exists(output_file):
                file_size = os.path.getsize(output_file)
                print(f"✓ File size: {file_size:,} bytes")
                
                # Try to play it
                print()
                print("To play the audio, run:")
                print(f"  start {output_file}")
            else:
                print("✗ Output file not created")
                return False
            
            return True
        else:
            print(f"✗ Synthesis failed with exit code {result.returncode}")
            print()
            print("STDOUT:")
            print(result.stdout)
            print()
            print("STDERR:")
            print(result.stderr)
            return False
            
    except subprocess.TimeoutExpired:
        print("✗ Synthesis timeout (60 seconds)")
        return False
    except Exception as e:
        print(f"✗ Error: {e}")
        return False

if __name__ == "__main__":
    text = sys.argv[1] if len(sys.argv) > 1 else "Hello, this is a test."
    voice = sys.argv[2] if len(sys.argv) > 2 else "af_bella"
    
    success = test_kokoro(text, voice)
    
    print()
    print("=" * 50)
    if success:
        print("✓ TEST PASSED")
    else:
        print("✗ TEST FAILED")
    print("=" * 50)
    
    sys.exit(0 if success else 1)
