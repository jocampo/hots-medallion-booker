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
        val anotherUser = User(2, "TestUser2")

        val rooms = roomKeeperService.getRooms()
        assertThat(rooms).isEmpty()
        val roomCode = roomKeeperService.bookRandomRoom(user)
        assertThat(rooms.size).isEqualTo(1)

        assertThatExceptionOfType(Exception::class.java).isThrownBy {
            roomKeeperService.vacateRoom("invalidRoomCode", user)
        }
        assertThatExceptionOfType(Exception::class.java).isThrownBy {
            roomKeeperService.vacateRoom(roomCode, anotherUser)
        }

        roomKeeperService.joinExistingRoom(roomCode, anotherUser)
        assertThat(rooms[roomCode]?.users?.size).isEqualTo(2)
        // User ownership was handed over to anotherUser
        assertThat(roomKeeperService.vacateRoom(roomCode, user)).isEqualTo(anotherUser.id)
        assertThat(rooms.size).isEqualTo(1)
        assertThat(rooms[roomCode]?.users?.size).isEqualTo(1)

        // Vacate the last user
        assertThat(roomKeeperService.vacateRoom(roomCode, anotherUser)).isNull()
        assertThat(rooms).isEmpty()
    }

    @Test
    fun joinExistingRoom() {
        val user = User(1, "TestUser")
        val anotherUser = User(2, "TestUser2")

        val rooms = roomKeeperService.getRooms()
        val roomCode = roomKeeperService.bookRandomRoom(user)

        assertThat(roomKeeperService.joinExistingRoom(roomCode, anotherUser)).isTrue()
        assertThatExceptionOfType(Exception::class.java).isThrownBy {
            roomKeeperService.joinExistingRoom("invalidRoomCode", anotherUser)
        }

        val yetAnotherUser = User(3, "TestUser3")
        assertThatExceptionOfType(Exception::class.java).isThrownBy {
            roomKeeperService.joinExistingRoom("invalidRoomCode", yetAnotherUser)
        }
        assertThat(roomKeeperService.joinExistingRoom(roomCode, yetAnotherUser)).isTrue()

        val user4 = User(4, "TestUser4")
        val user5 = User(5, "TestUser5")
        assertThat(roomKeeperService.joinExistingRoom(roomCode, user4)).isTrue()
        assertThat(roomKeeperService.joinExistingRoom(roomCode, user5)).isTrue()

        val user6 = User(6, "TestUser6")
        assertThat(roomKeeperService.joinExistingRoom(roomCode, user6)).isFalse()
        assertThat(rooms[roomCode]?.users?.size).isEqualTo(5)

    }
}