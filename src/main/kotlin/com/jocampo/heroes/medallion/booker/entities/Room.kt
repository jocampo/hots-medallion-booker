package com.jocampo.heroes.medallion.booker.entities

data class Room(
        val code: String,
        val users: MutableList<User>,
        var ownerId: Long
)