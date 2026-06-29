package xyz.nulldev.ts.config

import com.typesafe.config.Config

open class ConfigModule(private val getConfig: () -> Config) {
    val config: Config get() = getConfig()

    operator fun <T> invoke(): T {
        @Suppress("UNCHECKED_CAST")
        return config as T
    }
}
