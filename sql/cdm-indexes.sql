CREATE INDEX concept_synonym_concept_id
    ON concept_synonym (concept_id);

CREATE FULLTEXT INDEX concept_synonym_name
    ON concept_synonym (concept_synonym_name);

CREATE INDEX concept_id_1
    ON concept_relationship (concept_id_1);

CREATE INDEX concept_id_2
    ON concept_relationship (concept_id_2);

CREATE INDEX relationship_id
    ON concept_relationship (relationship_id);

CREATE INDEX source
    ON concept_relationship (source);

CREATE INDEX ancestor_concept_id
    ON concept_ancestor (min_levels_of_separation);

CREATE INDEX ancestor_concept_id_4
    ON concept_ancestor (ancestor_concept_id);

CREATE INDEX descendant_concept_id
    ON concept_ancestor (descendant_concept_id);

CREATE INDEX descendant_concept_id_2
    ON concept_ancestor (max_levels_of_separation);

CREATE INDEX concept_code
    ON concept (concept_code);

CREATE INDEX concept_concept_class_id_index
    ON concept (concept_class_id);

CREATE INDEX concept_id
    ON concept (concept_id);

CREATE FULLTEXT INDEX concept_name
    ON concept (concept_name);

CREATE INDEX concept_name_2
    ON concept (concept_name);

CREATE INDEX concept_vocabulary_id_index
    ON concept (vocabulary_id);