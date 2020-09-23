package com.jocampo.heroes.medallion.booker.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths

@Component
class RoomKeeperService(
        @Value("classpath:words/adjectives.txt") private var adjectivesFile: Resource,
        @Value("classpath:words/nouns.txt") private var nounsFile: Resource
) {
    init {
        val adjectives: List<String> = Files.readAllLines(Paths.get(adjectivesFile.file.path))
        val nouns: List<String> = Files.readAllLines(Paths.get(nounsFile.file.path))

        // TODO handle randomized creation of <adjectiveNoun###>. ie: pretentiousTadpole215
        // this should be used for room code provision
    }
}