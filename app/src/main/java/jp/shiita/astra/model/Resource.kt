package jp.shiita.astra.model

sealed class Resource<out T> {
    fun <R> map(transform: (T) -> R): Resource<R> = when (this) {
        is SuccessResource -> SuccessResource(transform(data))
        is ErrorResource -> ErrorResource(message, data?.let { transform(it) })
    }
}

data class SuccessResource<out T>(val data: T) : Resource<T>()

data class ErrorResource<out T>(val message: String, val data: T?) : Resource<T>()