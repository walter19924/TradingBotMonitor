# Trading Bot Monitor (Android)

App Kotlin + Jetpack Compose que sirve como **panel de monitoreo y control** para tu bot
de trading (Data Layer + Logic Engine + Execution Engine + Risk Layer). La app NO ejecuta
órdenes por sí misma: se conecta al backend (FastAPI/Node) que ya tienes diseñado.

## 1. Requisitos
- Android Studio Koala o más reciente.
- JDK 17.
- Tu backend expuesto por HTTPS/WSS (no funciona con `http://` plano por el
  `usesCleartextTraffic="false"` del manifest — en desarrollo local puedes usar ngrok/cloudflared).

## 2. Configurar la conexión al backend
Edita `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "BASE_HTTP_URL", "\"https://TU_URL_AQUI/\"")
buildConfigField("String", "BASE_WS_URL", "\"wss://TU_URL_AQUI/ws/stream\"")
```

## 3. Contrato que debe exponer tu Execution Engine (FastAPI/Node)

### REST
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/trades` | Historial/estado de operaciones |
| GET | `/api/risk-config` | Configuración actual de riesgo |
| PUT | `/api/risk-config` | Actualizar SL/TP/tamaño de posición/max trades |
| POST | `/api/bot/pause` | Pausar el bot |
| POST | `/api/bot/resume` | Reanudar el bot |
| POST | `/api/trades/{id}/close` | Cerrar una operación manualmente |

### WebSocket (`/ws/stream`)
Cada mensaje es un JSON con esta forma (ver `WsEvent` en el código):
```json
{ "type": "signal", "signal": { "id": "...", "symbol": "BTC/USDT", "exchange": "Binance",
  "direction": "BUY", "confluence": ["RSI<30","MACD_CROSS_UP"], "price": 61234.5,
  "timestamp": 1734900000 } }
```
```json
{ "type": "trade_update", "trade": { "id": "...", "symbol": "BTC/USDT", "side": "BUY",
  "entryPrice": 61234.5, "quantity": 0.01, "stopLoss": 60000, "takeProfit": 63000,
  "status": "OPEN", "pnl": null, "timestamp": 1734900000 } }
```

## 4. Notificaciones push (opcional, junto al bot de Telegram)
1. Crea un proyecto en Firebase, añade la app Android (`com.tradingbot.monitor`) y descarga
   `google-services.json` dentro de `app/`.
2. Tu backend, al ejecutar una orden, llama al **Firebase Admin SDK** para enviar el push
   al token que la app registra en `TradingFcmService.onNewToken()`.

## 5. Seguridad — importante
- Las **API keys de los exchanges nunca deben vivir en el teléfono**. Se quedan en el
  backend (variables de entorno / secret manager). La app solo habla con tu propio servidor.
- Añade autenticación real (JWT propio de tu backend) en `RetrofitClient.setAuthToken()`.
- Considera activar bloqueo biométrico (`androidx.biometric`) antes de mostrar el dashboard,
  dado que expone posiciones y PnL.

## 6. Publicación en Google Play
Las apps de trading/cripto tienen políticas específicas (declaraciones de riesgo, a veces
verificación adicional de cuenta de desarrollador). Revísalas antes de publicar.

## 7. Qué falta por construir (siguientes pasos sugeridos)
- Pantalla de configuración de riesgo (editar `RiskConfig` desde la UI).
- Gráfico de velas/equity con Vico (ya está la dependencia agregada).
- Autenticación (login) y almacenamiento seguro del token con `EncryptedSharedPreferences`.
- Bloqueo biométrico al abrir la app.
- Room para cachear trades offline.
