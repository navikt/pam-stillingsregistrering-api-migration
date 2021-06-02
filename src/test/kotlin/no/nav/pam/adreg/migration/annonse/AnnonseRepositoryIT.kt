package no.nav.pam.adreg.migration.annonse

import no.nav.pam.adreg.migration.Application
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*


@ExtendWith(SpringExtension::class)
@DataJpaTest
@ContextConfiguration(classes = [Application::class])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class AnnonseRepositoryIT {

    @Autowired
    lateinit var annonseRepository: AnnonseRepository

    @AfterEach
    fun clearRepository() = annonseRepository.deleteAll()

    @Test
    fun `test save`() {
        val id = 1000L;
        val uuid = UUID.randomUUID().toString()

        var annonse = Annonse().also {
            it.id = id
            it.uuid = uuid
            it.orgnr = "900000000"
            it.created = LocalDateTime.now()
            it.updated = it.created
        }

        annonse = annonseRepository.save(annonse)

        assertEquals(id, annonse.id)
        assertEquals(uuid, annonse.uuid)
        assertEquals("900000000", annonse.orgnr)
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun `test property storage`() {
        val id = 1000L;
        val uuid = UUID.randomUUID().toString()

        var annonse = Annonse().also {
            it.id = id
            it.uuid = uuid
            it.orgnr = "900000000"
            it.created = LocalDateTime.now()
            it.updated = it.created
            it.properties["foo"] = "bar"
            it.properties["baz"] = "some property value"
        }

        annonseRepository.save(annonse)

        val annonseFromDb = annonseRepository.findByUuid(uuid)

        assertEquals("bar", annonseFromDb?.properties?.get("foo"))
        assertEquals("some property value", annonseFromDb?.properties?.get("baz"))
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun  `test findAllIds`() {
        val annonser = listOf(1000L, 1001L, 2003L).map { id ->
            Annonse().also {
                it.id = id
                it.uuid = UUID.randomUUID().toString()
                it.orgnr = "900000000"
                it.created = LocalDateTime.now()
                it.updated = it.created
            }
        }

        val savedAll = annonseRepository.saveAll(annonser)

        var idPage = annonseRepository.findAllIds(PageRequest.of(0, 2, Sort.by("id")))
        assertEquals(3, idPage.totalElements)
        assertEquals(1000L, idPage.content[0])
        assertEquals(1001L, idPage.content[1])

        idPage = annonseRepository.findAllIds(idPage.nextPageable())
        assertEquals(2003L, idPage.content[0])
    }

}
