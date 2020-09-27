package com.jocampo.heroes.medallion.booker.entities

// TODO: Is there a better/less verbose way of ensuring non-empty strings?
data class Message(val msgType: String, val data: Any) {
    init {
        require(msgType.isNotBlank()) { "Field msgType can't be blank" }
    }
}

// Request/response types for WebSocket events to keep some form of structure in the "any" data field
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
data class GenericHeroRequest(val user: User, val roomCode: String, val hero: Hero, val team: String) {
    init {
        require(roomCode.isNotBlank()) { "Field roomCode name can't be blank" }
        require(team.isNotBlank()) { "Field team name can't be blank" }
    }
}

data class UsersChangedRoomResponse(val user: User, val room: Room)
data class ErrorResponse(val code: String?, val message: String?)