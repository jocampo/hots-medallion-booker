package com.jocampo.heroes.medallion.booker.utils

import com.jocampo.heroes.medallion.booker.entities.Hero
import com.jocampo.heroes.medallion.booker.services.RoomKeeperService

class TestUtils {
    companion object {
        fun generateDummyHero(name: String): Hero {
            return Hero(
                    name,
                    "some_short_name",
                    "Support",
                    "Melee",
                    mapOf("92x93" to "http://s3.hotsapi.net/img/heroes/92x93/abathur.png"
                    )
            )
        }
    }
}