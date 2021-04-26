package no.nav.pam.adreg.migration.annonse

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

@Service
class AnnonseStorageService(val annonseRepository: AnnonseRepository,
                            val entityManager: EntityManager) {

    @Transactional
    fun save(annonse: Annonse): Annonse = annonseRepository.save(annonse)

    @Transactional
    fun delete(annonse: Annonse) = annonseRepository.delete(annonse)

    @Transactional(readOnly = true)
    fun findById(id: Long): Annonse? = annonseRepository.findById(id).orElse(null)

    @Transactional(readOnly = true)
    fun findByUuid(uuid: String): Annonse? = annonseRepository.findByUuid(uuid)

    @Transactional
    fun updateAnnonseSequence(): Long {
        val currentMax = entityManager.createNativeQuery("SELECT MAX(id) FROM annonse").singleResult as Number?

        val nextVal: Long = (currentMax?.toLong()?:999) + 1

        entityManager.createNativeQuery("ALTER SEQUENCE annonse_seq RESTART WITH ${nextVal}").executeUpdate();

        return nextVal
    }

}
