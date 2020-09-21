package com.jocampo.heroes.medallion.booker.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jocampo.heroes.medallion.booker.controllers.HeroesController
import com.jocampo.heroes.medallion.booker.services.HotSAPIClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BeanConfig {
    @Bean
    fun objectMapper(): ObjectMapper = jacksonObjectMapper()
}