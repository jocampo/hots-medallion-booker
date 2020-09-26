package com.jocampo.heroes.medallion.booker.entities

// TODO: Is there a better/less verbose way of ensuring non-empty strings?
data class Message(val msgType: String, val data: Any) {
    init {
        require(msgType.isNotBlank()) { "Field msgType can't be blank" }
    }
}

// Request types for WebSocket events
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
data class LeaveRoomRequest(val roomCode: String, val user: User) {
    init {
        require(roomCode.isNotBlank()) { "Field userName name can't be blank" }
    }
}
data class UserJoinedRoomRequest(val user: User, val room: Room)
data class UserLeftRoomRequest(val roomCode: String, val userName: String) {
    init {
        require(roomCode.isNotBlank()) { "Field userName name can't be blank" }
        require(userName.isNotBlank()) { "Field userName name can't be blank" }
    }
}