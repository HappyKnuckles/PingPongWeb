package org.tabletennis.project.game

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LobbyState {
    private val _lobbyCode = MutableStateFlow("")
    val lobbyCode: StateFlow<String> = _lobbyCode
    
    private val _playerNumber = MutableStateFlow(0)
    val playerNumber: StateFlow<Int> = _playerNumber
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected
    
    fun generateLobbyCode(): String {
        val code = (10000..99999).random().toString()
        _lobbyCode.value = code
        return code
    }
    
    fun setLobbyCode(code: String) {
        _lobbyCode.value = code
    }
    
    fun setPlayerNumber(number: Int) {
        _playerNumber.value = number
    }
    
    fun setConnected(connected: Boolean) {
        _isConnected.value = connected
    }
}
