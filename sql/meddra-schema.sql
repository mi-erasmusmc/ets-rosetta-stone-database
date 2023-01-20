DROP TABLE IF EXISTS MEDDRA.SMQ_LIST;

CREATE TABLE MEDDRA.SMQ_LIST
(
    smq_code        BIGINT       NOT NULL,
    smq_name        VARCHAR(100) NOT NULL,
    smq_level       INTEGER      NOT NULL,
    smq_description VARCHAR(2000),
    smq_source      VARCHAR(2000),
    smq_note        VARCHAR(2000),
    meddra_version  VARCHAR(5)   NOT NULL,
    status          CHAR         NOT NULL,
    smq_algorithm   VARCHAR(2000)
);

DROP TABLE IF EXISTS MEDDRA.SMQ_CONTENT;

CREATE TABLE MEDDRA.SMQ_CONTENT
(
    smq_code                   BIGINT     NOT NULL,
    term_code                  BIGINT     NOT NULL,
    term_level                 INTEGER    NOT NULL,
    term_scope                 INTEGER    NOT NULL,
    term_category              CHAR       NOT NULL,
    term_weight                INTEGER    NOT NULL,
    term_status                CHAR       NOT NULL,
    term_addition_version      VARCHAR(5) NOT NULL,
    term_last_modified_version VARCHAR(5) NOT NULL
);

DROP TABLE IF EXISTS MEDDRA.MDHIER;

CREATE TABLE MEDDRA.MDHIER
(
    pt_code        INT          NOT NULL,
    hlt_code       INT          NOT NULL,
    hlgt_code      INT          NOT NULL,
    soc_code       INT          NOT NULL,
    pt_name        VARCHAR(255) NOT NULL,
    hlt_name       VARCHAR(255) NOT NULL,
    hlgt_name      VARCHAR(255) NOT NULL,
    soc_name       VARCHAR(255) NOT NULL,
    soc_abbrev     VARCHAR(5)   NOT NULL,
    null_field     VARCHAR(1)   NULL,
    pt_soc_code    INT          NULL,
    primary_soc_fg VARCHAR(1)   NULL
);

DROP TABLE IF EXISTS MEDDRA.LOW_LEVEL_TERM;

CREATE TABLE MEDDRA.LOW_LEVEL_TERM
(
    llt_code        INT          NOT NULL,
    llt_name        VARCHAR(255) NOT NULL,
    pt_code         INT          NULL,
    llt_whoart_code CHAR(7)      NULL,
    llt_harts_code  INT          NULL,
    llt_costart_sym CHAR(21)     NULL,
    llt_icd9_code   CHAR(8)      NULL,
    llt_icd9cm_code CHAR(8)      NULL,
    llt_icd10_code  CHAR(8)      NULL,
    llt_currency    CHAR         NULL,
    llt_jart_code   CHAR(8)      NULL
);






