package com.jocampo.heroes.medallion.booker.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths
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
    private val roomCodes = mutableListOf<String>()

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
    fun bookRoom(): String {
        var attempts = 0
        while (true) {
            if (attempts > maxAttempts) {
                throw Exception("Unable to find available room codes...")
            }

            val code = generateRoomCode()
            if (!roomCodes.contains(code)) {
                roomCodes.add(code)
                return code
            }
            attempts++
        }
    }

    /**
     * Frees up the reserved room given a room code
     */
    fun vacateRoom(code: String) {
        if (roomCodes.contains(code)) {
            roomCodes.remove(code)
        } else {
            throw Exception("Attempted to remove a room code that doesn't exist...")
        }
    }
}