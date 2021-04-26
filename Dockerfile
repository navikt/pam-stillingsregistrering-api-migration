FROM navikt/java:11
COPY target/migration-*.jar /app.jar
EXPOSE 8080
