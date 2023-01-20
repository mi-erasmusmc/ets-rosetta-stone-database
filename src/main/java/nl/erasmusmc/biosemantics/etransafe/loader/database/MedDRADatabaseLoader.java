package nl.erasmusmc.biosemantics.etransafe.loader.database;

import nl.erasmusmc.biosemantics.etransafe.repo.MedDRA;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static org.apache.commons.lang3.SystemUtils.USER_DIR;

public class MedDRADatabaseLoader implements DatabaseLoader {

    static final Logger logger = LogManager.getLogger();
    private final MedDRA db;

    public MedDRADatabaseLoader(MedDRA db) {
        this.db = db;
    }


    public void loadDb() {
        executeSQLFile(db, "schema");
        loadData();
        executeSQLFile(db, "indexes");
    }


    private void loadData() {
        String basePath = USER_DIR + "/data/meddra/";
        List<String> tables = List.of("SMQ_LIST", "SMQ_CONTENT", "MDHIER");
        tables.forEach(table -> {
            String sql = "load data local infile '" + basePath + table.toLowerCase() + ".asc' INTO TABLE " + table +
                    " FIELDS TERMINATED BY '$' ENCLOSED BY '' ESCAPED BY '\\b' LINES TERMINATED BY '\\n' STARTING BY '';";
            logger.info("Loading {}", table);
            db.executeSQL(sql);
        });
        String sql = "load data local infile '" + basePath + "llt" + ".asc' INTO TABLE " + "LOW_LEVEL_TERM" +
                " FIELDS TERMINATED BY '$' ENCLOSED BY '' ESCAPED BY '\\b' LINES TERMINATED BY '\\n' STARTING BY '';";
        logger.info("Loading {}", "LOW_LEVEL_TERM");
        db.executeSQL(sql);
    }

}
