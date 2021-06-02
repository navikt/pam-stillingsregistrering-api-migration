package no.nav.pam.adreg.migration.annonse

import no.nav.pam.adreg.migration.Application
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@ExtendWith(SpringExtension::class)
@SpringBootTest (classes = [Application::class])
@ActiveProfiles("test")
class AnnonseStorageServiceIT {

	@Autowired
	private lateinit var annonseStorageService: AnnonseStorageService

	@Autowired
	private lateinit var annonseRepository: AnnonseRepository

	@AfterEach
	fun deleteAllAnnonser() = annonseRepository.deleteAll()

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	fun `sequence update`() {
		var replicatedAds = listOf(
			Annonse().apply { id = 1000; uuid = UUID.randomUUID().toString(); updated = LocalDateTime.now(); created = updated; orgnr = "900000000" },
			Annonse().apply { id = 2000; uuid = UUID.randomUUID().toString(); updated = LocalDateTime.now(); created = updated; orgnr = "900000000" }
		)

		replicatedAds = replicatedAds.map { annonseStorageService.save(it) }

		assertEquals(2001L, annonseStorageService.updateAnnonseSequence())
	}

	@Test
	fun `repository counts`() {
		annonseStorageService.updateOrCreateAll(listOf(
			Annonse().apply { id = 1000; uuid = UUID.randomUUID().toString(); updated = LocalDateTime.now(); created = updated; orgnr = "900000000" },
			Annonse().apply { id = 2000; uuid = UUID.randomUUID().toString(); updated = LocalDateTime.now(); created = updated; orgnr = "900000000" }
		))

		val counts = annonseStorageService.getRepositoryCounts()

		assertEquals(2, counts.total)
		assertEquals(1, counts.distinctOrgnr)
		assertEquals(2, counts.statusCounts[Status.PAABEGYNT.name])
	}

}
