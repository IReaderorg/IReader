package org.ireader.core_api.http.main

interface JS {
    fun evaluateAsString(script: String): String
    fun evaluateAsInt(script: String): Int
    fun evaluateAsDouble(script: String): Double
    fun evaluateAsBoolean(script: String): Boolean
}