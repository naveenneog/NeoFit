package com.neofit.core.common

/** Generic screen state used by ViewModels exposing a single StateFlow. */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data object Empty : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

/** Result wrapper for repository/integration calls that may fail gracefully. */
sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Failure(val message: String, val cause: Throwable? = null) : DataResult<Nothing>

    fun getOrNull(): T? = (this as? Success)?.data

    fun <R> map(transform: (T) -> R): DataResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> this
    }

    companion object {
        inline fun <T> catching(block: () -> T): DataResult<T> = try {
            Success(block())
        } catch (t: Throwable) {
            Failure(t.message ?: "Unexpected error", t)
        }
    }
}
