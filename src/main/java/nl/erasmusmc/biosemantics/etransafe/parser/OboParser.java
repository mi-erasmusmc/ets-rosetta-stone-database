package nl.erasmusmc.biosemantics.etransafe.parser;

import nl.erasmusmc.biosemantics.etransafe.domain.OBOTerm;
import nl.erasmusmc.biosemantics.etransafe.exception.DBConstructionFailureException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class OboParser {

    private OboParser() {
    }


    public static List<OBOTerm> parse(String fileName) {
        List<OBOTerm> terms = new ArrayList<>(500);
        try (var file = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8))) {
            OBOTerm term;
            while ((term = nextTerm(file)) != null) {
                terms.add(term);
            }
        } catch (IOException e) {
            throw new DBConstructionFailureException(e);
        }
        return terms;
    }

    private static OBOTerm nextTerm(BufferedReader oboFile) {
        String line;
        OBOTerm term = null;
        try {
            while ((line = oboFile.readLine()) != null) {
                if (line.isEmpty() && term != null) {
                    return term;
                }
                if (line.startsWith("[Term]")) {
                    term = new OBOTerm();
                } else if (term != null) {
                    String[] pieces = line.split(":", 2);
                    if (pieces.length == 2) {
                        term.add(pieces[0].trim(), pieces[1].trim());
                    }
                }
            }
        } catch (IOException e) {
            throw new DBConstructionFailureException(e);
        }
        return term;
    }

}
