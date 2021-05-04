package no.nav.pam.adreg.migration

import no.nav.pam.adreg.migration.annonse.Annonse
import no.nav.pam.adreg.migration.annonse.AnnonseStorageService
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.IOException
import java.lang.RuntimeException
import java.net.InetAddress
import java.net.Socket
import java.time.LocalDateTime
import java.util.*

@SpringBootApplication(scanBasePackageClasses = [Application::class], scanBasePackages = ["no.nav.pam.feed"])
class Application

fun main(args: Array<String>) {
	// Delay startup until db port can be connected to (wait for cloud sql proxy)
	val dbHost = System.getenv("PAMADREGDB_HOST")?:throw IllegalStateException("Missing environment variable PAMADREGDB_HOST")
	val dbPort = Integer.parseInt(System.getenv("PAMADREGDB_PORT"))

	println("Waiting for database port at ${dbHost}:${dbPort} to become connectable ..")
	waitForPort(dbHost, dbPort)

	runApplication<Application>(*args)
}

fun waitForPort(host: String, port: Int): Boolean {
	for (attempt in IntRange(1, 10)) {
		try {
			Socket(InetAddress.getByName(host), port).use {
				println("Socket at ${host}:${port} is connectable at attempt ${attempt}")
				return true
			}
		} catch (io: IOException) {
			println("Socket at ${host}:${port} is not ready yet: ${io.message}")
		}
		Thread.sleep(1000)
	}
	return false
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
	}

}
