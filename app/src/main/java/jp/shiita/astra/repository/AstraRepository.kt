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
    suspend fun postSkyWayId(ownId: String, phi: Int, theta: Int): Resource<String?> =
        when (val res = runCatching {
            astraService.postSkyWayId(
                SkyWayIdWithPosBody(ownId, phi, theta)
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