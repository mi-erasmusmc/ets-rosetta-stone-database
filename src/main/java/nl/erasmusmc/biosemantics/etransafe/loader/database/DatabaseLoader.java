package nl.erasmusmc.biosemantics.etransafe.loader.database;

import nl.erasmusmc.biosemantics.etransafe.parser.SQLParser;
import nl.erasmusmc.biosemantics.etransafe.repo.Database;

import java.util.List;

import static org.apache.commons.lang3.SystemUtils.USER_DIR;

public interface DatabaseLoader {

    void loadDb();

    default void executeSQLFile(Database db, String target) {
        String path = USER_DIR + "/sql/" + db.getName() + "-" + target + ".sql";
        List<String> queries = SQLParser.loadQueries(path);
        queries.forEach(db::executeSQL);
    }

}
