# pam-stillingsregistrering-api-migration

Migreringsapplikasjon for backend
[pam-stillingsregistrering-api](https://github.com/navikt/pam-stillingsregistrering-api/)
fra on-prem-nais til GCP-nais. (Selvbetjent registrering av stillingsannonser på
arbeidsplassen.nav.no/stillingsregistrering).

Denne migreringen ble ferdigstilt 8. juni 2021 og dette repository ble arkivert 18.6.2021.


## Beskrivelse av migrering i steg

1. Denne standalone Spring Boot backend-appen ble utviklet med samme
   persisteringslag og datamodell som `pam-stillingsregistrering-api`, med noen
   forbedringer i db-schema og nullstilt Flyway-historikk. Db-genererte
   entity-id-er bevares slik de er i kilden (databasen on-prem). I så måte er
   det en 1:1 replikering av data.

2. Migrerings-appen ble deployet direkte til GCP og satt opp med
   cloudsql-database via naisapp-manifest. Den har samme app-navn som
   on-prem-appen, for å lette issues med db-tilgang osv. når den ekte backend
   skal ta over senere. Databasen populeres av nye Flyway-script i
   migrerings-appen.

3. Et enkelt JSON-feed-API ble laget i appen on-prem, slik at denne appen lese
   data-elementer (hoved-entity-aggregate) kontinuerlig. Feeden er basert på et
   updated-timestamp på root entity og filter + sortering på dette feltet.
   Samtidig lages et API som viser diverse sjekksummer på dataene (tellinger av
   diverse slag), som skal brukes til å verifisere at synk er 100%. Dette
   sjekksum-API-et er tilgjengelig både i appen on-prem og i GCP, for å kunne
   enkelt sammenlikne. (Kunne også benyttet kafka for replikering, men siden
   denne appen ikke har noe Kafka fra før var feed enkleste veien til mål.)
   
3. Migreringsappen leser kontinuerlig en JSON-feed med data-elementer (annonser)
   fra appen on-prem og replikrer alle endringer (nye/oppdaterte) til ny lagring
   i GCP (PostgreSQL). App on-prem er satt opp med nais.io-ingress for å bli
   nåbar fra GCP internt. På dette tidspunktet handler det om å fikse bugs med
   replikering og sikre at data synkes 100% korrekt hele tiden.
   
   For å oppdage slettede elementer gjør migrerings-appen periodevis en
   gjennomgang av alle replikerte annonser og slår opp i et API hos
   on-prem-appen for å finne ut hvilke id-er som er slettet, for deretter å
   synke de samme slettingene til lokalt repo. (Slettinger er ikke direkte
   representert i JSON-feeden.)
   
4. En gcp-branch ble opprettet i hoved-appen on-prem
   (`pam-stillingsregistrering-api`). I denne branchen gjøres alle tilpasninger
   av den ekte backend for kjøring i nytt miljø, og Github-workflows justeres
   for deploy ut til GCP. Flyway-script kopieres inn fra koden i denne
   migrerings-appen, for å få identisk db-historikk. Oppsett av
   cloudsql-databasen kopieres også inn i manifest.
   
   Med noen midlertidige kode-endringer settes backend opp til å kunne sperre
   all skriving. (Taktikk her var http-request--interceptor på CRUD-API som
   blokkerer skrivende verb + skrudd av periodiske tasker som skriver i
   backend.)
   
5. Når hoved-app backend deployes til GCP vil den ta over deploymentet som
   migreringsappen har, fordi de deler app-navn. Den vil da også "ta over"
   cloudsql-databasen, som er mer eller mindre ferdig synket opp med data fra
   on-prem.
   
   For å kunne både kjøre hoved-appen (i readonly), og samtidig kunne gjøre
   flere endelige synker med migrerings-appen, fikk migrerings-appen et enkelt
   kommandolinje-grensesnitt. Den ble så bundlet i Docker-image til hoved-appen,
   slik at den kunne eksekveres direkte fra POD til hovedapp (`kubectl exec -it
   ..`), med tilgang til cloudsql-database via miljøvariabler der. Bundlingen
   ble gjort ved å legge til et midlertidig steg i Github workflow som clonet og
   bygget migrerings-app-koden, og deretter ble produsert jar kopiert inn i
   image via `Dockerfile`.
   
6. Ved tidspunkt for produksjonssetting (innenfor vanlig arbeidstid) ble backend
   satt i read-only både on-prem og i GCP. Deretter ble det gjort en endelig
   synk via kommandolinje i POD i GCP med denne appen (siden mesteparten av data
   allerede er synket, så går denne raskt). Når sjekksummer er kontrollert OK er
   alt klart for svitsj. Frontend viste i denne perioden en melding til brukere
   om at det ikke var mulig å lagre noe i løsningen.
   
7. Appen kjører på en ingress (arbeidsplassen.nav.no) som både router trafikk
   til GCP-apper og on-prem-apper. Denne ingressen er manuelt oppdatert av
   infrastruktur-team i NAV. Når alt klart, så svitsjet infrastruktur-team
   trafikk til GCP for de aktuelle endepunktene til akkurat denne appen. Trafikk
   fra publikum flyttet seg da fra read-only backend on-prem til
   read-only-backend i GCP.
   
8. Frontend-app slettes fra on-prem-cluster, for å være sikker på at den ikke
   lenger videreformidler noe trafikk. Så slettes backend-appen fra
   on-prem, som nå er idle. (Database on-prem blir da idle.)
   
8. Etter kontroll av funksjon i prod-gcp ble backend deployet ut med full
   read/write, og publikum kan igjen lagre data i løsningen. (Etter dette
   tidspunktet er databasen on-prem utdatert og det vil være en større jobb å
   rulle tilbake, med revers-synk av nye endringer. Så det vil man helst ikke.)
   
   Migreringen innebar ikke full nedetid for sluttbrukere, men en kortere
   nedetid i form av lagringssperre.
   
9. Midlertidig kode ryddes vekk og gcp-brancher merges til master-brancher for
   backend/frontend. Deploy gjøres da ikke lenger til on-prem-clustre.
   
10. Denne migrerings-appen ble arkivert og denne lille beskrivelsen av stegene
    ble skrevet.


## Generelt om migrering av frontend

Frontend-appen
[pam-stillingsregistrering](https://github.com/navikt/pam-stillingsregistrering)
er vesentlig enklere å migrere, og alt rundt denne kan gjøres klart på forhånd.
Gcp-branch ble opprettet i kodebasen, og Github workflows i denne branchen
justert for deploy ut til GCP, med nytt naisapp-manifest og tilpassede
ingresser, etc.
   
   
## Kjøre lokalt

Ikke veldig aktuelt lenger. Dette ligger her mest for å kunne se tilbake på kode
og README.

Appen krever at flere miljøvariabler er satt på forhånd. Se
`src/main/application.yml` and `.nais/nais.yaml` for detaljer.

    mvn clean install
        
Manuell kjøring fra som kommandolinje-app:

    java -jar target/migration-*.jar --onepass --update --delete
    
    
Eller for kontinuerlig replikering (kjører som egen app):

    java -jar target/migration-*.jar --migration.scheduler.enabled=true
