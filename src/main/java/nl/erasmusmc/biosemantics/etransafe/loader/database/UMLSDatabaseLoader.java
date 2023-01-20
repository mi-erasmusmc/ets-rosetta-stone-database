package nl.erasmusmc.biosemantics.etransafe.loader.database;

import nl.erasmusmc.biosemantics.etransafe.repo.UMLS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.apache.commons.lang3.SystemUtils.USER_DIR;

public class UMLSDatabaseLoader implements DatabaseLoader {

    static final Logger logger = LogManager.getLogger();
    private final UMLS db;

    public UMLSDatabaseLoader(UMLS db) {
        this.db = db;
    }


    public void loadDb() {
        executeSQLFile(db, "schema");
        loadData();
        executeSQLFile(db, "indexes");
    }


    private void loadData() {
        String table = "MRCONSO";
        String basePath = USER_DIR + "/data/umls/";
        String sql = "load data local infile '" + basePath + table + ".RRF' INTO TABLE " + table +
                " FIELDS TERMINATED BY '|' ENCLOSED BY '' ESCAPED BY '\\b' LINES TERMINATED BY '\\n' STARTING BY '';";
        logger.info("Loading {}", table);
        db.executeSQL(sql);
    }

}
