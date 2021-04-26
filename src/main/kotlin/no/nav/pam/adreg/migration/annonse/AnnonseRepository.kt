package no.nav.pam.adreg.migration.annonse

import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface AnnonseRepository: PagingAndSortingRepository<Annonse, Long>, JpaSpecificationExecutor<Annonse> {

    fun findByUuid(uuid: String): Annonse?

}
