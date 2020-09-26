package com.jocampo.heroes.medallion.booker.entities

data class User(val id: Long, val name: String) {
    init {
        require(id > 0) { "Field id must be greater than zero" }
        require(name.isNotBlank()) { "Field name name can't be blank" }
    }
}