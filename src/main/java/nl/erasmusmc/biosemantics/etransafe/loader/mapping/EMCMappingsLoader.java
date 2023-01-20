package nl.erasmusmc.biosemantics.etransafe.loader.mapping;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import nl.erasmusmc.biosemantics.etransafe.domain.Concept;
import nl.erasmusmc.biosemantics.etransafe.exception.DBConstructionFailureException;
import nl.erasmusmc.biosemantics.etransafe.repo.CDM;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.SystemUtils.USER_DIR;

public class EMCMappingsLoader implements MappingLoader {

    private static final String SNOMED = "SNOMED";
    private static final String MAPPING_REPO = "https://raw.githubusercontent.com/mi-erasmusmc/send-snomed-mappings/master";
    private static final String SOURCE = "eTRANSAFE";
    private static final Logger logger = LogManager.getLogger();
    private final boolean eToxMA;
    private final CDM cdm;

    public EMCMappingsLoader(CDM cdm, boolean eToxMA) {
        this.cdm = cdm;
        this.eToxMA = eToxMA;
    }

    public void loadMappings() {
        processSSSOMMappingsFromRepo("Neoplasm Type", "/neoplasm/neoplasm_snomed.tsv");
        processSSSOMMappingsFromRepo("Laboratory Test Name", "/lab_test_name/labtestname_snomed.tsv");
        processSSSOMMappingsFromRepo("Specimen", "/specimen/specimen_snomed.tsv");
        processSSSOMMappingsFromRepo("Non-Neoplastic Findi", "/non-neoplastic/nonneo_snomed.tsv");
        processSSSOMMappingsFromRepo("HPATH", "/histopathology/hpath_snomed.tsv");
        if (eToxMA) {
            processSSSOMMappingsFromLocal("MA", "/data/mappings/sssom_etox_ma_snomed.tsv");
        } else {
            processSSSOMMappingsFromRepo("MA", "/mouse_anatomy/ma_snomed.tsv");
        }
    }

    private void processSSSOMMappingsFromLocal(String vocId, String path) {
        File file = new File(USER_DIR + path);
        logger.info("Loading mappings for {} from {}", vocId, path);
        CSVParser parser = new CSVParserBuilder().withSeparator('\t').build();
        try (var in = new FileInputStream(file);
             var csvReader = new CSVReaderBuilder(new InputStreamReader(in, StandardCharsets.UTF_8)).withCSVParser(parser).build()) {
            csvReader.skip(1); /* skip header */
            processTSV(vocId, csvReader);
        } catch (IOException | CsvValidationException e) {
            throw new DBConstructionFailureException(e);
        }
    }

    private void processSSSOMMappingsFromRepo(String vocId, String path) {
        var url = MAPPING_REPO + path;
        logger.info("Loading mappings for {} from {}", vocId, url);
        CSVParser parser = new CSVParserBuilder().withSeparator('\t').build();
        try (var in = new BufferedInputStream(new URL(url).openStream());
             var csvReader = new CSVReaderBuilder(new InputStreamReader(in, StandardCharsets.UTF_8)).withCSVParser(parser).build()) {
            csvReader.skip(1); /* skip header */
            processTSV(vocId, csvReader);
        } catch (IOException | CsvValidationException e) {
            throw new DBConstructionFailureException(e);
        }
    }

    private void processTSV(String vocId, CSVReader csvReader) throws IOException, CsvValidationException {
        String[] data;
        int unmapped = 0;
        Set<String> codesInMappingFile = new HashSet<>(500);
        List<SSSOMMapping> mappings = new ArrayList<>(500);
        while ((data = csvReader.readNext()) != null) {
            codesInMappingFile.add(data[0]);
            if (!data[6].equalsIgnoreCase("notdone")) {
                mappings.add(new SSSOMMapping(data[0], data[1], data[3], data[4], data[2], data[8]));
            } else {
                unmapped += 1;
                logger.debug("{} ({}) has not been mapped, consider mapping it", data[1], data[0]);
            }
        }
        var codesInDb = cdm.allConceptForCodeList(vocId);
        validate(codesInMappingFile, codesInDb.keySet(), unmapped);
        cdm.deleteMappingByVocAndSource(vocId, SNOMED, SOURCE);
        persistsMappings(mappings, codesInDb);
        logger.info("Done with {} mappings", vocId);
    }

    private void persistsMappings(List<SSSOMMapping> mappings, Map<String, Concept> conceptMap) {
        logger.info("Persisting new mappings");
        mappings.forEach(mapping -> {
            var sourceConcept = conceptMap.get(mapping.sourceCode);
            var groupId = mapping.sourceCode + "-" + mapping.groupId;
            var targetConcept = getValidTarget(mapping);
            cdm.addMapping(sourceConcept, targetConcept, mapping.relation, mapping.reverseRelation, groupId, SOURCE);
        });
    }


    private Concept getValidTarget(SSSOMMapping mapping) {
        var targetConcept = cdm.getConceptByCodeAndVocabulary(mapping.targetCode, SNOMED);
        var info = mapping.targetName + " " + mapping.targetCode;
        if (targetConcept == null) {
            logger.error("Target concept not found for {}", info);
        } else if (targetConcept.getInvalidReason() != null && !targetConcept.getInvalidReason().isBlank()) {
            logger.error("Target concept is no longer valid remap {}", info);
        } else if (!targetConcept.getConceptName().equals(mapping.targetName)) {
            logger.error("Inconsistency between name in mapping file and name in db. {} does not match {}", targetConcept.getConceptName(), info);
        }
        return targetConcept;
    }


    private void validate(Set<String> codesFromMapping, Set<String> codesInDb, int unmappedCount) {
        var match = codesInDb.equals(codesFromMapping);
        if (!match) {
            logger.error("{} concepts in mapping file", codesFromMapping.size());
            logger.error("{} concepts loaded into the database", codesInDb.size());
            throw new DBConstructionFailureException("""
                    The mapping is not up to date with the latest send,
                    add missing codes, can label as unmapped if you don't want to do mappings now,
                    or disable this code if you don't give a care.
                    """);
        } else {
            var u = unmappedCount > 0 ? unmappedCount + " have not been mapped" : "and have been mapped";
            logger.info("All {} terms are accounted for {}", codesFromMapping.size(), u);
        }
    }

    static class SSSOMMapping {

        final String sourceCode;
        final String sourceName;
        final String targetCode;
        final String targetName;
        final String relation;
        final String reverseRelation;
        int groupId;

        public SSSOMMapping(String sourceCode, String sourceName, String targetCode, String targetName, String relation, String groupId) {
            var skos = relation.split(":")[1];
            this.sourceCode = sourceCode;
            this.sourceName = sourceName;
            this.targetCode = targetCode;
            this.targetName = targetName;
            this.relation = skosToOMOP(skos);
            this.reverseRelation = skosToInverted(skos);
            this.groupId = Integer.parseInt(groupId);
        }

        private static String skosToOMOP(String skos) {
            return switch (skos) {
                case "exactMatch" -> "Exact Match";
                case "narrowMatch" -> "Narrow Match";
                case "broadMatch" -> "Broad Match";
                case "relatedMatch" -> "Related Match";
                default -> throw new DBConstructionFailureException("Unknown skos term: " + skos);
            };
        }

        private static String skosToInverted(String skos) {
            return switch (skos) {
                case "exactMatch" -> "Exact Match";
                case "narrowMatch" -> "Broad Match";
                case "broadMatch" -> "Narrow Match";
                case "relatedMatch" -> "Related Match";
                default -> throw new DBConstructionFailureException("Unknown skos term: " + skos);
            };
        }
    }
}
