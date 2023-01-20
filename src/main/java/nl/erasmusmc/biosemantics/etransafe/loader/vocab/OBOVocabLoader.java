package nl.erasmusmc.biosemantics.etransafe.loader.vocab;

import nl.erasmusmc.biosemantics.etransafe.domain.Concept;
import nl.erasmusmc.biosemantics.etransafe.domain.OBOTerm;
import nl.erasmusmc.biosemantics.etransafe.domain.Vocabulary;
import nl.erasmusmc.biosemantics.etransafe.parser.DateParser;
import nl.erasmusmc.biosemantics.etransafe.parser.OboParser;
import nl.erasmusmc.biosemantics.etransafe.repo.CDM;
import nl.erasmusmc.biosemantics.etransafe.repo.IDGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.SystemUtils.USER_DIR;

public class OBOVocabLoader implements VocabLoader {

    private static final Logger logger = LogManager.getLogger();
    private final boolean eToxMA;
    private final CDM cdm;


    public OBOVocabLoader(CDM cdm, boolean eToxMA) {
        this.cdm = cdm;
        this.eToxMA = eToxMA;
    }


    public void loadVocab() {
        String basePath = USER_DIR + "/data/ontologies/";
        // MOA is available in the folder but not used at present
        process(basePath + "hpath_20180424.obo", "HPATH", "Histopathology", "eTox Histopathology", "Observation", "Morph Abnormality", "24-04-2018");
        process(basePath + "in-life observation_20180129.obo", "ILO", "In-Life Observations", "eTox In-Life Observations", "Observation", "Morph Abnormality", "29-01-2018");
        if (eToxMA) {
            process(basePath + "ma_etox_20180129.obo", "MA", "Mouse Anatomy", "eTox Mouse Anatomy", "Spec Anatomic Site", "Body Structure", "24-04-2018");
        } else {
            process(basePath + "ma_original_20170207.obo", "MA", "Mouse Anatomy", "Mouse Adult Gross Anatomy Ontology", "Spec Anatomic Site", "Body Structure", "07-02-2017");
        }
    }

    private void process(String oboFilename, String vocId, String vocName, String vocReference, String domain, String conceptClass, String validDate) {
        List<OBOTerm> terms = OboParser.parse(oboFilename);
        Concept vocabularyConcept = createVocabulary(vocId, vocName, vocReference);
        Map<String, Concept> concepts = new HashMap<>(terms.size());
        terms.forEach(term -> {
            Concept concept = Concept.builder()
                    .conceptId(IDGenerator.generateId())
                    .conceptName(term.getName())
                    .conceptCode(term.getCode())
                    .domainId(domain)
                    .vocabularyId(vocId)
                    .conceptClassId(conceptClass)
                    .standardConcept(term.isObsolete() ? "" : "S")
                    .invalidReason(term.isObsolete() ? "D" : "")
                    .validEndDate(term.isObsolete() ? DateParser.parse(validDate) : DateParser.VALID_END_DATE)
                    .parents(term.getParents())
                    .synonyms(term.getSynonyms())
                    .build();
            cdm.addConcept(concept);
            concepts.put(concept.getConceptCode(), concept);
            cdm.addSynonyms(concept);
        });

        /*
         * generate the ancestors
         * first, populate the children for the concepts
         */

        concepts.values().stream()
                .filter(concept -> concept.getParents() != null && !concept.getParents().isEmpty())
                .forEach(concept -> concept.getParents()
                        .forEach(parent -> concepts.get(parent).addChild(concept.getConceptCode())));

        /* get the roots */
        List<Concept> roots = concepts.values().stream()
                .filter(concept -> concept.getParents() == null || concept.getParents().isEmpty()).toList();

        /* connect the roots to the vocabulary concept */
        roots.forEach(concept -> vocabularyConcept.addChild(concept.getConceptCode()));

        Set<String> exists = new HashSet<>();
        concepts.put(vocabularyConcept.getConceptCode(), vocabularyConcept);
        if (vocabularyConcept.getChildren() != null) {
            vocabularyConcept.getChildren().forEach(concept -> addAncestorTree(cdm, vocabularyConcept,
                    concepts.get(concept), 1, exists, concepts));
        }
    }

    private Concept createVocabulary(String vocId, String vocName, String vocReference) {
        Vocabulary vocabularyOld = cdm.getVocabularyById(vocId);
        Integer offset;
        if (vocabularyOld != null) {
            offset = vocabularyOld.vocabularyConceptId();
            cdm.clear(vocId);
        } else {
            offset = cdm.getNewVocabOffset();
        }

        Vocabulary vocabulary = new Vocabulary(vocId, vocName, vocReference, "1.0", offset);
        cdm.addVocabulary(vocabulary);

        Concept vocConcept = Concept.builder()
                .conceptId(IDGenerator.generateId())
                .conceptName(vocabulary.vocabularyName())
                .conceptCode("OMOP generated")
                .domainId("Metadata")
                .vocabularyId(vocId)
                .conceptClassId("Vocabulary")
                .build();

        cdm.addConcept(vocConcept);
        logger.info("Loading {}, with id offset {}", vocId, offset);
        return vocConcept;
    }

}
