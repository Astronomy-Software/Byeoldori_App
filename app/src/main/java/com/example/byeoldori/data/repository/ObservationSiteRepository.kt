package com.example.byeoldori.data.repository

import com.example.byeoldori.data.api.ObservationSiteApi
import com.example.byeoldori.data.model.dto.ObservationSite
import com.example.byeoldori.data.model.dto.ObservationSiteRegisterRequest
import com.example.byeoldori.data.model.dto.ObservationSiteUpdateRequest
import com.example.byeoldori.data.model.dto.ObservationSitesRecommendRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObservationSiteRepository @Inject constructor(
    private val api: ObservationSiteApi
) {

    /** 모든 관측지 조회 */
    suspend fun getAllSites(): List<ObservationSite> {
        return api.getAllSites()
    }

    /** 특정 관측지 조회 */
    suspend fun getSiteByName(name: String): List<ObservationSite> {
        return api.getSiteByName(name)
    }

    /** 관측지 등록 */
    suspend fun registerSite(req: ObservationSiteRegisterRequest): ObservationSite {
        return api.registerSite(req)
    }

    /** 관측지 추천 */
    suspend fun recommendSite(req: ObservationSitesRecommendRequest): List<ObservationSite> {
        return api.recommendSite(req)
    }

    /** 관측지 수정 */
    suspend fun updateSite(name: String, req: ObservationSiteUpdateRequest): ObservationSite {
        return api.updateSite(name, req)
    }

    /** 관측지 삭제 */
    suspend fun deleteSite(name: String) {
        // 서버 응답 타입에 따라 Any / Unit / ApiResponse 로 조정 가능
        api.deleteSite(name)
    }
}
