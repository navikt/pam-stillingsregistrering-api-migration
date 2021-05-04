package no.nav.pam.adreg.migration.replication

import no.nav.pam.adreg.migration.annonse.Annonse
import no.nav.pam.adreg.migration.annonse.Status
import java.time.LocalDate
import java.time.ZonedDateTime

data class AnnonseReplicationDto(
    val id: Long,
    val uuid: String,
    val updated: ZonedDateTime,
    val created: ZonedDateTime,
    val overskrift: String?,
    val soknadsfrist: String?,
    val arbeidsgiver: String?,
    val orgnr: String?,
    val publiserFra: LocalDate?,
    val sistePubliseringsDato: LocalDate?,
    val antallStillinger: Int?,
    val toBeExported: Boolean,
    val status: String,
    val property: Map<String, String>
) {

    internal fun toAnnonnse() = Annonse().also {
        it.id = id
        it.uuid = uuid
        it.updated = updated.toLocalDateTime()
        it.created = created.toLocalDateTime()
        it.overskrift = overskrift
        it.soknadsfrist = soknadsfrist
        it.arbeidsgiver = arbeidsgiver
        it.orgnr = orgnr
        it.publishAt = publiserFra
        it.validUntil = sistePubliseringsDato
        it.antallStillinger = antallStillinger
        it.toBeExported = toBeExported
        it.status = Status.valueOf(status)
        it.properties = LinkedHashMap(property)
    }

}
