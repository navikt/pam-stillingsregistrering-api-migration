# pam-stillingsregistrering-api-migration

Temporary app for continuous data migration for [stillingsregistrering-api](https://github.com/navikt/pam-stillingsregistrering-api/) from on-prem til cloud environment.

## Running locally

Requires several environment variables to be set, database details and source API URL. See `src/main/application.yml` and `.nais/nais.yaml` for details.

    mvn clean install
    java -jar target/migration-*.jar
