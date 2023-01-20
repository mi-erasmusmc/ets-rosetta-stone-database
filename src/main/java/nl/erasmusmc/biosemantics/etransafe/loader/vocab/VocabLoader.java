package nl.erasmusmc.biosemantics.etransafe.loader.vocab;

import nl.erasmusmc.biosemantics.etransafe.domain.Concept;
import nl.erasmusmc.biosemantics.etransafe.repo.CDM;

import java.util.Map;
import java.util.Set;

public interface VocabLoader {

    void loadVocab();

    default boolean existsAndSet(Concept parent, Concept child, Set<String> exists) {
        String key = parent.getConceptCode() + "_" + child.getConceptCode();
        if (!exists.contains(key)) {
            exists.add(key);
            return false;
        }
        return true;
    }

    default void addAncestorTree(CDM cdm, Concept parentConcept, Concept childConcept, int levelsOfSeparation, Set<String> exists, Map<String, Concept> concepts) {
        if (!existsAndSet(parentConcept, childConcept, exists)) {
            cdm.addConceptAncestor(parentConcept.getConceptId(), childConcept.getConceptId(), levelsOfSeparation, levelsOfSeparation);
            if (childConcept.getChildren() != null) {
                childConcept.getChildren().forEach(c -> {
                    addAncestorTree(cdm, parentConcept, concepts.getOrDefault(c, null), levelsOfSeparation + 1, exists, concepts);
                    addAncestorTree(cdm, childConcept, concepts.getOrDefault(c, null), 1, exists, concepts);
                });
            }
        }
    }
}
