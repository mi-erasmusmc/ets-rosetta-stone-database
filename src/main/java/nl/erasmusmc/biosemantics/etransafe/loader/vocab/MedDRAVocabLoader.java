package nl.erasmusmc.biosemantics.etransafe.loader.vocab;

import nl.erasmusmc.biosemantics.etransafe.domain.Concept;
import nl.erasmusmc.biosemantics.etransafe.domain.MeddraHierarchy;
import nl.erasmusmc.biosemantics.etransafe.repo.CDM;
import nl.erasmusmc.biosemantics.etransafe.repo.MedDRA;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.Set;


public class MedDRAVocabLoader {

    public static final String MEDDRA_VOCABULARY_ID = "MedDRA";
    public static final String IS_A_PRIMARY = "Is a primary";
    public static final String SUBSUMES_PRIMARY = "Subsumes primary";
    static final Logger logger = LogManager.getLogger();
    private final MedDRA medDRA;
    private final CDM cdm;


    public MedDRAVocabLoader(MedDRA medDRADb, CDM cdm) {
        this.medDRA = medDRADb;
        this.cdm = cdm;
    }


    public void loadVocab() {
        loadSMQs();
        loadPrimaryPath();
    }

    private void loadSMQs() {
        List<Concept> concepts = medDRA.getSMQs();

        concepts.forEach(smq -> {
            cdm.addConcept(smq);
            Set<Integer> termCodes = medDRA.getTermCodesForSMQ(Integer.parseInt(smq.getConceptCode()));

            logger.info("Found {} concepts for {}", termCodes.size(), smq.getConceptName());
            termCodes.stream()
                    .map(code -> cdm.getConceptByCode(String.valueOf(code), MEDDRA_VOCABULARY_ID))
                    .filter(Objects::nonNull)
                    .forEach(pt -> addMapping(smq, pt, "SMQ - MedDRA", "MedDRA - SMQ"));
        });
        logger.info("Done loading SMQs");
    }

    private void loadPrimaryPath() {
        List<MeddraHierarchy> hierarchies = medDRA.getPrimaryPath();
        logger.info("Loading {} primary paths for MedDRA", hierarchies.size());
        hierarchies.forEach(primary -> {
            var pt = cdm.getConceptByCode(String.valueOf(primary.ptCode()), MEDDRA_VOCABULARY_ID);

            if (pt != null) {

                var hlt = cdm.getConceptByCode(String.valueOf(primary.hltCode()), MEDDRA_VOCABULARY_ID);
                var hlgt = cdm.getConceptByCode(String.valueOf(primary.hlgtCode()), MEDDRA_VOCABULARY_ID);
                var soc = cdm.getConceptByCode(String.valueOf(primary.socCode()), MEDDRA_VOCABULARY_ID);

                addMapping(pt, hlt, IS_A_PRIMARY, SUBSUMES_PRIMARY);
                addMapping(hlt, hlgt, IS_A_PRIMARY, SUBSUMES_PRIMARY);
                addMapping(hlgt, soc, IS_A_PRIMARY, SUBSUMES_PRIMARY);
                addMapping(pt, soc, "PT - Primary SOC", null);
            }
        });
        logger.info("Done loading primary paths for MedDRA");
    }

    private void addMapping(Concept source, Concept target, String relation, String reverseRelation) {
        cdm.addMapping(source, target, relation, reverseRelation, null, "eTRANSAFE");
    }

}
