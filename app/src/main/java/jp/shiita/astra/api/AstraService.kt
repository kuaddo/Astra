package jp.shiita.astra.api

import jp.shiita.astra.api.body.ImageBody
import jp.shiita.astra.api.body.SkyWayIdBody
import jp.shiita.astra.api.body.SkyWayIdWithPosBody
import jp.shiita.astra.api.response.ImageResponse
import jp.shiita.astra.api.response.SkyWayIdResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path

interface AstraService {
    @POST("v1/waiters")
    suspend fun postSkyWayId(@Body skyWayIdWithPosBody: SkyWayIdWithPosBody): Response<SkyWayIdResponse>

    @HTTP(method = "DELETE", path = "v1/waiters", hasBody = true)
    suspend fun deleteSkyWayId(@Body skyWayIdBody: SkyWayIdBody): Response<Unit>

    @POST("v1/images/{skyWayId}")
    suspend fun postImage(@Path("skyWayId") skyWayId: String, @Body imageBody: ImageBody): Response<Unit>

    @GET("v1/images/{skyWayId}")
    suspend fun getImages(@Path("skyWayId") skyWayId: String): Response<List<ImageResponse>>
}