#!/usr/bin/env python3
"""
Gemini I18n Translator - Optimized with Multi-Key Support
Translates missing strings in strings.xml files using Google Gemini API.
Supports multiple API keys with automatic rotation on rate limits.
"""

import os
import sys
import xml.etree.ElementTree as ET
import requests
import time
import json
from pathlib import Path
from typing import Dict, List, Optional


# Configuration
API_KEYS = []
# Load API keys from environment variable (comma-separated)
if os.environ.get('GEMINI_API_KEY'):
    API_KEYS = [key.strip() for key in os.environ.get('GEMINI_API_KEY').split(',') if key.strip()]
# Also support multiple keys as GEMINI_API_KEY_1, GEMINI_API_KEY_2, etc.
i = 1
while os.environ.get(f'GEMINI_API_KEY_{i}'):
    API_KEYS.append(os.environ.get(f'GEMINI_API_KEY_{i}'))
    i += 1

API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
PROJECT_ROOT = Path('.')
I18N_BASE_DIR = PROJECT_ROOT / 'i18n/src/commonMain/composeResources'
SOURCE_FILE = I18N_BASE_DIR / 'values/strings.xml'
BATCH_SIZE = 200  # Max strings per request (Gemini 2.0 Flash handles ~30k tokens)
MAX_RETRIES_PER_KEY = 2   # Retry on rate limit per key before switching

# Track current API key index and rate-limited keys
current_key_index = 0
rate_limited_keys = set()  # Track which keys are rate-limited

LANG_NAMES = {
    'ar': 'Arabic', 'de': 'German', 'es': 'Spanish', 'fr': 'French',
    'it': 'Italian', 'ja': 'Japanese', 'ko': 'Korean', 'pt': 'Portuguese',
    'ru': 'Russian', 'zh-rCN': 'Simplified Chinese', 'zh-rTW': 'Traditional Chinese',
    'hi': 'Hindi', 'tr': 'Turkish', 'vi': 'Vietnamese', 'th': 'Thai',
    'id': 'Indonesian', 'ms': 'Malay', 'fil': 'Filipino', 'uk': 'Ukrainian',
    'pl': 'Polish', 'nl': 'Dutch', 'cs': 'Czech', 'hu': 'Hungarian',
    'ro': 'Romanian', 'sv': 'Swedish', 'da': 'Danish', 'fi': 'Finnish',
    'no': 'Norwegian', 'el': 'Greek', 'he': 'Hebrew', 'fa': 'Persian',
}

def get_next_api_key() -> Optional[str]:
    """Get the next available API key, rotating through available keys."""
    global current_key_index
    
    if not API_KEYS:
        return None
    
    # If all keys are rate-limited, return None
    if len(rate_limited_keys) >= len(API_KEYS):
        return None
    
    # Find next non-rate-limited key
    attempts = 0
    while attempts < len(API_KEYS):
        key = API_KEYS[current_key_index]
        key_id = current_key_index
        current_key_index = (current_key_index + 1) % len(API_KEYS)
        
        if key_id not in rate_limited_keys:
            return key
        
        attempts += 1
    
    return None


def mark_key_rate_limited(key: str):
    """Mark an API key as rate-limited."""
    global rate_limited_keys
    try:
        key_index = API_KEYS.index(key)
        rate_limited_keys.add(key_index)
        print(f"    [Key {key_index + 1}] Marked as rate-limited ({len(rate_limited_keys)}/{len(API_KEYS)} keys exhausted)")
    except ValueError:
        pass


def reset_rate_limits():
    """Reset rate limit tracking (call after waiting period)."""
    global rate_limited_keys
    rate_limited_keys.clear()


def setup_io():
    if sys.platform == 'win32':
        sys.stdout.reconfigure(encoding='utf-8')
        sys.stderr.reconfigure(encoding='utf-8')

def load_strings(file_path: Path) -> Dict[str, str]:
    if not file_path.exists():
        return {}
    try:
        tree = ET.parse(file_path)
        return {e.get('name'): e.text for e in tree.getroot().findall('string') if e.get('name')}
    except Exception as e:
        print(f"Error loading {file_path}: {e}")
        return {}

def save_strings(file_path: Path, strings: Dict[str, str]):
    root = ET.Element('resources')
    for key in sorted(strings.keys()):
        elem = ET.SubElement(root, 'string')
        elem.set('name', key)
        elem.text = strings[key]
    indent_xml(root)
    file_path.parent.mkdir(parents=True, exist_ok=True)
    ET.ElementTree(root).write(file_path, encoding='utf-8', xml_declaration=False)

def indent_xml(elem, level=0):
    indent = "\n" + "    " * level
    if len(elem):
        if not elem.text or not elem.text.strip():
            elem.text = indent + "    "
        if not elem.tail or not elem.tail.strip():
            elem.tail = indent
        for child in elem:
            indent_xml(child, level + 1)
        if not child.tail or not child.tail.strip():
            child.tail = indent
    elif level and (not elem.tail or not elem.tail.strip()):
        elem.tail = indent


def translate_batch(strings: Dict[str, str], target_lang: str, batch_num: int = 1, total_batches: int = 1) -> Dict[str, str]:
    """Translate using compact JSON format to minimize tokens. Supports multiple API keys with rotation."""
    if not API_KEYS:
        print(f"    [ERROR] No API keys configured")
        return {}
    
    lang_name = LANG_NAMES.get(target_lang, target_lang)
    input_data = {k: v for k, v in strings.items() if v}
    
    print(f"    [Batch {batch_num}/{total_batches}] Translating {len(input_data)} strings to {lang_name}...")
    
    prompt = f"""You are translating UI strings for IReader, an Android novel/book reader app.
Context: This app lets users read novels, manage their library, browse book sources, customize reading settings (fonts, themes, scroll modes), and track reading progress. Terms like "chapter", "source", "library", "bookmark" refer to book/novel reading features.

Translate to {lang_name}. Return JSON only.
Keep %1$s %d etc placeholders unchanged. Output: {{"key":"translation",...}}
Input: {json.dumps(input_data, ensure_ascii=False)}"""

    # Try each available API key
    keys_tried = 0
    max_keys_to_try = len(API_KEYS)
    
    while keys_tried < max_keys_to_try:
        api_key = get_next_api_key()
        
        if not api_key:
            print(f"    [Batch {batch_num}] All API keys are rate-limited!")
            # Wait and reset rate limits
            wait_time = 60
            print(f"    [Batch {batch_num}] Waiting {wait_time}s for rate limits to reset...")
            time.sleep(wait_time)
            reset_rate_limits()
            api_key = get_next_api_key()
            if not api_key:
                print(f"    [Batch {batch_num}] Still no available keys after waiting")
                return {}
        
        key_index = API_KEYS.index(api_key) + 1
        print(f"    [Batch {batch_num}] Using API key #{key_index}")
        
        # Try this key with retries
        for attempt in range(MAX_RETRIES_PER_KEY):
            try:
                print(f"    [Batch {batch_num}] Sending request (key #{key_index}, attempt {attempt + 1}/{MAX_RETRIES_PER_KEY})...")
                start_time = time.time()
                
                response = requests.post(
                    f"{API_URL}?key={api_key}",
                    headers={'Content-Type': 'application/json'},
                    json={"contents": [{"parts": [{"text": prompt}]}]},
                    timeout=60
                )
                
                elapsed = time.time() - start_time
                print(f"    [Batch {batch_num}] Response received in {elapsed:.1f}s (status: {response.status_code})")
                
                if response.status_code == 429:
                    wait = (2 ** attempt) * 3  # 3s, 6s
                    print(f"    [Batch {batch_num}] Rate limited on key #{key_index}! Waiting {wait}s...")
                    time.sleep(wait)
                    continue
                
                if response.status_code != 200:
                    print(f"    [Batch {batch_num}] API Error {response.status_code}: {response.text[:200]}")
                    if response.status_code == 429:
                        # Mark this key as rate-limited and try next key
                        mark_key_rate_limited(api_key)
                        break
                    return {}
                
                result = response.json()
                if 'candidates' not in result or not result['candidates']:
                    print(f"    [Batch {batch_num}] No candidates in response")
                    return {}
                
                content = result['candidates'][0]['content']['parts'][0]['text']
                content = content.replace('```json', '').replace('```', '').strip()
                
                parsed = json.loads(content)
                print(f"    [Batch {batch_num}] ✓ Successfully parsed {len(parsed)} translations using key #{key_index}")
                return parsed
                
            except json.JSONDecodeError as e:
                print(f"    [Batch {batch_num}] JSON parse error: {e}")
                print(f"    [Batch {batch_num}] Raw response: {content[:300]}...")
                return {}
            except requests.Timeout:
                print(f"    [Batch {batch_num}] Request timed out after 60s")
                if attempt < MAX_RETRIES_PER_KEY - 1:
                    continue
                return {}
            except Exception as e:
                print(f"    [Batch {batch_num}] Request error: {type(e).__name__}: {e}")
                if attempt < MAX_RETRIES_PER_KEY - 1:
                    continue
                return {}
        
        # If we exhausted retries on this key due to rate limiting, mark it and try next
        if response.status_code == 429:
            mark_key_rate_limited(api_key)
            keys_tried += 1
            print(f"    [Batch {batch_num}] Switching to next API key...")
            time.sleep(2)  # Brief pause before trying next key
            continue
        
        # If we got here, we failed for non-rate-limit reasons
        break
    
    print(f"    [Batch {batch_num}] Failed after trying {keys_tried} API key(s)")
    return {}

def process_language(lang_dir: Path, source_strings: Dict[str, str], lang_num: int = 1, total_langs: int = 1) -> str:
    """Process a single language, returns status message."""
    lang_code = lang_dir.name.replace('values-', '')
    target_file = lang_dir / 'strings.xml'
    
    print(f"\n[{lang_num}/{total_langs}] Processing {lang_code}...")
    print(f"  Target file: {target_file}")
    
    current_strings = load_strings(target_file)
    print(f"  Existing translations: {len(current_strings)}")
    
    # Only get missing strings
    missing = {k: v for k, v in source_strings.items() if k not in current_strings and v}
    
    if not missing:
        return f"✓ [{lang_code}] Up to date (0 missing)"
    
    print(f"  Missing translations: {len(missing)}")
    
    keys = list(missing.keys())
    total_batches = (len(keys) - 1) // BATCH_SIZE + 1
    new_translations = {}
    
    for i in range(0, len(keys), BATCH_SIZE):
        batch_num = i // BATCH_SIZE + 1
        batch = {k: missing[k] for k in keys[i:i+BATCH_SIZE]}
        translated = translate_batch(batch, lang_code, batch_num, total_batches)
        new_translations.update(translated)
        if i + BATCH_SIZE < len(keys):
            print(f"    Waiting 2s before next batch...")
            time.sleep(2)  # Rate limit between batches
    
    if new_translations:
        current_strings.update(new_translations)
        save_strings(target_file, current_strings)
        print(f"  Saved to {target_file}")
        return f"✓ [{lang_code}] Added {len(new_translations)}/{len(missing)} strings"
    return f"⚠ [{lang_code}] Translation failed (0/{len(missing)} strings)"

def main():
    setup_io()
    
    if not API_KEYS:
        print("❌ No API keys configured!")
        print("   Set GEMINI_API_KEY environment variable with comma-separated keys:")
        print("   export GEMINI_API_KEY='key1,key2,key3'")
        print("   OR use GEMINI_API_KEY_1, GEMINI_API_KEY_2, etc.")
        sys.exit(1)
    
    print(f"✓ Loaded {len(API_KEYS)} API key(s)")
    
    print(f"Loading source strings...")
    source_strings = load_strings(SOURCE_FILE)
    print(f"Loaded {len(source_strings)} strings")
    
    lang_dirs = sorted([d for d in I18N_BASE_DIR.iterdir() 
                        if d.is_dir() and d.name.startswith('values-')])
    
    print(f"Found {len(lang_dirs)} target languages: {[d.name.replace('values-', '') for d in lang_dirs]}")
    print("=" * 60)
    
    # Sequential to avoid rate limits
    success_count = 0
    fail_count = 0
    skip_count = 0
    
    for idx, lang_dir in enumerate(lang_dirs, 1):
        result = process_language(lang_dir, source_strings, idx, len(lang_dirs))
        print(result)
        
        if "✓" in result and "Up to date" in result:
            skip_count += 1
        elif "✓" in result:
            success_count += 1
        else:
            fail_count += 1
        
        if idx < len(lang_dirs):
            print("  Waiting 1s before next language...")
            time.sleep(1)
    
    print("\n" + "=" * 60)
    print(f"Summary: {success_count} updated, {skip_count} skipped, {fail_count} failed")
    print(f"API Keys: {len(API_KEYS)} total, {len(rate_limited_keys)} rate-limited")
    
    print("\n✨ Done!")

if __name__ == '__main__':
    main()
