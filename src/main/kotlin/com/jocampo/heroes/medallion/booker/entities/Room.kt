package com.jocampo.heroes.medallion.booker.entities

data class Room(
        val code: String,
        val users: MutableList<User>,
        var ownerId: Long,
        val redTeam: MutableList<Hero>,
        val blueTeam: MutableList<Hero>
) {
    companion object {
        val MAX_HEROES_PER_TEAM = 5
    }
    init {
        require(ownerId > 0) { "Field id must be greater than zero" }
        require(code.isNotBlank()) { "Field code name can't be blank" }
        require(users.isNotEmpty()) { "Room must have at least 1 user" }
        require(redTeam.size <= MAX_HEROES_PER_TEAM) { "Red team can only have $MAX_HEROES_PER_TEAM heroes at a time" }
        require(blueTeam.size <= MAX_HEROES_PER_TEAM) { "Blue team can only have $MAX_HEROES_PER_TEAM heroes at a time" }
    }
}

enum class Teams(val side: String) {
    BLUE("blue"),
    RED("red")
}