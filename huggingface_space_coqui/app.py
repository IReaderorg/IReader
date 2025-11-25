import gradio as gr
from TTS.api import TTS
import numpy as np
import logging

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Initialize Coqui TTS with high-quality model
logger.info("Loading Coqui TTS model...")
try:
    # Using fast_pitch for better performance
    # Alternative: "tts_models/en/ljspeech/tacotron2-DDC" for higher quality
    tts = TTS("tts_models/en/ljspeech/fast_pitch")
    logger.info("✅ Coqui TTS model loaded successfully!")
except Exception as e:
    logger.error(f"❌ Failed to load TTS model: {e}")
    raise

def text_to_speech(text, speed=1.0):
    """
    Convert text to speech using Coqui TTS
    
    Args:
        text: Text to synthesize (max 5000 characters)
        speed: Speech speed (0.5 = slow, 1.0 = normal, 2.0 = fast)
    
    Returns:
        Tuple of (sample_rate, audio_array)
    """
    try:
        # Validate input
        if not text or len(text.strip()) == 0:
            logger.warning("Empty text received")
            return None
        
        # Limit text length to prevent abuse
        if len(text) > 5000:
            logger.warning(f"Text too long ({len(text)} chars), truncating to 5000")
            text = text[:5000]
        
        # Validate speed
        speed = max(0.5, min(2.0, speed))
        
        logger.info(f"Synthesizing {len(text)} characters at {speed}x speed")
        
        # Generate audio
        wav = tts.tts(text=text, speed=speed)
        
        # Convert to numpy array
        wav_array = np.array(wav)
        
        logger.info(f"✅ Generated {len(wav_array)} audio samples")
        
        # Return sample rate and audio
        return (22050, wav_array)
        
    except Exception as e:
        logger.error(f"❌ Error during synthesis: {e}")
        raise gr.Error(f"Failed to generate speech: {str(e)}")

# Create Gradio interface
demo = gr.Interface(
    fn=text_to_speech,
    inputs=[
        gr.Textbox(
            label="Text to Synthesize",
            placeholder="Enter your text here (max 5000 characters)...",
            lines=5,
            max_lines=10
        ),
        gr.Slider(
            minimum=0.5,
            maximum=2.0,
            value=1.0,
            step=0.1,
            label="Speed (0.5 = slow, 1.0 = normal, 2.0 = fast)"
        )
    ],
    outputs=gr.Audio(
        label="Generated Speech",
        type="numpy"
    ),
    title="🎙️ Coqui TTS - High Quality Text-to-Speech",
    description="""
    High-quality neural text-to-speech powered by Coqui TTS.
    
    **Features:**
    - 🎯 Natural-sounding voice
    - ⚡ Fast synthesis
    - 🎚️ Adjustable speed (0.5x - 2.0x)
    - 📝 Up to 5000 characters per request
    - 🆓 Free and open source
    
    **Usage:**
    1. Enter your text in the box above
    2. Adjust the speed if needed
    3. Click Submit
    4. Download or play the generated audio
    """,
    examples=[
        ["Hello! Welcome to Coqui TTS. This is a high-quality text-to-speech service.", 1.0],
        ["The quick brown fox jumps over the lazy dog.", 1.0],
        ["This is a test of different speeds. Listen carefully to the pronunciation.", 0.8],
        ["Fast speech example for testing purposes.", 1.5],
        ["IReader uses this TTS service to read books aloud with natural-sounding voices.", 1.0]
    ],
    cache_examples=False
)

if __name__ == "__main__":
    logger.info("🚀 Starting Coqui TTS service...")
    demo.launch(
        server_name="0.0.0.0",
        server_port=7860,
        show_error=True
    )
