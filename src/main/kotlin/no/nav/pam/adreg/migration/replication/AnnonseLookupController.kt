package no.nav.pam.adreg.migration.replication

import no.nav.pam.adreg.migration.annonse.AnnonseRepository
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.websocket.server.PathParam

@RestController
@RequestMapping("/replication")
class AnnonseLookupController(val annonseRepository: AnnonseRepository) {

    @GetMapping("/ad/{uuid}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun adLookup(@PathParam("uuid") uuid: String): ResponseEntity<AnnonseReplicationDto> =
        annonseRepository.findByUuid(uuid)?.let { ResponseEntity.ok(fromAnnonse(it)) }?:ResponseEntity.notFound().build()

}
