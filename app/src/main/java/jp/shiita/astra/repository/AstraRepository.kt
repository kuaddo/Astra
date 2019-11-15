package jp.shiita.astra.repository

import android.util.Base64
import jp.shiita.astra.api.ApiErrorResponse
import jp.shiita.astra.api.ApiSuccessResponse
import jp.shiita.astra.api.AstraService
import jp.shiita.astra.api.body.ImageBody
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

    suspend fun postImage(ownId: String, imageBytes: ByteArray): Resource<Unit> =
        when (val res = runCatching {
            astraService.postImage(ownId, ImageBody(imageBytes.toBase64()))
        }.toApiResponse()) {
            is ApiSuccessResponse -> SuccessResource(Unit)
            is ApiErrorResponse -> ErrorResource(res.message, null)
        }

    suspend fun getImages(opponentId: String): Resource<List<ByteArray>> =
        when (val res = runCatching {
            astraService.getImages(opponentId)
        }.toApiResponse()) {
            is ApiSuccessResponse -> {
                val byteArrayList = res.body.map { it.image.fromBase64() }
                SuccessResource(byteArrayList)
            }
            is ApiErrorResponse -> ErrorResource(res.message, null)
        }

    private fun ByteArray.toBase64(): String = Base64.encodeToString(this, Base64.NO_WRAP)

    private fun String.fromBase64(): ByteArray = Base64.decode(this, Base64.NO_WRAP)
}