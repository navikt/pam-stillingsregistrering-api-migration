package no.nav.pam.adreg.migration.replication

import no.nav.pam.adreg.migration.annonse.Annonse
import no.nav.pam.adreg.migration.annonse.AnnonseStorageService
import no.nav.pam.adreg.migration.annonse.RepositoryCounts
import no.nav.pam.feed.client.FeedConnector
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Service for processing batches of annonse updates
 */
@Service
class AnnonseReplicationService(
    val annonseStorageService: AnnonseStorageService,
    val feedConnector: FeedConnector,
    val restTemplate: RestTemplate,
    @Value("\${migration.api.baseurl}") val migrationApiBaseurl: String
) {

    private val log = LoggerFactory.getLogger(AnnonseReplicationService::class.java)

    /**
     * Replicate a batch of annonser updates since provided time.
     * @return updated time of the latest updated annonse, or <code>null</code> in case of no updates since provided timestamp
     */
    fun processUpdateBatchSince(since: LocalDateTime): LocalDateTime? {

        val updatedAnnonser: List<AnnonseReplicationDto> =
            feedConnector.fetchContentList(migrationApiBaseurl+"/feed", since, AnnonseReplicationDto::class.java)

        log.info("${updatedAnnonser.size} annonser updated or created since ${since}")

        val updateResult =
            annonseStorageService.updateOrCreateAll(updatedAnnonser.discardInvalid().map { it.toAnnonnse() })

        log.info("Updated ${updateResult.updated} annonser, created ${updateResult.created} annonser")

        return updatedAnnonser.maxByOrNull { it.updated }?.updated?.withZoneSameInstant(ZoneId.systemDefault())?.toLocalDateTime()
    }

    // Annonser which are invalid and should not be kept for the future
    private fun List<AnnonseReplicationDto>.discardInvalid(): List<AnnonseReplicationDto> =
        filter {
            if (it.orgnr == null) {
                log.warn("Incoming annonse without orgnr will be skipped for replication: id=${it.id}, uuid=${it.uuid}")
                return@filter false
            }
            return@filter true
        }

    /**
     * Finds and deletes annonser from local repository that have vanished from source.
     * @return total number of deleted
     */
    fun processAllDeletes(): Int {
        val deleted: MutableSet<Long> = mutableSetOf()
        var page = annonseStorageService.findAllIds(PageRequest.of(0, 200, Sort.by("id")))

        while (! page.isEmpty) {
            log.info("processAllDeletes() at page ${page.number} ..")

            deleted.addAll(fetchDeletedFromSource(page.content))
            if (page.hasNext()) {
                page = annonseStorageService.findAllIds(page.nextPageable())
            } else break
        }

        if (deleted.isEmpty()) {
            log.info("No deleted annonser found")
        } else {
            log.info("Removing ${deleted.size} annonser which have been deleted from source")
        }

        deleted.forEach { annonseStorageService.deleteById(it) }

        return deleted.size
    }

    fun getRepositoryCounts(): RepositoryCounts = annonseStorageService.getRepositoryCounts()

    private fun fetchDeletedFromSource(annonseIds: List<Long>): Set<Long> {
        val uri = UriComponentsBuilder.fromUriString(migrationApiBaseurl).pathSegment("exists")

        val response = restTemplate.exchange(uri.build().toUri(),
            HttpMethod.POST,
            HttpEntity(annonseIds.map { AnnonseExistsDto(it) }),
            object : ParameterizedTypeReference<List<AnnonseExistsDto>>() {})

        return response.body!!.filter { it.exists == false }.map { it.id }.toSet()
    }

}
