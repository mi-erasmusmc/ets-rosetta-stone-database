CREATE TABLE IF NOT EXISTS concept
(
    concept_id       INT          NOT NULL,
    concept_name     VARCHAR(255) NOT NULL,
    domain_id        VARCHAR(20)  NOT NULL,
    vocabulary_id    VARCHAR(20)  NOT NULL,
    concept_class_id VARCHAR(20)  NOT NULL,
    standard_concept VARCHAR(1)   NULL,
    concept_code     VARCHAR(50)  NOT NULL,
    valid_start_date DATE         NOT NULL,
    valid_end_date   DATE         NOT NULL,
    invalid_reason   VARCHAR(255) NULL
);

CREATE TABLE IF NOT EXISTS concept_ancestor
(
    ancestor_concept_id      INT NOT NULL,
    descendant_concept_id    INT NOT NULL,
    min_levels_of_separation INT NOT NULL,
    max_levels_of_separation INT NOT NULL
);

CREATE TABLE IF NOT EXISTS concept_class
(
    concept_class_id         VARCHAR(20)  NOT NULL,
    concept_class_name       VARCHAR(255) NOT NULL,
    concept_class_concept_id INT          NOT NULL
);

CREATE TABLE IF NOT EXISTS concept_relationship
(
    concept_id_1     INT         NOT NULL,
    concept_id_2     INT         NOT NULL,
    relationship_id  VARCHAR(20) NOT NULL,
    valid_start_date DATE        NULL,
    valid_end_date   DATE        NULL,
    invalid_reason   VARCHAR(50) NULL,
    source           VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS concept_synonym
(
    concept_id           INT           NOT NULL,
    concept_synonym_name VARCHAR(4096) NOT NULL,
    language_concept_id  INT           NOT NULL
);

CREATE TABLE IF NOT EXISTS domain
(
    domain_id         VARCHAR(20)  NOT NULL,
    domain_name       VARCHAR(255) NOT NULL,
    domain_concept_id INT          NOT NULL
);

CREATE TABLE IF NOT EXISTS finding_mapping
(
    preclinical_concept_code VARCHAR(255) NULL,
    preclinical_organ_code   VARCHAR(255) NULL,
    clinical_concept_code    VARCHAR(255) NULL,
    distance                 INT          NULL
);

CREATE TABLE IF NOT EXISTS organ_mapping
(
    source_id   INT          NULL,
    source_name VARCHAR(255) NULL,
    source_code VARCHAR(255) NULL,
    target_id   INT          NULL,
    target_code VARCHAR(255) NULL,
    target_name VARCHAR(255) NULL,
    distance    INT          NULL
);

CREATE TABLE IF NOT EXISTS relationship
(
    relationship_id         VARCHAR(40)  NOT NULL,
    relationship_name       VARCHAR(255) NOT NULL,
    is_hierarchical         VARCHAR(1)   NOT NULL,
    defines_ancestry        VARCHAR(1)   NOT NULL,
    reverse_relationship_id VARCHAR(40)  NOT NULL,
    relationship_concept_id INT          NOT NULL
);

CREATE TABLE IF NOT EXISTS vocabulary
(
    vocabulary_id         VARCHAR(20)  NOT NULL,
    vocabulary_name       VARCHAR(255) NOT NULL,
    vocabulary_reference  VARCHAR(255) NOT NULL,
    vocabulary_version    VARCHAR(255) NULL,
    vocabulary_concept_id INT          NOT NULL
);

