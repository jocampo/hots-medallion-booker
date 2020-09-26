package com.jocampo.heroes.medallion.booker.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.jocampo.heroes.medallion.booker.entities.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.atomic.AtomicLong


class WebSocketRPCService(
        private val objectMapper: ObjectMapper,
        private val roomKeeperService: RoomKeeperService
) : TextWebSocketHandler() {

    private val logger: Logger = LoggerFactory.getLogger(WebSocketRPCService::class.java)
    private val sessionList = HashMap<WebSocketSession, User>()

    var uids = AtomicLong(0)

    @Throws(Exception::class)
    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.info("Started connection with $session.id")
    }

    @Throws(Exception::class)
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        logger.info("Closing connection with $session.id")
        sessionList -= session
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val json = objectMapper.readTree(message.payload)
        try {
            when (json.get("type").asText()) {
                WebSocketEventTypes.BOOK_ROOM.eventType -> {
                    if (sessionList.containsKey(session)) {
                        logger.error("User has already booked a room. Please leave the room before trying this" +
                                "operation again")
                        emitGenericError(session)
                        return
                    }
                    val request = objectMapper.readValue<BookRoomRequest>(json.get("data").toString())
                    val user = User(uids.getAndIncrement(), request.userName)
                    val roomCode = roomKeeperService.bookRandomRoom(user)

                    // Add the user to the session map
                    sessionList[session] = user
                    // The room was JUST booked, it must exist
                    val room = roomKeeperService.getRooms()[roomCode]!!
                    emit(session, Message(
                            WebSocketEventTypes.USER_JOINED_ROOM.eventType,
                            UserJoinedRoomRequest(user, room)
                    ))
                }
                WebSocketEventTypes.JOIN_ROOM.eventType -> {
                    if (sessionList.containsKey(session)) {
                        logger.error("User has already joined a room. Please leave the room before trying this" +
                                "operation again")
                        emitGenericError(session)
                        return
                    }
                    val request = objectMapper.readValue<JoinRoomRequest>(json.get("data").toString())
                    val user = User(uids.getAndIncrement(), request.userName)
                    if (!roomKeeperService.joinExistingRoom(request.roomCode, user)) {
                        // User wasn't able to join the room
                        emit(session, Message(WebSocketEventTypes.ERROR_OCCURRED.eventType,
                                "Room with code ${request.roomCode} is full. " +
                                        "User ${user.id} was denied entrance"))
                        return
                    }
                    sessionList[session] = user
                    // The user just joined the room, it must exist
                    val room = roomKeeperService.getRooms()[request.roomCode]!!
                    broadcastToRoom(
                            room,
                            Message(WebSocketEventTypes.USER_JOINED_ROOM.eventType, UserJoinedRoomRequest(user, room))
                    )
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
        } catch (e: Exception) {
            logger.error("Found an issue while processing request from user $session", e)
            // Inform the user about the error
            emitGenericError(session)
            // TODO: Should we inform the user about WHAT went wrong in a more actionable way?
        }
    }

    private fun emitGenericError(session: WebSocketSession) =
            emit(session, Message(WebSocketEventTypes.ERROR_OCCURRED.eventType,
                    "Found an issue while processing your request, please try again."))

    private fun emit(session: WebSocketSession, msg: Message) =
            session.sendMessage(TextMessage(objectMapper.writeValueAsString(msg)))

    private fun broadcast(msg: Message) =
            sessionList.forEach { emit(it.key, msg) }

    private fun broadcastToOthers(me: WebSocketSession, msg: Message) =
            sessionList.filterNot { it.key == me }.forEach { emit(it.key, msg) }

    private fun broadcastToRoom(room: Room, msg: Message) =
            room.users.forEach { emit(sessionList.filterValues { u -> u.id == it.id }.keys.first(), msg) }
}

enum class WebSocketEventTypes(val eventType: String) {
    BOOK_ROOM("bookRoom"),
    JOIN_ROOM("joinRoom"),
    LEAVE_ROOM("leaveRoom"),
    USER_JOINED_ROOM("userJoinedRoom"),
    USER_LEFT_ROOM("userLeftRoom"),
    ERROR_OCCURRED("errorOccurred")
}