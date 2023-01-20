package nl.erasmusmc.biosemantics.etransafe.loader.vocab;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import nl.erasmusmc.biosemantics.etransafe.domain.Concept;
import nl.erasmusmc.biosemantics.etransafe.domain.Vocabulary;
import nl.erasmusmc.biosemantics.etransafe.exception.DBConstructionFailureException;
import nl.erasmusmc.biosemantics.etransafe.repo.CDM;
import nl.erasmusmc.biosemantics.etransafe.repo.IDGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;


public class SENDListVocabLoader implements VocabLoader {

    private static final String OBSERVATION = "Observation";
    private static final Map<String, CodeList> CODE_LISTS_TO_LOAD = Map.of(
            "Specimen", new CodeList("Specimen", "Spec Anatomic Site", "Body Structure"),
            "Neoplasm Type", new CodeList("Neoplasm Type", OBSERVATION, "Morph Abnormality"),
            "Non-Neoplastic Finding Type", new CodeList("Non-Neoplastic Finding Type", OBSERVATION, "Morph Abnormality"),
            "Laboratory Test Name", new CodeList("Laboratory Test Name", OBSERVATION, "Lab Test")
    );
    private static final Logger logger = LogManager.getLogger();
    private final CSVParser parser = new CSVParserBuilder().withSeparator('\t').build();
    private final CDM cdm;


    public SENDListVocabLoader(CDM cdm) {
        this.cdm = cdm;
    }


    public void loadVocab() {
        var release = releaseDate();
        logger.info("Loading {}", release);
        try (var in = new BufferedInputStream(new URL("https://evs.nci.nih.gov/ftp1/CDISC/SEND/SEND%20Terminology.txt").openStream());
             var csvReader = new CSVReaderBuilder(new InputStreamReader(in, StandardCharsets.UTF_8)).withCSVParser(parser).build()) {
            csvReader.skip(1); /* skip header */

            String[] values;
            while ((values = csvReader.readNext()) != null) {
                var currentSendCodeList = values[3];
                var codeListMetaData = CODE_LISTS_TO_LOAD.get(currentSendCodeList);
                if (codeListMetaData != null) {
                    String code = values[1];
                    if (code.isEmpty()) {
                        var vocId = codeListMetaData.vocabularyId;
                        var vocabularyOld = cdm.getVocabularyById(codeListMetaData.vocabularyId);
                        var offset = vocabularyOld != null ? vocabularyOld.vocabularyConceptId() : cdm.getNewVocabOffset();
                        cdm.clear(vocId);
                        addVocabulary(release, codeListMetaData.vocabularyId, offset, values);
                    } else {
                        addConcept(codeListMetaData.domain, codeListMetaData.conceptClass, codeListMetaData.vocabularyId, values);
                    }
                }

            }
        } catch (IOException | CsvValidationException e) {
            logger.error("Error loading SEND");
            e.printStackTrace();
            throw new DBConstructionFailureException(e);
        }
    }

    private void addConcept(String domain, String conceptClass, String vocabularyId, String[] values) {
        var conceptCode = values[0];
        var conceptName = values[4];
        var synonyms = Arrays.stream(values[5].split(";")).map(String::trim).toList();

        var concept = Concept.builder()
                .conceptId(IDGenerator.generateId())
                .conceptName(conceptName)
                .conceptCode(conceptCode)
                .domainId(domain)
                .vocabularyId(vocabularyId)
                .conceptClassId(conceptClass)
                .standardConcept("S")
                .synonyms(synonyms)
                .build();

        cdm.addConcept(concept);
        cdm.addSynonyms(concept);
    }

    private void addVocabulary(String sendVersion, String vocabularyId, Integer offset, String[] values) {
        logger.info("Adding {}", vocabularyId);
        var vocabulary = new Vocabulary(vocabularyId, values[7], values[6], sendVersion, offset);
        cdm.addVocabulary(vocabulary);
        var vocConcept = Concept.builder()
                .conceptId(IDGenerator.generateId())
                .conceptName(vocabulary.vocabularyName())
                .conceptCode(values[0])
                .domainId("Metadata")
                .vocabularyId(vocabularyId)
                .conceptClassId("Vocabulary")
                .build();
        cdm.addConcept(vocConcept);
    }

    private String releaseDate() {
        try (var in = new BufferedInputStream(new URL("https://evs.nci.nih.gov/ftp1/CDISC/SEND/SEND%20Publication%20Date%20Stamp.txt").openStream());
             var csvReader = new CSVReaderBuilder(new InputStreamReader(in, StandardCharsets.UTF_8)).withCSVParser(parser).build()) {
            csvReader.skip(1); /* skip header */
            var data = csvReader.readNext();
            return data[2];
        } catch (IOException | CsvValidationException e) {
            logger.error("Failed to load SEND release date");
            e.printStackTrace();
            throw new DBConstructionFailureException(e);
        }
    }


    static class CodeList {

        final String vocabularyId;
        final String domain;
        final String conceptClass;

        public CodeList(String codeListName, String domain, String conceptClass) {
            this.vocabularyId = codeListName.length() > 20 ? codeListName.substring(0, 20) : codeListName;
            this.domain = domain;
            this.conceptClass = conceptClass;
        }
    }
}
