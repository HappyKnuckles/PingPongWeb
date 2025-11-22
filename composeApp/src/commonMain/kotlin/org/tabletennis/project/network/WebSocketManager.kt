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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

@Serializable
data class CollisionEvent(
    val x: Float,
    val y: Float,
    val v: Float,
    @SerialName("goal_x") val goalX: Float? = null)

@Serializable
data class CoordinatesEvent(
    val x: Float,
    val y: Float,
    val v: Float
    // @SerialName("goal_x") val goalX: Float? = null
)

@Serializable
data class WebSocketMessage(
    val type: String,
    val data: CollisionEvent? = null
)

class WebSocketManager {

    private val serverUrl = "131.159.222.93"
    private val serverPort = 3000

    private val _bothPlayersConnected = MutableStateFlow(false)
    val bothPlayersConnected: StateFlow<Boolean> = _bothPlayersConnected

    private val _collisionEvent = MutableStateFlow<CollisionEvent?>(null)
    val collisionEvent: StateFlow<CollisionEvent?> = _collisionEvent

    private val _coordinatesEvent = MutableStateFlow<CoordinatesEvent?>(null)
    val coordinatesEvent: StateFlow<CoordinatesEvent?> = _coordinatesEvent

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
                            println(message)
                            if (message == "start") {
                                _bothPlayersConnected.value = true
                            } else if (message.contains("collision")) {
                                val rootObject = json.parseToJsonElement(message).jsonObject

                                val dataObject = rootObject["data"]

                                if (dataObject != null) {
                                    val event = json.decodeFromJsonElement<CollisionEvent>(dataObject)

                                    _collisionEvent.value = event
                                    println("Parsed Collision: $event")
                            }} else if (message.contains("coordinates")) {
                                val rootObject = json.parseToJsonElement(message).jsonObject

                                val dataObject = rootObject["data"]

                                if (dataObject != null) {
                                    val event = json.decodeFromJsonElement<CoordinatesEvent>(dataObject)

                                    _coordinatesEvent.value = event
                                    println("Parsed Coordinates: $event")
                            }}
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
