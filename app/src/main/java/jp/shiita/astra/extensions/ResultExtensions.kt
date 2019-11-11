package jp.shiita.astra.extensions

import jp.shiita.astra.api.ApiResponse
import retrofit2.Response
import timber.log.Timber

fun <T> Result<Response<T>>.toApiResponse(): ApiResponse<T> =
    fold(
        onSuccess = { ApiResponse.create(it) },
        onFailure = {
            Timber.e(it)
            ApiResponse.create(it)
        }
    )