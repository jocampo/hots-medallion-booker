package com.jocampo.heroes.medallion.booker.controllers

import com.jocampo.heroes.medallion.booker.entities.Hero
import com.jocampo.heroes.medallion.booker.services.HotSAPIClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HeroesController(
        private val hotSAPIClient: HotSAPIClient
) {
    @GetMapping("/heroes")
    fun getAll(): List<Hero> {
        return hotSAPIClient.fetchAllHeroes()
    }
}