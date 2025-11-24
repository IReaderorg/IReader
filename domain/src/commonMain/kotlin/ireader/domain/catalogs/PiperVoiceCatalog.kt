package ireader.domain.catalogs

import ireader.domain.models.tts.VoiceGender
import ireader.domain.models.tts.VoiceModel
import ireader.domain.models.tts.VoiceQuality

/**
 * Comprehensive catalog of Piper TTS voices
 * Shared between Android and Desktop platforms
 * 
 * Based on official Piper voices from HuggingFace
 */
object PiperVoiceCatalog {
    
    fun getAllVoices(): List<VoiceModel> = piperVoices
    
    fun getVoiceById(id: String): VoiceModel? = piperVoices.find { it.id == id }
    
    fun getVoicesByLanguage(language: String): List<VoiceModel> {
        return piperVoices.filter { it.language == language }
    }
    
    fun getSupportedLanguages(): List<String> {
        return piperVoices.map { it.language }.distinct().sorted()
    }
    
    private val piperVoices = listOf(
        // English (US) voices
        VoiceModel(
            id = "en_US-lessac-medium",
            name = "English (US) - Lessac - Medium",
            language = "en",
            locale = "en-US",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/lessac/medium/en_US-lessac-medium.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/lessac/medium/en_US-lessac-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Professional US English male voice with excellent clarity",
            tags = listOf("english", "us", "male", "professional")
        ),
        VoiceModel(
            id = "en_US-lessac-high",
            name = "English (US) - Lessac - High",
            language = "en",
            locale = "en-US",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.HIGH,
            sampleRate = 22050,
            modelSize = 94_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/lessac/high/en_US-lessac-high.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/lessac/high/en_US-lessac-high.onnx.json",
            checksum = "",
            license = "MIT",
            description = "High-quality US English male voice",
            tags = listOf("english", "us", "male", "high-quality")
        ),
        VoiceModel(
            id = "en_US-amy-medium",
            name = "English (US) - Amy - Medium",
            language = "en",
            locale = "en-US",
            gender = VoiceGender.FEMALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/amy/medium/en_US-amy-medium.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/amy/medium/en_US-amy-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Natural US English female voice",
            tags = listOf("english", "us", "female", "natural")
        ),
        VoiceModel(
            id = "en_US-libritts-high",
            name = "English (US) - LibriTTS - High",
            language = "en",
            locale = "en-US",
            gender = VoiceGender.FEMALE,
            quality = VoiceQuality.HIGH,
            sampleRate = 22050,
            modelSize = 94_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/libritts/high/en_US-libritts-high.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/libritts/high/en_US-libritts-high.onnx.json",
            checksum = "",
            license = "MIT",
            description = "High-quality US English female voice from LibriTTS",
            tags = listOf("english", "us", "female", "high-quality")
        ),
        VoiceModel(
            id = "en_US-ryan-high",
            name = "English (US) - Ryan - High",
            language = "en",
            locale = "en-US",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.HIGH,
            sampleRate = 22050,
            modelSize = 94_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/ryan/high/en_US-ryan-high.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/ryan/high/en_US-ryan-high.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Premium US English male voice",
            tags = listOf("english", "us", "male", "premium")
        ),
        
        // English (GB) voices
        VoiceModel(
            id = "en_GB-alan-medium",
            name = "English (GB) - Alan - Medium",
            language = "en",
            locale = "en-GB",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_GB/alan/medium/en_GB-alan-medium.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_GB/alan/medium/en_GB-alan-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Refined British English male voice",
            tags = listOf("english", "british", "uk", "male")
        ),
        VoiceModel(
            id = "en_GB-alba-medium",
            name = "English (GB) - Alba - Medium",
            language = "en",
            locale = "en-GB",
            gender = VoiceGender.FEMALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_GB/alba/medium/en_GB-alba-medium.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_GB/alba/medium/en_GB-alba-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Natural British English female voice",
            tags = listOf("english", "british", "uk", "female")
        ),
        
        // Spanish voices
        VoiceModel(
            id = "es_ES-mls_10246-low",
            name = "Spanish (Spain) - MLS 10246 - Low",
            language = "es",
            locale = "es-ES",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.LOW,
            sampleRate = 22050,
            modelSize = 18_874_368L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/es/es_ES/mls_10246/low/es_ES-mls_10246-low.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/es/es_ES/mls_10246/low/es_ES-mls_10246-low.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Spanish male voice",
            tags = listOf("spanish", "spain", "male")
        ),
        VoiceModel(
            id = "es_ES-mls_9972-low",
            name = "Spanish (Spain) - MLS 9972 - Low",
            language = "es",
            locale = "es-ES",
            gender = VoiceGender.FEMALE,
            quality = VoiceQuality.LOW,
            sampleRate = 22050,
            modelSize = 18_874_368L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/es/es_ES/mls_9972/low/es_ES-mls_9972-low.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/es/es_ES/mls_9972/low/es_ES-mls_9972-low.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Spanish female voice",
            tags = listOf("spanish", "spain", "female")
        ),
        VoiceModel(
            id = "es_MX-ald-medium",
            name = "Spanish (Mexico) - Ald - Medium",
            language = "es",
            locale = "es-MX",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/es/es_MX/ald/medium/es_MX-ald-medium.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/es/es_MX/ald/medium/es_MX-ald-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Mexican Spanish male voice",
            tags = listOf("spanish", "mexican", "male")
        ),
        
        // French voices
        VoiceModel(
            id = "fr_FR-siwis-medium",
            name = "French (France) - Siwis - Medium",
            language = "fr",
            locale = "fr-FR",
            gender = VoiceGender.FEMALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/fr/fr_FR/siwis/medium/fr_FR-siwis-medium.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/fr/fr_FR/siwis/medium/fr_FR-siwis-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Elegant French female voice",
            tags = listOf("french", "france", "female")
        ),
        VoiceModel(
            id = "fr_FR-upmc-medium",
            name = "French (France) - UPMC - Medium",
            language = "fr",
            locale = "fr-FR",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/fr/fr_FR/upmc/medium/fr_FR-upmc-medium.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/fr/fr_FR/upmc/medium/fr_FR-upmc-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Natural French male voice",
            tags = listOf("french", "france", "male")
        ),
        
        // German voices
        VoiceModel(
            id = "de_DE-thorsten-medium",
            name = "German (Germany) - Thorsten - Medium",
            language = "de",
            locale = "de-DE",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/de/de_DE/thorsten/medium/de_DE-thorsten-medium.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/de/de_DE/thorsten/medium/de_DE-thorsten-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Professional German male voice",
            tags = listOf("german", "germany", "male")
        ),
        VoiceModel(
            id = "de_DE-thorsten-high",
            name = "German (Germany) - Thorsten - High",
            language = "de",
            locale = "de-DE",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.HIGH,
            sampleRate = 22050,
            modelSize = 94_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/de/de_DE/thorsten/high/de_DE-thorsten-high.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/de/de_DE/thorsten/high/de_DE-thorsten-high.onnx.json",
            checksum = "",
            license = "MIT",
            description = "High-quality German male voice",
            tags = listOf("german", "germany", "male", "high-quality")
        ),
        VoiceModel(
            id = "de_DE-eva_k-medium",
            name = "German (Germany) - Eva K - Medium",
            language = "de",
            locale = "de-DE",
            gender = VoiceGender.FEMALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/de/de_DE/eva_k/medium/de_DE-eva_k-medium.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/de/de_DE/eva_k/medium/de_DE-eva_k-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Clear German female voice",
            tags = listOf("german", "germany", "female")
        ),
        
        // Italian voices
        VoiceModel(
            id = "it_IT-riccardo-medium",
            name = "Italian (Italy) - Riccardo - Medium",
            language = "it",
            locale = "it-IT",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/it/it_IT/riccardo/x_low/it_IT-riccardo-x_low.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/it/it_IT/riccardo/x_low/it_IT-riccardo-x_low.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Expressive Italian male voice",
            tags = listOf("italian", "italy", "male")
        ),
        
        // Portuguese voices
        VoiceModel(
            id = "pt_BR-faber-medium",
            name = "Portuguese (Brazil) - Faber - Medium",
            language = "pt",
            locale = "pt-BR",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/pt/pt_BR/faber/medium/pt_BR-faber-medium.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/pt/pt_BR/faber/medium/pt_BR-faber-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Natural Brazilian Portuguese male voice",
            tags = listOf("portuguese", "brazilian", "brazil", "male")
        ),
        VoiceModel(
            id = "pt_PT-tugao-medium",
            name = "Portuguese (Portugal) - Tugão - Medium",
            language = "pt",
            locale = "pt-PT",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/pt/pt_PT/tugao/medium/pt_PT-tugao-medium.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/pt/pt_PT/tugao/medium/pt_PT-tugao-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "European Portuguese male voice",
            tags = listOf("portuguese", "portugal", "male")
        ),
        
        // Chinese voices
        VoiceModel(
            id = "zh_CN-huayan-medium",
            name = "Chinese (Mandarin) - Huayan - Medium",
            language = "zh",
            locale = "zh-CN",
            gender = VoiceGender.FEMALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/zh/zh_CN/huayan/medium/zh_CN-huayan-medium.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/zh/zh_CN/huayan/medium/zh_CN-huayan-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Natural Mandarin Chinese female voice",
            tags = listOf("chinese", "mandarin", "female")
        ),
        
        // Japanese voices
        VoiceModel(
            id = "ja_JP-natsumivoice-medium",
            name = "Japanese - Natsumi - Medium",
            language = "ja",
            locale = "ja-JP",
            gender = VoiceGender.FEMALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/ja/ja_JP/natsumivoice/medium/ja_JP-natsumivoice-medium.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/ja/ja_JP/natsumivoice/medium/ja_JP-natsumivoice-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Clear Japanese female voice",
            tags = listOf("japanese", "japan", "female")
        ),
        
        // Korean voices
        VoiceModel(
            id = "ko_KR-kss-medium",
            name = "Korean - KSS - Medium",
            language = "ko",
            locale = "ko-KR",
            gender = VoiceGender.FEMALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/ko/ko_KR/kss/medium/ko_KR-kss-medium.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/ko/ko_KR/kss/medium/ko_KR-kss-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Natural Korean female voice",
            tags = listOf("korean", "korea", "female")
        ),
        
        // Russian voices
        VoiceModel(
            id = "ru_RU-dmitri-medium",
            name = "Russian - Dmitri - Medium",
            language = "ru",
            locale = "ru-RU",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/ru/ru_RU/dmitri/medium/ru_RU-dmitri-medium.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/ru/ru_RU/dmitri/medium/ru_RU-dmitri-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Clear Russian male voice",
            tags = listOf("russian", "russia", "male")
        ),
        VoiceModel(
            id = "ru_RU-irina-medium",
            name = "Russian - Irina - Medium",
            language = "ru",
            locale = "ru-RU",
            gender = VoiceGender.FEMALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/ru/ru_RU/irina/medium/ru_RU-irina-medium.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/ru/ru_RU/irina/medium/ru_RU-irina-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Natural Russian female voice",
            tags = listOf("russian", "russia", "female")
        ),
        
        // Arabic voices
        VoiceModel(
            id = "ar_JO-kareem-medium",
            name = "Arabic (Jordan) - Kareem - Medium",
            language = "ar",
            locale = "ar-JO",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/ar/ar_JO/kareem/medium/ar_JO-kareem-medium.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/ar/ar_JO/kareem/medium/ar_JO-kareem-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Natural Arabic male voice",
            tags = listOf("arabic", "jordan", "male")
        ),
        
        // Dutch voices
        VoiceModel(
            id = "nl_NL-mls-medium",
            name = "Dutch (Netherlands) - MLS - Medium",
            language = "nl",
            locale = "nl-NL",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/nl/nl_NL/mls/medium/nl_NL-mls-medium.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/nl/nl_NL/mls/medium/nl_NL-mls-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Natural Dutch male voice",
            tags = listOf("dutch", "netherlands", "male")
        ),
        
        // Polish voices
        VoiceModel(
            id = "pl_PL-mls_6892-low",
            name = "Polish - MLS 6892 - Low",
            language = "pl",
            locale = "pl-PL",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.LOW,
            sampleRate = 22050,
            modelSize = 18_874_368L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/pl/pl_PL/mls_6892/low/pl_PL-mls_6892-low.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/pl/pl_PL/mls_6892/low/pl_PL-mls_6892-low.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Polish male voice",
            tags = listOf("polish", "poland", "male")
        ),
        
        // Turkish voices
        VoiceModel(
            id = "tr_TR-dfki-medium",
            name = "Turkish - DFKI - Medium",
            language = "tr",
            locale = "tr-TR",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/tr/tr_TR/dfki/medium/tr_TR-dfki-medium.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/tr/tr_TR/dfki/medium/tr_TR-dfki-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Natural Turkish male voice",
            tags = listOf("turkish", "turkey", "male")
        ),
        
        // Ukrainian voices
        VoiceModel(
            id = "uk_UA-lada-medium",
            name = "Ukrainian - Lada - Medium",
            language = "uk",
            locale = "uk-UA",
            gender = VoiceGender.FEMALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/uk/uk_UA/lada/medium/uk_UA-lada-medium.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/uk/uk_UA/lada/medium/uk_UA-lada-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Natural Ukrainian female voice",
            tags = listOf("ukrainian", "ukraine", "female")
        ),
        
        // Vietnamese voices
        VoiceModel(
            id = "vi_VN-vais1000-medium",
            name = "Vietnamese - VAIS1000 - Medium",
            language = "vi",
            locale = "vi-VN",
            gender = VoiceGender.MALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/vi/vi_VN/vais1000/medium/vi_VN-vais1000-medium.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/vi/vi_VN/vais1000/medium/vi_VN-vais1000-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Natural Vietnamese male voice",
            tags = listOf("vietnamese", "vietnam", "male")
        ),
        
        // Hindi voices
        VoiceModel(
            id = "hi_IN-coqui-medium",
            name = "Hindi (India) - Coqui - Medium",
            language = "hi",
            locale = "hi-IN",
            gender = VoiceGender.FEMALE,
            quality = VoiceQuality.MEDIUM,
            sampleRate = 22050,
            modelSize = 63_201_308L,
            downloadUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/hi/hi_IN/coqui/medium/hi_IN-coqui-medium.onnx",
            configUrl = "https://huggingface.co/rhasspy/piper-voices/resolve/main/hi/hi_IN/coqui/medium/hi_IN-coqui-medium.onnx.json",
            checksum = "",
            license = "MIT",
            description = "Natural Hindi female voice",
            tags = listOf("hindi", "india", "female")
        )
    )
}
