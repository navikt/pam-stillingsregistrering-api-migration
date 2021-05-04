package no.nav.pam.adreg.migration.replication

data class AnnonseExistsDto(
    val id: Long,
    val exists: Boolean? = null
)
