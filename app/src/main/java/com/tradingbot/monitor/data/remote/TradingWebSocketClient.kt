package com.tradingbot.monitor.data.remote

import com.google.gson.Gson
import com.tradingbot.monitor.BuildConfig
import com.tradingbot.monitor.data.model.WsEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

enum class ConnectionState { CONNECTING, CONNECTED, DISCONNECTED }

/**
 * Se conecta al stream del Orquestador (Execution Engine) y expone los eventos
 * (señales del Logic Engine + actualizaciones de trades) como un Flow.
 * Reconecta automáticamente con backoff simple si se cae la conexión.
 */
class TradingWebSocketClient(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val gson = Gson()
    private var webSocket: WebSocket? = null
    private var manuallyClosed = false
    private var retryDelayMs = 2000L

    private val _events = MutableSharedFlow<WsEvent>(replay = 0, extraBufferCapacity = 64)
    val events: SharedFlow<WsEvent> = _events

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    fun connect() {
        manuallyClosed = false
        openSocket()
    }

    private fun openSocket() {
        _connectionState.value = ConnectionState.CONNECTING
        val request = Request.Builder().url(BuildConfig.BASE_WS_URL).build()

        webSocket = RetrofitClient.wsOkHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = ConnectionState.CONNECTED
                retryDelayMs = 2000L // reset backoff al conectar bien
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                runCatching {
                    val event = gson.fromJson(text, WsEvent::class.java)
                    scope.launch { _events.emit(event) }
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.value = ConnectionState.DISCONNECTED
                scheduleReconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionState.value = ConnectionState.DISCONNECTED
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        if (manuallyClosed) return
        scope.launch {
            delay(retryDelayMs)
            retryDelayMs = (retryDelayMs * 1.5).toLong().coerceAtMost(30_000L)
            openSocket()
        }
    }

    fun disconnect() {
        manuallyClosed = true
        webSocket?.close(1000, "Cierre manual")
        _connectionState.value = ConnectionState.DISCONNECTED
    }
}
