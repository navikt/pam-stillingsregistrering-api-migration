CREATE TABLE annonse
(
    id                NUMERIC(19, 0)          NOT NULL,
    uuid              CHAR(36)                NOT NULL,
    overskrift        VARCHAR(255),
    antall_stillinger NUMERIC(5, 0),
    soknadsfrist      VARCHAR(255),
    status            VARCHAR(36),
    valid_until       TIMESTAMP,
    updated           TIMESTAMP               NOT NULL,
    created           TIMESTAMP               NOT NULL,
    publish_at        TIMESTAMP,
    arbeidsgiver      VARCHAR(255),
    orgnr             VARCHAR(9)              NOT NULL,
    to_be_exported    BOOLEAN DEFAULT FALSE NOT NULL,
    CONSTRAINT pk_annonse PRIMARY KEY (id),
    CONSTRAINT uc_annonseuuid UNIQUE (uuid)
);

CREATE SEQUENCE annonse_seq START WITH 1000;

CREATE TABLE annonse_property (
  annonse_id     NUMERIC(19, 0) NOT NULL,
  property_key   VARCHAR(255)   NOT NULL,
  property_value TEXT           NOT NULL,
  CONSTRAINT fk_annonse FOREIGN KEY (annonse_id) REFERENCES annonse (id),
  CONSTRAINT uc_annonse_property_key UNIQUE (annonse_id, property_key)
);

CREATE INDEX index_orgnr ON annonse (orgnr);

-- pam-feed
CREATE TABLE feedtask (
  id            NUMERIC(19, 0) NOT NULL,
  feed_name     VARCHAR(255)  NOT NULL,
  last_run_date TIMESTAMP,
  CONSTRAINT uc_feedtask_name UNIQUE (feed_name),
  CONSTRAINT pk_feedtask PRIMARY KEY (id)
);

CREATE SEQUENCE feedtask_seq START WITH 1;


-- shedlock
CREATE TABLE shedlock (
  name       VARCHAR(64) PRIMARY KEY,
  lock_until TIMESTAMP(3),
  locked_at  TIMESTAMP(3),
  locked_by  VARCHAR(255)
);
