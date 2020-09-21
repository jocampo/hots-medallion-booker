package com.jocampo.heroes.medallion.booker.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jocampo.heroes.medallion.booker.entities.Hero
import io.github.rybalkinsd.kohttp.ext.httpGet
import io.github.rybalkinsd.kohttp.jackson.ext.toJson
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class HotSAPIClient(
        private val objectMapper: ObjectMapper
) {
    private val HOTS_API_URL_BASE = "https://hotsapi.net/api/v1"

    fun fetchAllHeroes() {
        logger.debug { "Processing request to fetch heroes from the HotS API" }
        "$HOTS_API_URL_BASE/${HotSAPIMethods.HEROES.path}"
                .httpGet()
                .use {
                    val a = it.toJson()
                    val b = "a"
                }
        jacksonObjectMapper()
    }
}

enum class HotSAPIMethods(val path: String) {
    HEROES("heroes")
}