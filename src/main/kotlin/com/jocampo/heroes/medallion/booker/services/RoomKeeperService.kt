package com.jocampo.heroes.medallion.booker.services

import com.jocampo.heroes.medallion.booker.entities.Room
import com.jocampo.heroes.medallion.booker.entities.User
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

    private var adjectives: List<String> = Files.readAllLines(Paths.get(adjectivesFile.file.path))
    private var nouns: List<String> = Files.readAllLines(Paths.get(nounsFile.file.path))
    private val rooms = ConcurrentHashMap<String, Room>()

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
    fun bookRoom(user: User): String {
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

        // The user belongs to the user
        room.users.removeIf { it.id == user.id }
        return if (room.users.isEmpty()) {
            // Free up the room, as it's empty now
            releaseRoom(code)
            null
        } else {
            // Room isn't empty. Give ownership to another user in the room
            room.ownerId = room.users.random().id
            room.ownerId
        }
    }
}