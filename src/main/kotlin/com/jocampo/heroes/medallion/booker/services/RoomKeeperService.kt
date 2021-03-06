package com.jocampo.heroes.medallion.booker.services

import com.jocampo.heroes.medallion.booker.entities.*
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
    private val ongoingTimers: HashSet<String> = hashSetOf()

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
                rooms[code] = Room(code, mutableListOf(user), user.id, arrayListOf(), arrayListOf())
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
            throw RKSException(RKSErrorCodes.ROOM_DOES_NOT_EXIST.code, "The room $code doesn't exist")
        }
    }

    /**
     * The user gives up the room. If it's empty afterwards, then {releaseRoom} is invoked
     * to release the resource. TODO: fix docstring to reflect functionality
     *
     * @throws RKSException: - When the room code doesn't exist.
     * - When the user doesn't belong to the room.
     *
     * @return Returns an {Int} ONLY if the room ownership has been given to another user in the room.
     * That new owner User.id is returned.
     */
    fun vacateRoom(code: String, user: User): Long? {
        // TODO: test if contains(user) works properly
        if (!rooms.containsKey(code)) {
            throw RKSException(RKSErrorCodes.ROOM_DOES_NOT_EXIST.code, "The room $code doesn't exist")
        }
        // The room EXISTS
        val room = rooms[code]!!

        if (!room.users.map { it.id }.contains(user.id)) {
            throw RKSException(RKSErrorCodes.USER_NOT_IN_ROOM.code, "Attempted to leave a room the user doesn't belong" +
                    " to...")
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
     * @throws RKSException: - When the room code doesn't exist.
     * - When the user already belongs to the room.
     *
     * @return Boolean: True if they were able to join the room, False otherwise
     * A cap of 5 users per room is maintained.
     */
    fun joinExistingRoom(code: String, user: User): Boolean {
        if (!rooms.containsKey(code)) {
            throw RKSException(RKSErrorCodes.ROOM_DOES_NOT_EXIST.code, "The room $code doesn't exist")
        }
        // The room EXISTS
        val room = rooms[code]!!

        if (room.users.map { it.id }.contains(user.id)) {
            throw RKSException(RKSErrorCodes.USER_ALREADY_IN_ROOM.code, "Attempted to join a room the user already" +
                    "belongs to")
        }

        if (room.users.size == maxUsersPerRoom) {
            logger.info("Room with code $code is full. User ${user.id} was denied entrance")
            return false
        }
        room.users.add(user)
        return true
    }

    /**
     * Utility method to grab the right team hero collection (blue/red) from a room
     * @param room: room with the hero collections on each team
     * @param team: com.jocampo.heroes.medallion.booker.entities.Teams enum value
     *
     * @return the MutableList pointing towards the right collection
     */
    private fun getHeroCollectionPerSide(room: Room, team: String): MutableList<Hero> {
        return when (team) {
            Teams.BLUE.side -> {
                room.blueTeam
            }
            else -> {
                room.redTeam
            }
        }
    }

    fun addHeroToRoom(code: String, hero: Hero, team: String) {
        if (!rooms.containsKey(code)) {
            throw RKSException(RKSErrorCodes.ROOM_DOES_NOT_EXIST.code, "The room $code doesn't exist")
        }
        // Point towards the right team so as to prevent code duplication
        val teamCollection = getHeroCollectionPerSide(rooms[code]!!, team)

        if (teamCollection.size == Room.MAX_HEROES_PER_TEAM) {
            throw RKSException(RKSErrorCodes.TEAM_IS_FULL.code,
                    "That team already has ${Room.MAX_HEROES_PER_TEAM} heroes and is full")
        }
        if (teamCollection.any { it.name == hero.name }) {
            throw RKSException(RKSErrorCodes.HERO_ALREADY_IN_TEAM.code,
                    "That team already has ${hero.name} in it.")
        }

        teamCollection.add(hero)
    }

    fun removeHeroFromRoom(code: String, hero: Hero, team: String) {
        if (!rooms.containsKey(code)) {
            throw RKSException(RKSErrorCodes.ROOM_DOES_NOT_EXIST.code, "The room $code doesn't exist")
        }
        // Point towards the right team so as to prevent code duplication
        val teamCollection = getHeroCollectionPerSide(rooms[code]!!, team)

        if (teamCollection.none { it.name == hero.name }) {
            throw RKSException(RKSErrorCodes.HERO_NOT_IN_TEAM.code,
                    "That team doesn't have ${hero.name} in it.")
        }

        teamCollection.remove(hero)
    }

    /**
     * Validates the roomCode and gets the matching Room
     */
    fun getRoomSafe(roomCode: String): Room {
        if (!rooms.containsKey(roomCode)) {
            throw RKSException(RKSErrorCodes.ROOM_DOES_NOT_EXIST.code, "The room $roomCode doesn't exist")
        }
        return rooms[roomCode]!!
    }

    fun calculateMedallionCode(roomCode: String, hero: Hero, team: String) = "$roomCode/${hero.name}/$team"

    fun checkOngoingMedallions(identifier: String) = ongoingTimers.contains(identifier)

    fun registerMedallionTimer(medallionIdentifier: String) {
        if (ongoingTimers.contains(medallionIdentifier)) {
            logger.error("Medallion with identifier $medallionIdentifier is already on CD")
            throw RKSException(RKSErrorCodes.MEDALLION_ALREADY_ON_CD.code, "That hero's medallion is already on CD")
        }
        ongoingTimers.add(medallionIdentifier)
    }

    fun clearMedallionTimer(medallionIdentifier: String) {
        if (!ongoingTimers.contains(medallionIdentifier)) {
            logger.error("Medallion with identifier $medallionIdentifier doesn't exist and was attempted to be removed")
            throw RKSException(RKSErrorCodes.MEDALLION_NOT_ON_CD.code, "That hero's medallion is not CD")
        }
        ongoingTimers -= medallionIdentifier
    }
}