package nl.erasmusmc.biosemantics.etransafe.repo;

import nl.erasmusmc.biosemantics.etransafe.domain.Concept;
import nl.erasmusmc.biosemantics.etransafe.domain.Vocabulary;
import nl.erasmusmc.biosemantics.etransafe.exception.DBConstructionFailureException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static nl.erasmusmc.biosemantics.etransafe.parser.DateParser.NOW;
import static nl.erasmusmc.biosemantics.etransafe.parser.DateParser.VALID_END_DATE;


public class CDM implements Database {
    private static final Logger logger = LogManager.getLogger();
    private static final int DELTA = 10000000;
    private final String server;
    private final String db;
    private final String user;
    private final String password;
    private Integer englishConcept;
    private Connection connection = null;


    public CDM(String aServer, String aDatabase, String aUser, String aPassword) {
        this.server = aServer;
        this.db = aDatabase;
        this.user = aUser;
        this.password = aPassword;
    }


    /*
     * this code tries to find the first slot of 10000000 that has no concepts allotted
     */
    public Integer getNewVocabOffset() {
        for (int i = 6; i < 100; i++) {
            try (PreparedStatement statement = getConnection().prepareStatement("SELECT COUNT(*) FROM concept WHERE concept_id >= ? AND concept_id < ?")) {
                statement.setInt(1, i * DELTA);
                statement.setInt(2, (i + 1) * DELTA);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    if (count == 0) {
                        return i * DELTA;
                    }
                }
            } catch (SQLException e) {
                logger.error(e);
            }
        }
        return null;
    }

    private Connection getConnection() {
        try {
            if (connection == null) {
                connection = DriverManager.getConnection(String.format("jdbc:mysql://%s/%s?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&allowLoadLocalInfile=true&serverTimezone=UTC&user=%s&password=%s&useSSL=false&verifyServerCertificate=false&allowPublicKeyRetrieval=true&allowMultiQueries=true", server, db, user, password));
            }
        } catch (Exception e) {
            logger.error(e);
            throw new DBConstructionFailureException(e);
        }
        return connection;
    }

    public void clear(String vocId) {
        try (Statement statement = getConnection().createStatement()) {
            logger.debug("Deleting vocabulary {}", vocId);
            Integer minConceptId = getMinConceptByVoc(vocId);
            Integer maxConceptId = getMaxConceptByVoc(vocId);
            statement.executeUpdate("delete from vocabulary where vocabulary_id = \"" + vocId + "\"");
            if (minConceptId != null && maxConceptId != null) {
                statement.executeUpdate("delete from concept where concept_id >= " + minConceptId + " AND concept_id <= " + maxConceptId);
                statement.executeUpdate("delete from concept_synonym where concept_id >= " + minConceptId + " AND concept_id <= " + maxConceptId);
                statement.executeUpdate("delete from concept_ancestor where ancestor_concept_id >= " + minConceptId + " AND ancestor_concept_id <= " + maxConceptId);
                statement.executeUpdate("delete from concept_relationship where concept_id_1 >= " + minConceptId + " AND concept_id_1 <= " + maxConceptId);
                statement.executeUpdate("delete from concept_relationship where concept_id_2 >= " + minConceptId + " AND concept_id_2 <= " + maxConceptId);
            }
            logger.info("Cleared {}", vocId);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public Concept getConceptByName(String name) {
        try (PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM concept WHERE concept_name = ?")) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return conceptFromResultSet(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DBConstructionFailureException(e);
        }
    }

    public Integer getMinConceptByVoc(String vocId) {
        Integer result = null;
        try (PreparedStatement statement = getConnection().prepareStatement("SELECT MIN(concept_id) FROM concept WHERE vocabulary_id = ?")) {
            statement.setString(1, vocId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getInt(1);
                if (result == 0) {
                    result = null;
                }
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return result;
    }

    public Integer getMaxConceptByVoc(String vocId) {
        Integer result = null;
        try (PreparedStatement statement = getConnection().prepareStatement("SELECT MAX(concept_id) FROM concept WHERE vocabulary_id = ?")) {
            statement.setString(1, vocId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getInt(1);
                if (result == 0) {
                    result = null;
                }
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return result;
    }

    public void addVocabulary(Vocabulary vocabulary) {
        try (PreparedStatement statement = getConnection().prepareStatement("INSERT INTO vocabulary (vocabulary_id, vocabulary_name, vocabulary_reference, vocabulary_version, vocabulary_concept_id) VALUES (?, ?, ?, ?, ?)")) {
            statement.setString(1, vocabulary.vocabularyId());
            statement.setString(2, vocabulary.vocabularyName());
            statement.setString(3, vocabulary.vocabularyReference());
            statement.setString(4, vocabulary.vocabularyVersion());
            statement.setInt(5, vocabulary.vocabularyConceptId());
            statement.executeUpdate();
            Integer conceptInitial = vocabulary.vocabularyConceptId();
            IDGenerator.setId(conceptInitial);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public void addConcept(Concept concept) {
        try (PreparedStatement statement = getConnection().prepareStatement("""
                INSERT INTO concept (concept_id, concept_name, domain_id, vocabulary_id, concept_class_id,
                standard_concept, concept_code, valid_start_date, valid_end_date, invalid_reason)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """)) {
            statement.setInt(1, concept.getConceptId());
            statement.setString(2, concept.getConceptName());
            statement.setString(3, concept.getDomainId());
            statement.setString(4, concept.getVocabularyId());
            statement.setString(5, concept.getConceptClassId());
            statement.setString(6, concept.getStandardConcept());
            statement.setString(7, concept.getConceptCode());
            statement.setDate(8, concept.getValidStartDate());
            statement.setDate(9, concept.getValidEndDate());
            statement.setString(10, concept.getInvalidReason());
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public void addConceptAncestor(Integer ancestorConceptId, Integer descendantConceptId, int minLevelsOfSeparation, int maxLevelsOfSeparation) {
        try (PreparedStatement statement = getConnection().prepareStatement("INSERT INTO concept_ancestor (ancestor_concept_id, descendant_concept_id, min_levels_of_separation, max_levels_of_separation) VALUES (?, ?, ?, ?)")) {
            statement.setInt(1, ancestorConceptId);
            statement.setInt(2, descendantConceptId);
            statement.setInt(3, minLevelsOfSeparation);
            statement.setInt(4, maxLevelsOfSeparation);
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public Concept getConceptByCode(String sourceCode, String vocabularyId) {
        try (PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM concept WHERE concept_code = ? AND vocabulary_id = ?")) {
            statement.setString(1, sourceCode);
            statement.setString(2, vocabularyId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return conceptFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Concept getConceptByCodeAndVocabulary(String sourceCode, String vocId) {
        try (PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM concept WHERE concept_code = ? AND vocabulary_id = ?;")) {
            statement.setString(1, sourceCode);
            statement.setString(2, vocId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return conceptFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return null;
    }


    public void addMapping(Concept sourceConcept, Concept targetConcept, String relation, String reverseRelation, String reason, String source) {
        addMapping(sourceConcept, targetConcept, relation, reason, source);
        // reverse
        if (reverseRelation != null) {
            addMapping(targetConcept, sourceConcept, reverseRelation, reason, source);
        }
    }

    private void addMapping(Concept sourceConcept, Concept targetConcept, String relation, String reason, String source) {
        try (PreparedStatement statement = getConnection().prepareStatement("INSERT INTO concept_relationship (concept_id_1, concept_id_2, relationship_id, valid_start_date, valid_end_date, invalid_reason, source) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            statement.setInt(1, sourceConcept.getConceptId());
            statement.setInt(2, targetConcept.getConceptId());
            statement.setString(3, relation);
            statement.setDate(4, NOW);
            statement.setDate(5, VALID_END_DATE);
            statement.setString(6, reason);
            statement.setString(7, source);
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public Vocabulary getVocabularyById(String vocabularyId) {
        try (PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM vocabulary WHERE vocabulary_id = ?")) {
            statement.setString(1, vocabularyId);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new Vocabulary(resultSet.getString("vocabulary_id"),
                        resultSet.getString("vocabulary_name"),
                        resultSet.getString("vocabulary_reference"),
                        resultSet.getString("vocabulary_version"),
                        resultSet.getInt("vocabulary_concept_id"));
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return null;
    }

    public void executeSQL(String sql) {
        try (Statement statement = getConnection().createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            logger.error(e);
            throw new DBConstructionFailureException(e);
        }
    }

    @Override
    public String getName() {
        return "cdm";
    }

    public void deleteWrongAncestors() {
        logger.info("Deleting entries from ancestor table that are from different vocabularies, takes about an hour");
        String selectStmt = """
                SELECT c1.concept_id anc, c2.concept_id AS des FROM concept c1
                JOIN concept_ancestor a ON a.ancestor_concept_id = c1.concept_id
                JOIN concept c2 ON a.descendant_concept_id = c2.concept_id
                WHERE c1.vocabulary_id != c2.vocabulary_id;
                """;

        String deleteStmt = "DELETE FROM concept_ancestor WHERE ancestor_concept_id = ? AND descendant_concept_id = ?;";

        try (PreparedStatement stmt = getConnection().prepareStatement(selectStmt)) {
            ResultSet rs = stmt.executeQuery();
            logger.info("Fetched deletable ancestor entries");
            long counter = 1;
            try (PreparedStatement delStmt = getConnection().prepareStatement(deleteStmt)) {
                while (rs.next()) {
                    counter++;
                    delStmt.setInt(1, rs.getInt(1));
                    delStmt.setInt(2, rs.getInt(2));
                    delStmt.addBatch();
                    if (counter % 5000 == 0) {
                        delStmt.executeBatch();
                        logger.info("Deleted {} out of approx. 2 million entries", counter);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteMappingByVocAndSource(String voc1, String voc2, String source) {
        logger.info("Deleting old {} <-> {} Mappings from {}", voc1, voc2, source);
        deleteMappingByVocAndSources(voc1, voc2, source);
        deleteMappingByVocAndSources(voc2, voc1, source);
    }


    private void deleteMappingByVocAndSources(String sourceVocId, String targetVocId, String source) {
        try (PreparedStatement statement = getConnection().prepareStatement("""
                DELETE FROM concept_relationship
                WHERE source = ?
                AND concept_id_1 IN (SELECT concept_id FROM concept WHERE vocabulary_id = ?)
                AND concept_id_2 IN (SELECT concept_id FROM concept WHERE vocabulary_id = ?)
                """)) {
            statement.setString(1, source);
            statement.setString(2, sourceVocId);
            statement.setString(3, targetVocId);
            statement.executeUpdate();

            statement.setString(1, source);
            statement.setString(2, targetVocId);
            statement.setString(3, sourceVocId);
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public void addSynonyms(Concept concept) {
        if (concept.getSynonyms() != null) {
            concept.getSynonyms().forEach(synonym -> {
                try (PreparedStatement statement = getConnection().prepareStatement("INSERT INTO concept_synonym (concept_id, concept_synonym_name, language_concept_id) VALUES (?, ?, ?)")) {
                    statement.setInt(1, concept.getConceptId());
                    statement.setString(2, synonym.trim());
                    statement.setInt(3, getEnglishConceptId());
                    statement.executeUpdate();
                } catch (SQLException e) {
                    logger.error(e);
                }
            });
        }
    }

    private int getEnglishConceptId() {
        if (englishConcept == null) {
            englishConcept = getConceptByName("English language").getConceptId();
        }
        return englishConcept;
    }

    private Concept conceptFromResultSet(ResultSet resultSet) throws SQLException {
        return Concept.builder()
                .conceptId(resultSet.getInt("concept_id"))
                .conceptCode(resultSet.getString("concept_code"))
                .conceptName(resultSet.getString("concept_name"))
                .domainId(resultSet.getString("domain_id"))
                .standardConcept(resultSet.getString("standard_concept"))
                .vocabularyId(resultSet.getString("vocabulary_id"))
                .conceptClassId(resultSet.getString("concept_class_id"))
                .invalidReason(resultSet.getString("invalid_reason"))
                .build();
    }

    public void cleanConceptRelationShipTable() {
        dedupe();
        indexRel();
    }

    public void dedupe() {
        String sql1 = """
                CREATE TABLE concept_relationship_clean AS
                SELECT ROW_NUMBER() OVER ( ORDER BY concept_id_1 ) AS id,
                concept_id_1, concept_id_2, relationship_id, MIN(valid_start_date) AS valid_start_date, MAX(valid_end_date) AS valid_end_date, invalid_reason, source
                FROM concept_relationship
                GROUP BY concept_id_1, concept_id_2, relationship_id, invalid_reason, source
                """;
        String sql2 = "DROP TABLE concept_relationship;";
        String sql3 = "RENAME TABLE concept_relationship_clean TO concept_relationship;";
        try (Statement stmt = getConnection().createStatement()) {
            logger.info("Creating new deduplicated concept relationship table");
            stmt.executeUpdate(sql1);
            logger.info("Removing old concept relationship table");
            stmt.executeUpdate(sql2);
            stmt.executeUpdate(sql3);
        } catch (SQLException e) {
            throw new DBConstructionFailureException(e);
        }
    }


    public void deleteCircularRelationShip() {
        logger.info("Deleting circular relationships");
        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate("DELETE FROM concept_relationship WHERE concept_id_1 = concept_id_2");
        } catch (SQLException e) {
            throw new DBConstructionFailureException(e);
        }
    }

    public void indexRel() {
        logger.info("Setting indexes on concept relationship table");
        String sql = """             
                CREATE INDEX concept_id_1
                    ON concept_relationship (concept_id_1);
                                
                CREATE INDEX concept_id_2
                    ON concept_relationship (concept_id_2);
                                
                CREATE INDEX relationship_id
                    ON concept_relationship (relationship_id);
                                
                CREATE INDEX source
                    ON concept_relationship (source);
                    """;
        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new DBConstructionFailureException(e);
        }
    }

    public Map<String, Concept> allConceptForCodeList(String vocId) {
        Map<String, Concept> codes = new HashMap<>(500);
        var sql = "SELECT DISTINCT * FROM concept WHERE vocabulary_id = ? AND domain_id != 'Metadata' AND valid_end_date = '2099-12-31'";
        try (var stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, vocId);
            var rs = stmt.executeQuery();
            while (rs.next()) {
                codes.put(rs.getString("concept_code"), conceptFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DBConstructionFailureException(e);
        }
        return codes;
    }
}
