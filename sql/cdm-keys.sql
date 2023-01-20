ALTER TABLE concept
    ADD CONSTRAINT concept_pk
        PRIMARY KEY (concept_id);

ALTER TABLE concept_ancestor
    ADD CONSTRAINT
        FOREIGN KEY (ancestor_concept_id) REFERENCES concept (`concept_id`) ON DELETE CASCADE;

ALTER TABLE concept_ancestor
    ADD CONSTRAINT
        FOREIGN KEY (descendant_concept_id) REFERENCES concept (`concept_id`) ON DELETE CASCADE;

ALTER TABLE concept_relationship
    ADD CONSTRAINT
        FOREIGN KEY (concept_id_2) REFERENCES concept (`concept_id`) ON DELETE CASCADE;

ALTER TABLE concept_relationship
    ADD CONSTRAINT
        FOREIGN KEY (concept_id_1) REFERENCES concept (`concept_id`) ON DELETE CASCADE;

ALTER TABLE concept_synonym
    ADD CONSTRAINT
        FOREIGN KEY (concept_id) REFERENCES concept (`concept_id`) ON DELETE CASCADE;
