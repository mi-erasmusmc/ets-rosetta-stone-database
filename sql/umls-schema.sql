CREATE TABLE MRCONSO
(
    CUI      CHAR(8)      NOT NULL,
    LAT      CHAR(3)      NOT NULL,
    TS       CHAR         NOT NULL,
    LUI      VARCHAR(10)  NOT NULL,
    STT      VARCHAR(3)   NOT NULL,
    SUI      VARCHAR(10)  NOT NULL,
    ISPREF   CHAR         NOT NULL,
    AUI      VARCHAR(9)   NOT NULL,
    SAUI     VARCHAR(50)  NULL,
    SCUI     VARCHAR(100) NULL,
    SDUI     VARCHAR(100) NULL,
    SAB      VARCHAR(40)  NOT NULL,
    TTY      VARCHAR(40)  NOT NULL,
    CODE     VARCHAR(100) NOT NULL,
    STR      TEXT         NOT NULL,
    SRL      INT          NOT NULL,
    SUPPRESS CHAR         NOT NULL,
    CVF      INT          NULL
);
