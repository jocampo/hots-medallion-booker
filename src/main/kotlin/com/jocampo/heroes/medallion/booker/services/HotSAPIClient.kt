package com.jocampo.heroes.medallion.booker.services

import com.jocampo.heroes.medallion.booker.entities.Hero
import io.github.rybalkinsd.kohttp.ext.httpGet
import mu.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class HotSAPIClient {
    private val HOTS_API_URL_BASE = "hotsapi.net/api/v1"

    fun fetchAllHeroes() {
        val request = HOTS_API_URL_BASE + HotSAPIMethods.HEROES.path
        val response = request.httpGet()
        logger.debug { response.toString() }
    }
}

enum class HotSAPIMethods(val path: String) {
    HEROES("heroes")
}