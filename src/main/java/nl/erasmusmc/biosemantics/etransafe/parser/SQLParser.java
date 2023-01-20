package nl.erasmusmc.biosemantics.etransafe.parser;

import nl.erasmusmc.biosemantics.etransafe.exception.DBConstructionFailureException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class SQLParser {

    private static final Logger logger = LogManager.getLogger();


    private SQLParser() {
    }

    public static List<String> loadQueries(String path) {
        try {
            logger.info("Loading {}", path);
            Path filePath = Path.of(path);
            String[] queries = Files.readString(filePath).trim().split(";");
            return Arrays.stream(queries).map(String::trim).filter(q -> !q.isEmpty()).toList();
        } catch (IOException e) {
            throw new DBConstructionFailureException(e);
        }
    }
}
