package xyz.nulldev.ts.config

interface ConfigManager {
    val config: com.typesafe.config.Config
    fun <T : Any> module(clazz: Class<T>): T
}

inline fun <reified T : Any> ConfigManager.moduleOf(): T = module(T::class.java)
