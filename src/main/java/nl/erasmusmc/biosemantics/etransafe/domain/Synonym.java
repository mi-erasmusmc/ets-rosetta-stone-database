package nl.erasmusmc.biosemantics.etransafe.domain;

import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class Synonym {

    private static final Pattern pattern = Pattern.compile("\"([^\"]*)\"\\s([A-Z]+)\\s\\[([A-Z]*)]");
    private String term;
    private boolean exact;
    private String source;

    public Synonym(String line) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            this.term = matcher.group(1);
            this.exact = matcher.group(2).equalsIgnoreCase("EXACT");
            this.source = matcher.group(3);
        }
    }

}
