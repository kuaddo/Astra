package jp.shiita.astra.api

import retrofit2.Response

/**
 * Common class used by API responses.
 * @param <T> the type of the response object
 */
sealed class ApiResponse<T> {
    companion object {
        fun <T> create(error: Throwable): ApiErrorResponse<T> {
            return ApiErrorResponse(error.message ?: "unknown error")
        }

        fun <T> create(response: Response<T>): ApiResponse<T> {
            return if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    ApiErrorResponse("empty response error")
                } else {
                    // TODO: APIがstatus codeに対応したら修正
//                    val status = body.status
//                    when (status.code) {
//                        "0000" -> ApiSuccessResponse(body)
//                        else -> ApiErrorCodeResponse(status.code, status.errorMessage ?: "")
//                    }
                    ApiSuccessResponse(body)
                }
            } else {
                val msg = response.errorBody()?.string()
                val errorMsg = if (msg.isNullOrEmpty()) response.message() else msg
                ApiErrorResponse(errorMsg ?: "unknown error")
            }
        }
    }
}

data class ApiSuccessResponse<T>(val body: T) : ApiResponse<T>()

data class ApiErrorResponse<T>(val message: String) : ApiResponse<T>()

//data class ApiErrorCodeResponse<T : CommonResponse>(
//    val code: String,
//    val message: String
//) : ApiResponse<T>()