package jp.shiita.astra.api.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SkyWayIdResponse(
    @Json(name = "skyway_id") val opponentId: String?
)