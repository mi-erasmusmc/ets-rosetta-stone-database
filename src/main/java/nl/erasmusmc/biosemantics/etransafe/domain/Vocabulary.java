package nl.erasmusmc.biosemantics.etransafe.domain;

public record Vocabulary(String vocabularyId, String vocabularyName, String vocabularyReference,
                         String vocabularyVersion, Integer vocabularyConceptId) {

}
