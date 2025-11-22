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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

@Serializable
data class CoordinatesEvent(
    val x: Float,
    val y: Float,
    val v: Float
)

@Serializable
data class ScoreEvent(
    val score: List<Int>, // Receives [playerScore, opponentScore]
    val message: String
)

class WebSocketManager {

    private val serverUrl = "131.159.222.93"
    private val serverPort = 3000

    private val _bothPlayersConnected = MutableStateFlow(false)
    val bothPlayersConnected: StateFlow<Boolean> = _bothPlayersConnected

    private val _coordinatesEvent = MutableStateFlow<CoordinatesEvent?>(null)
    val coordinatesEvent: StateFlow<CoordinatesEvent?> = _coordinatesEvent

    private val _scoreEvent = MutableStateFlow<ScoreEvent?>(null)
    val scoreEvent: StateFlow<ScoreEvent?> = _scoreEvent

    private val client = HttpClient {
        install(WebSockets)
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
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
                            println("Received: $message")

                            if (message == "start") {
                                _bothPlayersConnected.value = true
                            }
                            else if (message.contains("coordinates")) {
                                try {
                                    val rootObject = json.parseToJsonElement(message).jsonObject
                                    val dataObject = rootObject["data"]
                                    if (dataObject != null) {
                                        val event = json.decodeFromJsonElement<CoordinatesEvent>(dataObject)
                                        _coordinatesEvent.value = event
                                    }
                                } catch (e: Exception) {
                                    println("Error parsing coordinates: ${e.message}")
                                }
                            }
                            else if (message.contains("score")) {
                                try {
                                    val event = json.decodeFromString<ScoreEvent>(message)
                                    _scoreEvent.value = event
                                    println("Parsed Score: $event")
                                } catch (e: Exception) {
                                    println("Error parsing score: ${e.message}")
                                }
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