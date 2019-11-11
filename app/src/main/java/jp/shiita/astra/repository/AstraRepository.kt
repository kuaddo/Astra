package jp.shiita.astra.repository

import jp.shiita.astra.api.ApiErrorResponse
import jp.shiita.astra.api.ApiSuccessResponse
import jp.shiita.astra.api.AstraService
import jp.shiita.astra.api.body.SkyWayIdBody
import jp.shiita.astra.api.body.SkyWayIdWithPosBody
import jp.shiita.astra.extensions.toApiResponse
import jp.shiita.astra.model.ErrorResource
import jp.shiita.astra.model.Resource
import jp.shiita.astra.model.SuccessResource
import javax.inject.Inject

class AstraRepository @Inject constructor(
    private val astraService: AstraService
) {
    // TODO: x, y, zに関しては座標取得の実装に合わせて修正する
    suspend fun postSkyWayId(ownId: String, x: Double, y: Double, z: Double): Resource<String?> =
        when (val res = runCatching {
            astraService.postSkyWayId(
                SkyWayIdWithPosBody(ownId, x, y, z)
            )
        }.toApiResponse()) {
            is ApiSuccessResponse -> SuccessResource(res.body.opponentId)
            is ApiErrorResponse -> ErrorResource(res.message, null)
        }

    suspend fun deleteSkyWayId(ownId: String): Resource<Unit> =
        when (val res = runCatching {
            astraService.deleteSkyWayId(SkyWayIdBody(ownId))
        }.toApiResponse()) {
            is ApiSuccessResponse -> SuccessResource(Unit)
            is ApiErrorResponse -> ErrorResource(res.message, null)
        }
}