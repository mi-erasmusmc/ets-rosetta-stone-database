package nl.erasmusmc.biosemantics.etransafe.repo;

import nl.erasmusmc.biosemantics.etransafe.domain.MeddraSnomedPair;
import nl.erasmusmc.biosemantics.etransafe.exception.DBConstructionFailureException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class UMLS implements Database {
    private final String server;
    private final String db;
    private final String user;
    private final String password;
    private final Logger logger = LogManager.getLogger();
    private Connection connection;

    public UMLS(String server, String db, String user, String password) {
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
            } catch (SQLException e) {
                logger.error("Error connecting to UMLS db: {}", e.getMessage());
                throw new DBConstructionFailureException(e);
            }
        }
        return connection;
    }

    public Set<MeddraSnomedPair> getMedDRA2SNOMED() {
        logger.info("Loading mappings from UMLS");
        Set<MeddraSnomedPair> result = new HashSet<>();
        try (PreparedStatement statement = getConnection().prepareStatement("""
                SELECT DISTINCT a.code AS SNOMED, b.code AS MedDRA
                FROM mrconso a, mrconso b
                WHERE a.suppress = 'N' AND a.sab = 'SNOMEDCT_US'
                AND a.cui = b.cui AND a.tty = 'PT'
                AND b.suppress = 'N' AND b.sab = 'MDR'
                """)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                MeddraSnomedPair meddra2snomed = new MeddraSnomedPair();
                meddra2snomed.setSnomedCode(resultSet.getString("SNOMED"));
                meddra2snomed.setMeddraCode(resultSet.getString("MedDRA"));
                result.add(meddra2snomed);
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        logger.info("Loaded {} pairs from UMLS", result.size());
        return result;
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
        return "umls";
    }

}

