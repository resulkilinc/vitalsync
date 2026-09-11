package com.vitalsync.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Demo REST uçları — JSONPlaceholder HTTPS (ögretim rubric'i: Retrofit + REST).
 * POST başarılı olduğunda uzak kimlik döner; gerçek hasta sunucusu değildir.
 */
interface VitalsRemoteApi {

    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: Long): Response<RemotePostDto>

    @POST("posts")
    suspend fun uploadPost(@Body body: VitalUploadRequestDto): Response<VitalUploadResponseDto>
}
