package org.tabletennis.project.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WebSocketManager {

    private val serverUrl = "131.159.222.93"
    private val serverPort = 3000

    private val _bothPlayersConnected = MutableStateFlow(false)
    val bothPlayersConnected: StateFlow<Boolean> = _bothPlayersConnected

    private val client = HttpClient {
        install(WebSockets)
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var connectionJob: Job? = null


    fun connect(hostToken: String) {
        disconnect()

        connectionJob = scope.launch {
            try {
                client.webSocket(
                    method = HttpMethod.Get,
                    host = serverUrl,
                    port = serverPort,
                    path = "/",
                    request = {
                        url.parameters.append("token", hostToken)
                    }
                ) {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val message = frame.readText()

                            if (message == "start") {
                                _bothPlayersConnected.value = true
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _bothPlayersConnected.value = false
            }
        }
    }

    fun disconnect() {
        connectionJob?.cancel()
        _bothPlayersConnected.value = false
    }
}