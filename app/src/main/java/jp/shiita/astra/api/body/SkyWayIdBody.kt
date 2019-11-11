package jp.shiita.astra.api.body

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SkyWayIdBody(
    @Json(name = "skyway_id") val ownId: String
)