package no.nav.pam.adreg.migration.annonse

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface AnnonseRepository: PagingAndSortingRepository<Annonse, Long>, JpaSpecificationExecutor<Annonse> {

    fun findByUuid(uuid: String): Annonse?

    @Query("select count(distinct a.orgnr) from Annonse a")
    fun countDistinctOrgnr(): Long

    @Query("select new no.nav.pam.adreg.migration.annonse.CountStatus(a.status, count(a.status)) from Annonse a group by a.status")
    fun numberByStatus(): List<CountStatus>

    /**
     * Find all ids of entities meeting the paging restriction provided in the {@code Pageable} object.
     */
    @Query("select a.id from Annonse a")
    fun findAllIds(pageable: Pageable): Page<Long>

}

data class CountStatus(
    val status: Status,
    val count: Long
)
