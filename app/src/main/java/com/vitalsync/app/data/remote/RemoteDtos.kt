package com.vitalsync.app.data.remote

import com.google.gson.annotations.SerializedName

/** JSONPlaceholder GET /posts/{id} */
data class RemotePostDto(
    val userId: Long?,
    val id: Long?,
    val title: String?,
    val body: String?,
)

/** JSONPlaceholder POST /posts istek gövdesi */
data class VitalUploadRequestDto(
    val title: String,
    val body: String,
    @SerializedName("userId") val userId: Long,
)

/** JSONPlaceholder POST yanıtı (id üretilir) */
data class VitalUploadResponseDto(
    val id: Long?,
    val title: String?,
    val body: String?,
    @SerializedName("userId") val userId: Long?,
)
