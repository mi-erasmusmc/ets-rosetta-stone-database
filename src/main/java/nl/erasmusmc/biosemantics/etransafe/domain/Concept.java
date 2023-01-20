package nl.erasmusmc.biosemantics.etransafe.domain;

import lombok.Builder;
import lombok.Getter;
import nl.erasmusmc.biosemantics.etransafe.parser.DateParser;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class Concept {
    private Integer conceptId;
    private String conceptCode;
    private String conceptName;
    private String conceptClassId;
    private String domainId;
    private String vocabularyId;
    private String standardConcept;
    private String invalidReason;
    @Builder.Default
    private Date validStartDate = DateParser.VALID_START_DATE;
    @Builder.Default
    private Date validEndDate = DateParser.VALID_END_DATE;
    private List<String> children;
    private List<String> synonyms;
    private List<String> parents;

    public void addChild(String child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        if (!this.children.contains(child)) {
            this.children.add(child);
        }
    }

}
