package com.jocampo.heroes.medallion.booker.config

import com.jocampo.heroes.medallion.booker.services.WebSocketRPCService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig : WebSocketConfigurer {
    private val logger: Logger = LoggerFactory.getLogger(WebSocketConfig::class.java)

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        val wsPath = "/rooms"
        logger.info("Registering ws handler for $wsPath")
        registry.addHandler(WebSocketRPCService(), wsPath).withSockJS()
    }
}