package com.jocampo.heroes.medallion.booker.config

import com.jocampo.heroes.medallion.booker.services.WebSocketRPCService
import mu.KotlinLogging
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

private val logger = KotlinLogging.logger {}

@Configuration
@EnableWebSocket
class WebSocketConfig : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        val wsPath = "/rooms"
        logger.info { "Registering ws handler for $wsPath" }
        registry.addHandler(WebSocketRPCService(), wsPath).withSockJS()
    }
}