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

    // Probably want to use actual UUIDS in the future
    var uids = AtomicLong(1)

    @Throws(Exception::class)
    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.info("Started connection with $session.id")
    }

    @Throws(Exception::class)
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        logger.info("Closing connection with $session.id, looking for rooms to clean-up")
        // If user is breaking the session WITHOUT first leaving the room, we need to force him
        // out of it.
        handleUserLeavingRoom(session, true)
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
                WebSocketEventTypes.LEAVE_ROOM.eventType -> {
                    if (!sessionList.containsKey(session)) {
                        logger.error("User doesn't have an existing session, which is required by this event type")
                        emitGenericError(session)
                        return
                    }
                    handleUserLeavingRoom(session, false)
                    // In the future, we might not want to tie leaving a room with having your session removed
                    sessionList -= session
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

    private fun broadcastToRoom(room: Room, msg: Message) =
            room.users.forEach { emit(sessionList.filterValues { u -> u.id == it.id }.keys.first(), msg) }

    /**
     * Forces the user to leave the room and notifies the remaining users (if any). And also potentially notifies the
     * change of ownership through the same event. Since the user ALREADY closed the connection, there's no need to notify him.
     *
     * @param session: existing session
     * @param isConnectionClosed: if true, it means that the user leaving the room has already closed the connection
     * Otherwise, the session is still valid and we need to notify the user that he has successfully left the room.
     */
    private fun handleUserLeavingRoom(session: WebSocketSession, isConnectionClosed: Boolean) {
        val user = sessionList[session]!!
        roomKeeperService.getRooms().values.find { it.users.any { u -> u.id == user.id } }?.let {
            when (roomKeeperService.vacateRoom(it.code, user)) {
                null -> {
                    // The room was deleted... do nothing, since we don't need to notify anyone
                    logger.info("Upon $user closing connection without leaving the room, the room" +
                            "$it has been released...")
                }
                else -> {
                    // 2 scenarios, the iterator has already changed, so the call is the same:
                    // Ownership wasn't handed off. Notify remaining users of who left
                    // Ownership change happened
                    broadcastToRoom(
                            it,
                            Message(WebSocketEventTypes.USER_LEFT_ROOM.eventType, UserLeftRoomRequest(user, it))
                    )
                    // Notify the user only if he hasn't left yet (closed the websocket connection)
                    if (!isConnectionClosed) {
                        emit(session, Message(WebSocketEventTypes.USER_LEFT_ROOM.eventType, UserLeftRoomRequest(user, it)))
                    }
                }
            }
        }
    }

    private fun broadcast(msg: Message) =
            sessionList.forEach { emit(it.key, msg) }

    private fun broadcastToOthers(me: WebSocketSession, msg: Message) =
            sessionList.filterNot { it.key == me }.forEach { emit(it.key, msg) }
}

// Can we eventually unite request types with event types? 🤔
enum class WebSocketEventTypes(val eventType: String) {
    BOOK_ROOM("bookRoom"),
    JOIN_ROOM("joinRoom"),
    LEAVE_ROOM("leaveRoom"),
    USER_JOINED_ROOM("userJoinedRoom"),
    USER_LEFT_ROOM("userLeftRoom"),
    ERROR_OCCURRED("errorOccurred"),
    ADD_HERO("addHero"),
    REMOVE_HERO("removeHero"),
    HERO_ADDED("heroAdded"),
    HERO_REMOVED("heroRemoved"),
    MEDALLION_USED("medallionUsed"),
    MEDALLION_OFF_CD("medallionOffCD")
}