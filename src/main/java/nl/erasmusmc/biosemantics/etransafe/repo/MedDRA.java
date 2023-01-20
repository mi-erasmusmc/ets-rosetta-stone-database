package nl.erasmusmc.biosemantics.etransafe.repo;

import nl.erasmusmc.biosemantics.etransafe.domain.Concept;
import nl.erasmusmc.biosemantics.etransafe.domain.MeddraHierarchy;
import nl.erasmusmc.biosemantics.etransafe.exception.DBConstructionFailureException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

public class MedDRA implements Database {
    private final String server;
    private final String db;
    private final String user;
    private final String password;
    private final Logger logger = LogManager.getLogger();
    private Connection connection;

    public MedDRA(String server, String db, String user, String password) {
        this.server = server;
        this.db = db;
        this.user = user;
        this.password = password;
    }

    private Connection getConnection() {
        if (connection == null) {
            try {
                int port = 3306;
                String connectionString = "jdbc:mysql://" + server + ":" + port + "/" + db + "?user=" + user + "&password=" + password + DB_CONNECTION_PARAMS;
                connection = DriverManager.getConnection(connectionString);
                return connection;
            } catch (SQLException e) {
                logger.error("Error connecting to MedDRA db: {}", e.getMessage());
                throw new DBConstructionFailureException(e);
            }
        }
        return connection;
    }

    public void executeSQL(String sql) {
        try (Statement statement = getConnection().createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            logger.error(e);
            throw new DBConstructionFailureException(e);
        }
    }

    public List<Concept> getSMQs() {
        List<Concept> concepts = new ArrayList<>(300);

        String sql = """
                SELECT DISTINCT c.smq_code + 666000000 AS concept_id,
                trim(TRAILING ' (SMQ)' FROM c.smq_name) AS concept_name,
                c.smq_code AS concept_code FROM SMQ_LIST c;
                """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Concept concept = conceptFromResultSet(rs);
                concepts.add(concept);
            }

        } catch (SQLException e) {
            logger.error(e);
        }
        return concepts;
    }

    private Concept conceptFromResultSet(ResultSet rs) throws SQLException {
        return Concept.builder()
                .conceptId(rs.getInt("concept_id"))
                .conceptName(rs.getString("concept_name"))
                .conceptCode(rs.getString("concept_code"))
                .domainId("Condition")
                .vocabularyId("MedDRA")
                .standardConcept("S")
                .conceptClassId("SMQ")
                .build();
    }

    public List<MeddraHierarchy> getPrimaryPath() {
        List<MeddraHierarchy> result = new ArrayList<>(25592);
        String sql = """
                SELECT pt_code, hlt_code, hlgt_code, soc_code
                FROM MDHIER
                where primary_soc_fg = 'Y'
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                var hierarchy = new MeddraHierarchy(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4));
                result.add(hierarchy);
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return result;
    }


    public Set<Integer> getTermCodesForSMQ(Integer smq) {

        String sql = "SELECT DISTINCT term_code AS concept_code " +
                "FROM SMQ_CONTENT c WHERE smq_code = ? AND term_level != 0;";

        Set<Integer> conceptCodes = getCodes(smq, sql);
        if (!isEmpty(conceptCodes)) {
            return conceptCodes;
        }
        return getTermCodesForHighLevelSMQ(smq);
    }

    private Set<Integer> getTermCodesForHighLevelSMQ(Integer smq) {
        String sql = "SELECT DISTINCT term_code AS concept_code " +
                "FROM SMQ_CONTENT c WHERE smq_code = ? AND term_level = 0;";

        return getCodes(smq, sql).stream()
                .map(this::getTermCodesForSMQ)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private Set<Integer> getCodes(Integer smq, String sql) {
        Set<Integer> codes = new HashSet<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, smq);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                codes.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return codes;
    }


    @Override
    public String getName() {
        return "meddra";
    }

}

