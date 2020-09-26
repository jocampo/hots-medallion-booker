package com.jocampo.heroes.medallion.booker.entities


data class RKSException(val rks_error_code: String, val error_message: String): Throwable()

enum class RKSErrorCodes(val code: String) {
    ROOM_DOES_NOT_EXIST("0100"),
    ROOM_IS_FULL("0101"),
    USER_ALREADY_IN_ROOM("0102"),
    USER_NOT_IN_ROOM("0103")
}