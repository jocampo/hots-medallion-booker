package com.jocampo.heroes.medallion.booker.services

import com.jocampo.heroes.medallion.booker.entities.User
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest


@SpringBootTest
internal class RoomKeeperServiceTest {

    // TODO: Break these tests down so they are granular
    @Autowired
    private lateinit var roomKeeperService: RoomKeeperService

    @AfterEach
    fun tearDown() {
        roomKeeperService.getRooms().clear()
    }

    @Test
    fun contextLoads() {
        assertThat(roomKeeperService).isNotNull
    }

    @Test
    fun generateRoomCode() {
        val codeA = roomKeeperService.generateRoomCode()
        val codeB = roomKeeperService.generateRoomCode()
        assertThat(codeA).isInstanceOf(String::class.java)
        assertThat(codeA).isNotEqualTo(codeB)
    }

    @Test
    fun bookRandomRoom() {
        val user = User(1, "TestUser")
        val rooms = roomKeeperService.getRooms()
        assertThat(rooms).isEmpty()
        val roomCode = roomKeeperService.bookRandomRoom(user)
        assertThat(roomCode).isInstanceOf(String::class.java)
        assertThat(rooms).isNotEmpty
        assertThat(rooms[roomCode]?.ownerId).isEqualTo(user.id)
        assertThat(rooms[roomCode]?.users?.size).isEqualTo(1)
        assertThat(rooms[roomCode]?.users?.elementAt(0)).isEqualTo(user)
    }

    @Test
    fun releaseRoom() {
        val user = User(1, "TestUser")
        val rooms = roomKeeperService.getRooms()
        val roomCode = roomKeeperService.bookRandomRoom(user)
        roomKeeperService.releaseRoom(roomCode)
        assertThat(rooms).isEmpty()
        assertThatExceptionOfType(Exception::class.java).isThrownBy {
            roomKeeperService.releaseRoom("invalidRoomCode")
        }
    }

    @Test
    fun vacateRoom() {
        val user = User(1, "TestUser")
        val rooms = roomKeeperService.getRooms()
        val roomCode = roomKeeperService.bookRandomRoom(user)
    }
}