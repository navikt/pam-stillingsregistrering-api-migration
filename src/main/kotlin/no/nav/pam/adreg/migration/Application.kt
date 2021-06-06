package no.nav.pam.adreg.migration

import no.nav.pam.adreg.migration.annonse.Annonse
import no.nav.pam.adreg.migration.annonse.AnnonseStorageService
import no.nav.pam.adreg.migration.replication.AnnonseReplicationService
import no.nav.pam.adreg.migration.replication.AnnonseReplicationTask
import no.nav.pam.feed.taskscheduler.FeedTaskService
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ConfigurableApplicationContext
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

private fun waitForPort(host: String, port: Int): Boolean {
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
class OnePassMigrationRunner(
	feedTaskService: FeedTaskService,
	annonseReplicationService: AnnonseReplicationService
): ApplicationRunner, ApplicationContextAware {

	private lateinit var applicationContext: ApplicationContext

	private val taskInstance = AnnonseReplicationTask(feedTaskService, annonseReplicationService)

	override fun run(args: ApplicationArguments?) {
		if (args?.containsOption("onepass") != true) {
			return
		}

		if (args.containsOption("update")) {
			taskInstance.executeUpdateBatch()
		}

		if (args.containsOption("delete")) {
			taskInstance.executeDeleteBatch()
		}

		(applicationContext as ConfigurableApplicationContext).close()
	}

	override fun setApplicationContext(applicationContext: ApplicationContext) {
		this.applicationContext = applicationContext
	}

}
