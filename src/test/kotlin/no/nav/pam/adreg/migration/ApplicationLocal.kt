package no.nav.pam.adreg.migration

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ApplicationLocal

fun main(args: Array<String>) {
    runApplication<ApplicationLocal>(*(args + "--spring.profiles.active=test"))
}
