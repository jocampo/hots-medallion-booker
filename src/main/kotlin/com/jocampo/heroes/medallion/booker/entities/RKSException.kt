package com.jocampo.heroes.medallion.booker.entities


data class RKSException(val rks_error_code: String, val error_message: String): Throwable()

enum class RKSErrorCodes(val code: String) {
    ROOM_DOES_NOT_EXIST("0100"),
    ROOM_IS_FULL("0101"),
    USER_ALREADY_IN_ROOM("0102"),
    USER_NOT_IN_ROOM("0103"),
    TEAM_IS_FULL("0104"),
    HERO_ALREADY_IN_TEAM("0105"),
    HERO_NOT_IN_TEAM("0106"),
    MEDALLION_ALREADY_ON_CD("0107"),
    MEDALLION_NOT_ON_CD("0108")
}