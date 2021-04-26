package no.nav.pam.adreg.migration

import no.nav.pam.adreg.migration.annonse.Annonse
import no.nav.pam.adreg.migration.annonse.AnnonseStorageService
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@SpringBootApplication
class Application

fun main(args: Array<String>) {
	runApplication<Application>(*args)
}

@Component
@Profile("dbtest")
class DbTestingConfig(val aservice: AnnonseStorageService): ApplicationRunner {
	override fun run(args: ApplicationArguments?) {

		var annonse = aservice.findById(1002)?: Annonse().apply { id = 1002; uuid = UUID.randomUUID().toString() }

		annonse.apply {
			toBeExported = false
			overskrift = "En overskrift her"
			arbeidsgiver = "Fisk og flesk AS"
			updated = LocalDateTime.now()
			created = LocalDateTime.now()
			orgnr = "900000000"

			properties["foo"] = "bar"
			properties["x"] = "some value"
		}

		annonse = aservice.save(annonse)

		println("Saved a test annonse")
		println(annonse)

		aservice.updateAnnonseSequence()
	}

}
