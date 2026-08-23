package com.tradingbot.monitor.data.repository

import com.tradingbot.monitor.data.model.RiskConfig
import com.tradingbot.monitor.data.model.Trade
import com.tradingbot.monitor.data.remote.RetrofitClient
import com.tradingbot.monitor.data.remote.TradingWebSocketClient

class TradingRepository(
    private val wsClient: TradingWebSocketClient = TradingWebSocketClient()
) {
    val events = wsClient.events
    val connectionState = wsClient.connectionState

    fun startRealtimeStream() = wsClient.connect()
    fun stopRealtimeStream() = wsClient.disconnect()

    suspend fun fetchTradeHistory(): List<Trade> = RetrofitClient.api.getTradeHistory()

    suspend fun fetchRiskConfig(): RiskConfig = RetrofitClient.api.getRiskConfig()

    suspend fun updateRiskConfig(config: RiskConfig): RiskConfig =
        RetrofitClient.api.updateRiskConfig(config)

    suspend fun pauseBot() = RetrofitClient.api.pauseBot()

    suspend fun resumeBot() = RetrofitClient.api.resumeBot()

    suspend fun closeTrade(tradeId: String) = RetrofitClient.api.closeTrade(tradeId)
}
