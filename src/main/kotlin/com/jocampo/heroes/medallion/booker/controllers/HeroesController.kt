package com.jocampo.heroes.medallion.booker.controllers

import com.jocampo.heroes.medallion.booker.entities.Hero
import com.jocampo.heroes.medallion.booker.services.HotSAPIClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URL

@RestController
class HeroesController(
        private val hotSAPIClient: HotSAPIClient
) {

    @GetMapping("/heroes")
    fun getAll(): List<Hero> {
        hotSAPIClient.fetchAllHeroes()
        return listOf<Hero>(Hero("a", "a", "a", "a", "asd"))
    }
}