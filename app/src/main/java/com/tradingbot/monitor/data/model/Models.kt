package com.tradingbot.monitor.data.model

/**
 * Señal generada por el motor de análisis (RSI + MACD + Bollinger, etc.)
 * Coincide con lo que tu Logic Engine debería publicar por WebSocket / REST.
 */
data class Signal(
    val id: String,
    val symbol: String,          // ej. "BTC/USDT"
    val exchange: String,        // ej. "Binance"
    val direction: String,       // "BUY" | "SELL"
    val confluence: List<String>, // ej. ["RSI<30", "MACD_CROSS_UP", "BB_LOWER"]
    val price: Double,
    val timestamp: Long
)

/**
 * Operación ejecutada por el Execution Engine.
 */
data class Trade(
    val id: String,
    val symbol: String,
    val exchange: String,
    val side: String,            // "BUY" | "SELL"
    val entryPrice: Double,
    val quantity: Double,
    val stopLoss: Double?,
    val takeProfit: Double?,
    val status: String,          // "OPEN" | "CLOSED" | "CANCELLED"
    val pnl: Double?,
    val timestamp: Long
)

/**
 * Configuración de riesgo actual (editable desde la app, aplicada en el backend).
 */
data class RiskConfig(
    val maxPositionSizePercent: Double,
    val defaultStopLossPercent: Double,
    val defaultTakeProfitPercent: Double,
    val maxOpenTrades: Int,
    val botEnabled: Boolean
)

/**
 * Envoltorio genérico de los mensajes que llegan por WebSocket.
 * type: "signal" | "trade_update" | "status" | "heartbeat"
 */
data class WsEvent(
    val type: String,
    val signal: Signal? = null,
    val trade: Trade? = null,
    val statusMessage: String? = null
)
