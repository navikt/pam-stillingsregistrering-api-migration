package no.nav.pam.adreg.migration.annonse

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

@Service
class AnnonseStorageService(val annonseRepository: AnnonseRepository,
                            val entityManager: EntityManager) {

    @Transactional
    fun save(annonse: Annonse): Annonse = annonseRepository.save(annonse)

    @Transactional
    fun deleteById(id: Long) = annonseRepository.deleteById(id)

    @Transactional
    fun delete(annonse: Annonse) = annonseRepository.delete(annonse)

    @Transactional(readOnly = true)
    fun findById(id: Long): Annonse? = annonseRepository.findById(id).orElse(null)

    @Transactional(readOnly = true)
    fun findAllIds(pageable: Pageable) = annonseRepository.findAllIds(pageable)

    @Transactional
    fun updateOrCreateAll(annonseList: List<Annonse>): UpdateResult {
        var created = 0

        val saved = annonseList.map {
            val existingAnnonse =
                findById(it.id ?: throw IllegalArgumentException("Annonse without database id"))

            if (existingAnnonse == null) {
                ++created
            }

            save(it.validateAndMerge(existingAnnonse))
        }

        updateAnnonseSequence()

        return UpdateResult(saved.size - created, created)
    }

    @Transactional(readOnly = true)
    fun getRepositoryCounts() = RepositoryCounts(
        annonseRepository.count(),
        annonseRepository.countDistinctOrgnr(),
        annonseRepository.numberByStatus()
            .sortedBy { it.count }
            .map { it.status.name to it.count }
            .toMap(LinkedHashMap())
    )

    private fun Annonse.validateAndMerge(existingAnnonse: Annonse?): Annonse {
        if (id == null || uuid == null) {
            throw IllegalArgumentException("Incoming annonse without id/uuid")
        }
        if (orgnr == null) {
            throw IllegalArgumentException("Incoming annonse without orgnr is invalid (employer ownership missing)")
        }
        if (existingAnnonse != null && id != existingAnnonse.id) {
            throw IllegalArgumentException("Annonse id mismatch")
        }
        if (existingAnnonse != null && uuid != existingAnnonse.uuid) {
            throw IllegalArgumentException("Annonse UUID mismatch, id=${id}")
        }

        return this
    }

    @Transactional
    internal fun updateAnnonseSequence(): Long {
        entityManager.flush()

        val currentMax = entityManager.createNativeQuery("SELECT MAX(id) FROM annonse").singleResult as Number?

        val nextVal: Long = (currentMax?.toLong()?:999) + 1

        entityManager.createNativeQuery("ALTER SEQUENCE annonse_seq RESTART WITH ${nextVal}").executeUpdate();

        return nextVal
    }

}

data class UpdateResult (
    val updated: Int,
    val created: Int,
)

data class RepositoryCounts (
    val total: Long,
    val distinctOrgnr: Long,
    val statusCounts: Map<String, Long>
)
