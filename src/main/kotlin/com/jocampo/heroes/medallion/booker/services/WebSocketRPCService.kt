package com.jocampo.heroes.medallion.booker.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jocampo.heroes.medallion.booker.entities.Message
import com.jocampo.heroes.medallion.booker.entities.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.socket.*
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.atomic.AtomicLong

class WebSocketRPCService : TextWebSocketHandler() {
    private val logger: Logger = LoggerFactory.getLogger(WebSocketRPCService::class.java)

    val sessionList = HashMap<WebSocketSession, User>()
    var uids = AtomicLong(0)

    @Throws(Exception::class)
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        logger.info("Closing connection with $session.id")
        sessionList -= session
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val json = ObjectMapper().readTree(message.payload)
        when (json.get("type").asText()) {
            "joinRoom" -> {
                // TODO: Introduce concept of rooms
                val user = User(uids.getAndIncrement(), json.get("data").asText())
                sessionList[session] = user
                logger.info("User joined room! $session.id")
                // tell this user about all other users
                emit(session, Message("listAllUsers", sessionList.values))
                // tell all other users, about this user
                broadcastToOthers(session, Message("joinRoom", user))
            }
            "leaveRoom" -> {
                logger.info("User left room! $session.id")
                val user = sessionList[session] ?: throw Exception("Caller is expected to have a valid session")

                broadcastToOthers(session, Message("leaveRoom", user))
            }
            "addHero" -> {

            }
            "listAllHeroes" -> {

            }
        }
    }

    fun emit(session: WebSocketSession, msg: Message) =
            session.sendMessage(TextMessage(jacksonObjectMapper().writeValueAsString(msg)))

    fun broadcast(msg: Message) =
            sessionList.forEach { emit(it.key, msg) }

    fun broadcastToOthers(me: WebSocketSession, msg: Message) =
            sessionList.filterNot { it.key == me }.forEach { emit(it.key, msg) }
}