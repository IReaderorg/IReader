package ir.kazemcodes.infinity.domain.model

sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    class Success<T>(val data: T) : Resource<T>()
    class Error(val message: Throwable) : Resource<Nothing>()
}
