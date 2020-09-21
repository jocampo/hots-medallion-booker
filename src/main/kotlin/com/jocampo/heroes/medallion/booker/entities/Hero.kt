package com.jocampo.heroes.medallion.booker.entities

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.net.URL


/*
 * TODO: https://kotlinlang.org/docs/reference/kotlin-doc.html
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Hero (
        val name: String,
        val short_name: String,
        val role: String,
        val type: String,
        val icon_url: URL
)