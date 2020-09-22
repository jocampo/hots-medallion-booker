package com.jocampo.heroes.medallion.booker.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.jocampo.heroes.medallion.booker.entities.Hero
import io.github.rybalkinsd.kohttp.ext.httpGet
import io.github.rybalkinsd.kohttp.jackson.ext.toType
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class HotSAPIClient(
        private val objectMapper: ObjectMapper
) {
    private val HOTS_API_URL_BASE = "https://hotsapi.net/api/v1"

    fun fetchAllHeroes(): List<Hero> {
        logger.debug { "Processing request to fetch heroes from the HotS API" }
        "$HOTS_API_URL_BASE/${HotSAPIMethods.HEROES.path}"
                .httpGet()
                .use {
                    return it.toType(objectMapper)!!
                }
    }
}

enum class HotSAPIMethods(val path: String) {
    HEROES("heroes")
}