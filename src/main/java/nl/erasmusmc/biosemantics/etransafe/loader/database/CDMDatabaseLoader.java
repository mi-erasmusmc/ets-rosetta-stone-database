package nl.erasmusmc.biosemantics.etransafe.loader.database;

import nl.erasmusmc.biosemantics.etransafe.domain.VocabularyTables;
import nl.erasmusmc.biosemantics.etransafe.repo.CDM;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.apache.commons.lang3.SystemUtils.USER_DIR;

public class CDMDatabaseLoader implements DatabaseLoader {

    private static final Logger logger = LogManager.getLogger();
    private final CDM cdm;


    public CDMDatabaseLoader(CDM cdm) {
        this.cdm = cdm;
    }


    public void loadDb() {
        executeSQLFile(cdm, "schema");
        loadVocabularyData();
        deleteFromAncestorWhereEqualsDescendant();
        deleteCircularRelationship();
        executeSQLFile(cdm, "indexes");
        // Load keys with on delete cascade first to delete the unwanted vocabs cascading throughout
        logger.info("Loading the keys takes a fair amount of time");
        executeSQLFile(cdm, "keys");
        deleteUnwantedVocabs();
        deleteSnomedMeddraAncestorLinks();
        logger.info("CDM is ready to go");
    }

    private void deleteCircularRelationship() {
        cdm.deleteCircularRelationShip();
    }


    private void deleteUnwantedVocabs() {
        logger.info("Going to delete stuff from CDM we don't need, to reduce DB bloat");
        logger.info("Deleting 'OSM', 'UB04 Typ bill' and 'UCUM' vocabularies");
        String delConceptSQL = "DELETE FROM concept WHERE vocabulary_id in ('OSM','UCUM','UB04 Typ bill', 'SOPT')";
        cdm.executeSQL(delConceptSQL);
        logger.info("Deleting the 'DRUG' and 'Race' domains");
        String delDomainSQL = "DELETE FROM concept WHERE domain_id in ('DRUG','Race')";
        cdm.executeSQL(delDomainSQL);
    }


    private void loadVocabularyData() {
        String basePath = USER_DIR + "/data/vocabulary/";
        for (VocabularyTables table : VocabularyTables.values()) {
            String sql = "load data local infile '" + basePath + table +
                    ".csv' INTO TABLE " + table.toString().toLowerCase() +
                    " FIELDS TERMINATED BY '\\t' ENCLOSED BY '' ESCAPED BY '\\b' LINES TERMINATED BY '\\n' STARTING BY '' ignore 1 lines;";
            logger.info("Loading {}", table);
            cdm.executeSQL(sql);
        }
        String addId = "ALTER TABLE concept_relationship ADD id INT PRIMARY KEY AUTO_INCREMENT";
        cdm.executeSQL(addId);
    }


    // The concept_ancestor contains snomed meddra references, this messes up the 'expand' endpoint in the semantic service, so we remove them
    private void deleteSnomedMeddraAncestorLinks() {
        cdm.deleteWrongAncestors();
    }

    private void deleteFromAncestorWhereEqualsDescendant() {
        logger.info("Deleting entries from concept_ancestor table where concept and ancestor are the same");
        String sql = "DELETE FROM concept_ancestor WHERE ancestor_concept_id = descendant_concept_id;";
        logger.info(sql);
        cdm.executeSQL(sql);
    }
}
