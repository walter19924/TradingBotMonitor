package com.tradingbot.monitor.data.remote

import com.tradingbot.monitor.data.model.RiskConfig
import com.tradingbot.monitor.data.model.Trade
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Endpoints que debe exponer tu microservicio FastAPI/Node (Execution Engine).
 * Ajusta las rutas a las que ya tengas implementadas en el backend.
 */
interface ApiService {

    @GET("api/trades")
    suspend fun getTradeHistory(): List<Trade>

    @GET("api/risk-config")
    suspend fun getRiskConfig(): RiskConfig

    @PUT("api/risk-config")
    suspend fun updateRiskConfig(@Body config: RiskConfig): RiskConfig

    @POST("api/bot/pause")
    suspend fun pauseBot()

    @POST("api/bot/resume")
    suspend fun resumeBot()

    @POST("api/trades/{id}/close")
    suspend fun closeTrade(@retrofit2.http.Path("id") tradeId: String)
}
