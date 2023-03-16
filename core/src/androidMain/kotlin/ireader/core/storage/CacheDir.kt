package ireader.core.storage

import android.os.Environment
import java.io.File

val CacheDir: File =  File(Environment.getExternalStorageDirectory(), "IReader/Extensions/")