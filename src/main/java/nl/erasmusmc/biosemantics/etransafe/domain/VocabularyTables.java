package nl.erasmusmc.biosemantics.etransafe.domain;

public enum VocabularyTables {

    CONCEPT,
    CONCEPT_ANCESTOR,
    CONCEPT_CLASS,
    CONCEPT_RELATIONSHIP,
    CONCEPT_SYNONYM,
    DOMAIN,
    RELATIONSHIP,
    VOCABULARY

    // We don't use the 'drug_strength' table, and don't want to load it, so it is not included here.

}
