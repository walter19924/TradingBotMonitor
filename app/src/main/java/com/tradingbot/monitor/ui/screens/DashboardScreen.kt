package com.tradingbot.monitor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tradingbot.monitor.data.model.Signal
import com.tradingbot.monitor.data.model.Trade
import com.tradingbot.monitor.data.remote.ConnectionState
import com.tradingbot.monitor.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trading Bot Monitor") },
                actions = {
                    ConnectionBadge(state.connectionState)
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            BotControlRow(
                botEnabled = state.botEnabled,
                onToggle = { viewModel.toggleBot() }
            )

            Spacer(Modifier.height(16.dp))
            Text("Señales en vivo", style = MaterialTheme.typography.titleMedium)
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.signals) { signal -> SignalRow(signal) }
            }

            Spacer(Modifier.height(16.dp))
            Text("Operaciones", style = MaterialTheme.typography.titleMedium)
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.trades) { trade ->
                    TradeRow(trade, onClose = { viewModel.closeTrade(trade.id) })
                }
            }

            state.errorMessage?.let {
                Text(
                    "Error: $it",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ConnectionBadge(state: ConnectionState) {
    val (label, color) = when (state) {
        ConnectionState.CONNECTED -> "En vivo" to Color(0xFF2E7D32)
        ConnectionState.CONNECTING -> "Conectando..." to Color(0xFFF9A825)
        ConnectionState.DISCONNECTED -> "Sin conexión" to Color(0xFFC62828)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .padding(end = 4.dp)
        )
        Text(label, color = color, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun BotControlRow(botEnabled: Boolean, onToggle: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(if (botEnabled) "Bot activo" else "Bot en pausa")
        Switch(checked = botEnabled, onCheckedChange = { onToggle() })
    }
}

@Composable
fun SignalRow(signal: Signal) {
    ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text("${signal.symbol} · ${signal.direction} · ${signal.exchange}",
                style = MaterialTheme.typography.titleSmall)
            Text("Precio: ${signal.price}")
            Text("Confluencia: ${signal.confluence.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun TradeRow(trade: Trade, onClose: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text("${trade.symbol} · ${trade.side} · ${trade.status}",
                style = MaterialTheme.typography.titleSmall)
            Text("Entrada: ${trade.entryPrice}  Cant.: ${trade.quantity}")
            Text("SL: ${trade.stopLoss ?: "-"}  TP: ${trade.takeProfit ?: "-"}")
            trade.pnl?.let { Text("PnL: $it") }
            if (trade.status == "OPEN") {
                TextButton(onClick = onClose) { Text("Cerrar operación") }
            }
        }
    }
}
