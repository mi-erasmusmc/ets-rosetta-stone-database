package nl.erasmusmc.biosemantics.etransafe.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OBOTerm {

    private static final String SYNONYM = "synonym";
    private final Map<String, List<String>> attributes = new HashMap<>();


    public String getName() {
        return attributes.get("name").get(0);
    }

    public String getCode() {
        return attributes.get("id").get(0);
    }

    public List<String> getSynonyms() {
        return attributes.getOrDefault(SYNONYM, new ArrayList<>());
    }

    public List<String> getParents() {
        return attributes.getOrDefault("is_a", new ArrayList<>());
    }

    public void add(String key, String value) {
        if (key.equalsIgnoreCase(SYNONYM)) {
            Synonym synonym = new Synonym(value);
            if (synonym.getTerm() != null && synonym.isExact()) {
                addAttribute(key, synonym.getTerm());
            }
        } else if (key.equalsIgnoreCase("is_a")) {
            int epos = value.indexOf("!");
            value = (epos != -1 ? value.substring(0, epos) : value).trim();
            addAttribute(key, value);
        } else if (key.equalsIgnoreCase("relationship")) {
            int epos = value.indexOf("!");
            String keyValue = (epos != -1 ? value.substring(0, epos) : value).trim();
            String[] parts = keyValue.split(" ");
            if (parts[0].equalsIgnoreCase("part_of") || parts[0].equalsIgnoreCase("develops_from")) {
                value = parts[1];
                addAttribute(key, value);
            }
        } else {
            addAttribute(key, value);
        }
    }

    private void addAttribute(String key, String value) {
        var v = attributes.computeIfAbsent(key, n -> new ArrayList<>());
        if (!v.contains(value)) {
            v.add(value);
        }
    }


    public boolean isObsolete() {
        return attributes.containsKey("is_obsolete") && Boolean.parseBoolean(attributes.get("is_obsolete").get(0));
    }

}
