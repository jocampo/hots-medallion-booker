package com.jocampo.heroes.medallion.booker.entities

data class Room(
        val code: String,
        val users: MutableList<User>,
        var ownerId: Long
) {
    init {
        require(ownerId > 0) { "Field id must be greater than zero" }
        require(code.isNotBlank()) { "Field code name can't be blank" }
        require(users.isNotEmpty()) { "Room must have at least 1 user" }
    }
}