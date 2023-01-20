package nl.erasmusmc.biosemantics.etransafe.loader.mapping;

import nl.erasmusmc.biosemantics.etransafe.domain.Concept;
import nl.erasmusmc.biosemantics.etransafe.repo.CDM;
import nl.erasmusmc.biosemantics.etransafe.repo.UMLS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UMLSMappingsLoader implements MappingLoader {

    private static final Logger logger = LogManager.getLogger();
    private static final String MEDDRA = "MedDRA";
    private static final String SNOMED = "SNOMED";
    private static final String UMLS = "UMLS";
    final CDM cdm;
    final UMLS umlsDb;

    public UMLSMappingsLoader(CDM cdm, UMLS umls) {
        this.cdm = cdm;
        this.umlsDb = umls;
    }


    public void loadMappings() {
        cdm.deleteMappingByVocAndSource(MEDDRA, SNOMED, UMLS);
        umlsDb.getMedDRA2SNOMED().forEach(mapping -> {
            Concept snomedConcept = cdm.getConceptByCode(mapping.getSnomedCode(), SNOMED);
            if (snomedConcept != null) {
                Concept meddraConcept = cdm.getConceptByCode(mapping.getMeddraCode(), MEDDRA);
                if (meddraConcept != null) {
                    cdm.addMapping(meddraConcept, snomedConcept, "Maps to", "Mapped from", null, UMLS);
                    cdm.addMapping(snomedConcept, meddraConcept, "Maps to", "Mapped from", null, UMLS);
                } else {
                    logger.error("Could not find MedDRA concept for {}", mapping.getSnomedCode());
                }
            } else {
                logger.debug("Could not find SNOMED concept for {}", mapping.getMeddraCode());
            }
        });
        logger.info("Done loading mappings from UMLS");
    }
}
