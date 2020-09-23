package com.jocampo.heroes.medallion.booker.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.jocampo.heroes.medallion.booker.entities.Hero
import io.github.rybalkinsd.kohttp.ext.httpGet
import io.github.rybalkinsd.kohttp.jackson.ext.toType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class HotSAPIClient(
        private val objectMapper: ObjectMapper
) {
    private final val HOTS_API_URL_BASE = "https://hotsapi.net/api/v1"
    private val logger: Logger = LoggerFactory.getLogger(HotSAPIClient::class.java)

    fun fetchAllHeroes(): List<Hero> {
        logger.debug("Processing request to fetch all heroes")
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