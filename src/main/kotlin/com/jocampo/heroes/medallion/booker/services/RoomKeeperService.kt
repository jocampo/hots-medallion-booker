package com.jocampo.heroes.medallion.booker.services

import com.jocampo.heroes.medallion.booker.entities.Room
import com.jocampo.heroes.medallion.booker.entities.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

@Component
class RoomKeeperService(
        @Value("classpath:words/adjectives.txt") private var adjectivesFile: Resource,
        @Value("classpath:words/nouns.txt") private var nounsFile: Resource
) {
    private final val highestRoomNumber: Int = 9999
    private final val maxAttempts: Int = 250
    private final val maxUsersPerRoom: Int = 5

    private var adjectives: List<String> = Files.readAllLines(Paths.get(adjectivesFile.file.path))
    private var nouns: List<String> = Files.readAllLines(Paths.get(nounsFile.file.path))
    private val rooms = ConcurrentHashMap<String, Room>()
    private val logger: Logger = LoggerFactory.getLogger(RoomKeeperService::class.java)

    fun getRooms(): ConcurrentHashMap<String, Room> = rooms

    /**
     * Handles randomized creation of room codes with the following shape
     * <adjectiveNoun###>. ie: PretentiousTadpole215
     *
     * @return new room code
     */
    fun generateRoomCode(): String =
            adjectives.random().capitalize() +
            nouns.random().capitalize() +
            Random.nextInt(highestRoomNumber).toString()

    /**
     * Adds a room code to the reserved rooms collection.
     *
     * @throws Exception when an available room code could not be generated (due to too many conflicts)
     *
     * @return the newly booked room code
     */
    fun bookRandomRoom(user: User): String {
        var attempts = 0
        while (true) {
            if (attempts > maxAttempts) {
                throw Exception("Unable to find available room codes...")
            }

            val code = generateRoomCode()
            if (!rooms.containsKey(code)) {
                rooms[code] = Room(code, mutableListOf(user), user.id)
                return code
            }
            attempts++
        }
    }

    /**
     * Frees up the reserved room given a room code
     */
    fun releaseRoom(code: String) {
        if (rooms.containsKey(code)) {
            rooms.remove(code)
        } else {
            throw Exception("Attempted to remove a room code that doesn't exist...")
        }
    }

    /**
     * The user gives up the room. If it's empty afterwards, then {releaseRoom} is invoked
     * to release the resource. TODO: fix docstring to reflect functionality
     *
     * @throws Exception: - When the room code doesn't exist.
     * - When the user doesn't belong to the room.
     *
     * @return Returns an {Int} ONLY if the room ownership has been given to another user in the room.
     * That new owner User.id is returned.
     */
    fun vacateRoom(code: String, user: User): Long? {
        // TODO: test if contains(user) works properly
        if (!rooms.containsKey(code)) {
            throw Exception("Attempted to leave a room that doesn't exist...")
        }
        // The room EXISTS
        val room = rooms[code]!!

        if (!room.users.map { it.id }.contains(user.id)) {
            throw Exception("Attempted to leave a room the user doesn't belong to...")
        }

        // The user belongs to the room
        room.users.removeIf { it.id == user.id }
        return if (room.users.isEmpty()) {
            // Free up the room, as it's empty now
            releaseRoom(code)
            null
        } else {
            // Room isn't empty. Give ownership to another user in the room
            // but only if the user that's leaving was the owner
            if (room.ownerId == user.id) {
                room.ownerId = room.users.random().id
                room.ownerId
            } else {
                // No ownership change
                -1L
            }
        }
    }

    /**
     * A user joins an existing room they're not currently in
     *
     * @throws Exception: - When the room code doesn't exist.
     * - When the user already belongs to the room.
     *
     * @return Boolean: True if they were able to join the room, False otherwise
     * A cap of 5 users per room is maintained.
     */
    fun joinExistingRoom(code: String, user: User): Boolean {
        if (!rooms.containsKey(code)) {
            throw Exception("Attempted to leave a room that doesn't exist...")
        }
        // The room EXISTS
        val room = rooms[code]!!

        if (room.users.map { it.id }.contains(user.id)) {
            throw Exception("Attempted to join a room the user is already in...")
        }

        if (room.users.size == maxUsersPerRoom) {
            logger.info("Room with code $code is full. User ${user.id} was denied entrance")
            return false
        }
        room.users.add(user)
        return true
    }
}