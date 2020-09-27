package com.jocampo.heroes.medallion.booker.entities

// TODO: Is there a better/less verbose way of ensuring non-empty strings?
data class Message(val msgType: String, val data: Any) {
    init {
        require(msgType.isNotBlank()) { "Field msgType can't be blank" }
    }
}

// Request types for WebSocket events
// Some of these requests are more like responses, but at the same time they are a request from one
// of the sides (client vs server) in the websocket communication, so we're just going to leave them like this for now
data class BookRoomRequest(val userName: String) {
    init {
        require(userName.isNotBlank()) { "Field userName name can't be blank" }
    }
}
data class JoinRoomRequest(val roomCode: String, val userName: String) {
    init {
        require(roomCode.isNotBlank()) { "Field roomCode name can't be blank" }
        require(userName.isNotBlank()) { "Field userName name can't be blank" }
    }
}
data class UserJoinedRoomRequest(val user: User, val room: Room)
data class UserLeftRoomRequest(val user: User, val room: Room)
data class GenericHeroRequest(val user: User, val roomCode: String, val hero: Hero, val team: String)

data class ErrorResponse(val code: String?, val message: String?)