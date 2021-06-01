package no.nav.pam.adreg.migration.replication

import no.nav.pam.adreg.migration.annonse.RepositoryCounts
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.Month


@RestController
@RequestMapping("/replication")
class AnnonseReplicationController(
    val annonseReplicationService: AnnonseReplicationService
) {

    private val log = LoggerFactory.getLogger(AnnonseReplicationController::class.java)

    @PostMapping("updateAll", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun executeUpdates(): ResponseEntity<Map<String,*>> {
        var since = LocalDateTime.of(2013, Month.JUNE, 11, 0, 0)

        log.info("Execute update of all ads updated since ${since}")

        var next = annonseReplicationService.processUpdateBatchSince(since)
        while (next != null && next.isAfter(since)) {
            log.info("Next batch starts at ${next}")
            since = next
            next = annonseReplicationService.processUpdateBatchSince(since)
        }

        log.info("Stopped with latest updated at ${next}")

        return ResponseEntity.ok(mapOf("result" to "success", "latestUpdate" to next))
    }

    @PostMapping("delete", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun executeDeletes(): ResponseEntity<Map<String,*>> {

        log.info("Manual trigger of replication deletes processing received")

        val n = annonseReplicationService.processAllDeletes()

        return ResponseEntity.ok(mapOf("result" to "success", "deleteCount" to n))
    }

    /**
     * Returns some handy checksums from annonse repository in a JSON payload.
     * @return
     */
    @GetMapping("/counts", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun counts(): ResponseEntity<RepositoryCounts> = ResponseEntity.ok(annonseReplicationService.getRepositoryCounts())

}
