package no.nav.pam.adreg.migration.rest

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class StatusController {

    @GetMapping("/isready")
    fun isReady() = ResponseEntity.ok("OK")

    @GetMapping("/isalive")
    fun isHealthy() = ResponseEntity.ok("OK")

}
