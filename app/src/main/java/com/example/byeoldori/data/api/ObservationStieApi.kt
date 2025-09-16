
package com.example.byeoldori.data.api

import com.example.byeoldori.data.model.dto.*
import retrofit2.http.*

interface ObservationSiteApi {
    @GET("observationsites")
    suspend fun getAllSites(): List<ObservationSite>

    @GET("observationsites/{name}")
    suspend fun getSiteByName(
        @Path("name") name: String
    ): List<ObservationSite>

    @POST("observationsites")
    suspend fun registerSite(
        @Body body: ObservationSiteRegisterRequest
    ): ObservationSite

    @POST("observationsites/recommend")
    suspend fun recommendSite(
        @Body body: ObservationSitesRecommendRequest
    ): List<ObservationSite>

    @PUT("observationsites/{name}")
    suspend fun updateSite(
        @Path("name") name: String,
        @Body body: ObservationSiteUpdateRequest,
    ): ObservationSite

    @DELETE("observationsites/{name}")
    suspend fun deleteSite(
        @Path("name") name: String
    ): Any   // TODO: Response 모델 뭔지 모르겠음??
}
