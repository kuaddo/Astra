package jp.shiita.astra.api

import jp.shiita.astra.api.body.SkyWayIdBody
import jp.shiita.astra.api.body.SkyWayIdWithPosBody
import jp.shiita.astra.api.response.SkyWayIdResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.POST

interface AstraService {
    @POST("v1/waiters")
    suspend fun postSkyWayId(@Body skyWayIdWithPosBody: SkyWayIdWithPosBody): Response<SkyWayIdResponse>

    @HTTP(method = "DELETE", path = "v1/waiters", hasBody = true)
    suspend fun deleteSkyWayId(@Body skyWayIdBody: SkyWayIdBody): Response<Unit>
}