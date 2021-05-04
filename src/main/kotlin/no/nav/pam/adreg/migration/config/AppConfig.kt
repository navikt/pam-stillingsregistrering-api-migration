package no.nav.pam.adreg.migration.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Configuration
@EnableJpaRepositories(basePackages = ["no.nav.pam.adreg.migration", "no.nav.pam.feed"])
@EntityScan(basePackages = ["no.nav.pam.adreg.migration", "no.nav.pam.feed"])
class AppConfig {

    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate = builder
        .setConnectTimeout(Duration.ofSeconds(10))
        .setReadTimeout(Duration.ofSeconds(60))
        .build()

}
