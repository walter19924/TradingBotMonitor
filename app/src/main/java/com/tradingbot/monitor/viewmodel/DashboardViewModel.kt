package com.tradingbot.monitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingbot.monitor.data.model.RiskConfig
import com.tradingbot.monitor.data.model.Signal
import com.tradingbot.monitor.data.model.Trade
import com.tradingbot.monitor.data.remote.ConnectionState
import com.tradingbot.monitor.data.repository.TradingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class DashboardUiState(
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val signals: List<Signal> = emptyList(),
    val trades: List<Trade> = emptyList(),
    val riskConfig: RiskConfig? = null,
    val botEnabled: Boolean = true,
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val repository: TradingRepository = TradingRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeConnection()
        observeEvents()
        loadInitialData()
        repository.startRealtimeStream()
    }

    private fun observeConnection() {
        viewModelScope.launch {
            repository.connectionState.collect { state ->
                _uiState.value = _uiState.value.copy(connectionState = state)
            }
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            repository.events.collect { event ->
                when (event.type) {
                    "signal" -> event.signal?.let { newSignal ->
                        val updated = listOf(newSignal) + _uiState.value.signals
                        _uiState.value = _uiState.value.copy(signals = updated.take(50))
                    }
                    "trade_update" -> event.trade?.let { updatedTrade ->
                        val current = _uiState.value.trades.toMutableList()
                        val idx = current.indexOfFirst { it.id == updatedTrade.id }
                        if (idx >= 0) current[idx] = updatedTrade else current.add(0, updatedTrade)
                        _uiState.value = _uiState.value.copy(trades = current)
                    }
                }
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            runCatching {
                val trades = repository.fetchTradeHistory()
                val risk = repository.fetchRiskConfig()
                _uiState.value = _uiState.value.copy(
                    trades = trades,
                    riskConfig = risk,
                    botEnabled = risk.botEnabled
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun toggleBot() {
        viewModelScope.launch {
            runCatching {
                if (_uiState.value.botEnabled) repository.pauseBot() else repository.resumeBot()
                _uiState.value = _uiState.value.copy(botEnabled = !_uiState.value.botEnabled)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun closeTrade(tradeId: String) {
        viewModelScope.launch {
            runCatching { repository.closeTrade(tradeId) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(errorMessage = e.message) }
        }
    }

    override fun onCleared() {
        repository.stopRealtimeStream()
        super.onCleared()
    }
}
