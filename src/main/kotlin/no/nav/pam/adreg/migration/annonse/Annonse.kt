package no.nav.pam.adreg.migration.annonse

import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*


@Entity
@Table(name = "annonse")
@SequenceGenerator(name = "annonse_seq", sequenceName = "annonse_seq", allocationSize = 1)
class Annonse {

    @Id
    // Ids are replicated from another database, so no sequence use here, must be set explicitly.
    //@GeneratedValue(generator = "annonse_seq")
    var id: Long? = null

    var uuid: String? = null

    var overskrift: String? = null
    var soknadsfrist: String? = null
    var arbeidsgiver: String? = null
    var orgnr: String? = null

    var toBeExported: Boolean = false

    // Changed from original schema, from ordinal db storage, to string based enum storage:
    @Enumerated(EnumType.STRING)
    var status: Status = Status.PAABEGYNT

    var updated: LocalDateTime? = null
    var created: LocalDateTime? = null
    var validUntil: LocalDate? = null
    var publishAt: LocalDate? = null

    var antallStillinger: Int? = null

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "property_key")
    @Column(name = "property_value", nullable = false)
    @CollectionTable(
        name = "annonse_property",
        joinColumns = [JoinColumn(name = "annonse_id")],
        uniqueConstraints = [UniqueConstraint(columnNames = ["annonse_id", "property_key"])])
    var properties: MutableMap<String,String> = mutableMapOf()

    override fun toString(): String {
        return "Annonse(id=$id, uuid=$uuid, overskrift=$overskrift, soknadsfrist=$soknadsfrist, arbeidsgiver=$arbeidsgiver, orgnr=$orgnr, toBeExported=$toBeExported, status=$status, updated=$updated, created=$created, validUntil=$validUntil, publishAt=$publishAt, properties=$properties)"
    }

}

enum class Status {
    PAABEGYNT, TIL_GODKJENNING, TIL_PUBLISERING, AVVIST, PUBLISERT, AVSLUTTET, TIL_AVSLUTTING
}
