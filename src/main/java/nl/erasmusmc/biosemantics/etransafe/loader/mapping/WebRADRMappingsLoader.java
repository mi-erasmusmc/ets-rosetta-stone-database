package nl.erasmusmc.biosemantics.etransafe.loader.mapping;

import nl.erasmusmc.biosemantics.etransafe.domain.Concept;
import nl.erasmusmc.biosemantics.etransafe.domain.MeddraSnomedPair;
import nl.erasmusmc.biosemantics.etransafe.repo.CDM;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.apache.commons.lang3.SystemUtils.USER_DIR;

public class WebRADRMappingsLoader implements MappingLoader {

    private static final String MEDDRA = "MedDRA";
    private static final String SNOMED = "SNOMED";
    private static final String WEBRADR = "WebRADR";
    private static final String WEB_RADR_FILE = "SNOMED_CT-MedDRA_Maps_11_May_2022.xlsx";
    private static final Logger logger = LogManager.getLogger();
    private final CDM cdm;


    public WebRADRMappingsLoader(CDM cdm) {
        this.cdm = cdm;
    }


    public void loadMappings() {
        String excelFile = USER_DIR + "/data/mappings/" + WEB_RADR_FILE;
        logger.info("Loading web-radr mappings");

        cdm.deleteMappingByVocAndSource(MEDDRA, SNOMED, WEBRADR);
        readMedDRA2SnomedMappingFromWebRADR(excelFile).forEach(mapping -> addMeddraSnomedMapping(mapping, true));
        readSnomed2MeddraMappingFromWebRADR(excelFile).forEach(mapping -> addMeddraSnomedMapping(mapping, false));

        logger.info("Done loading web-radr mappings");

    }

    private void addMeddraSnomedMapping(MeddraSnomedPair mapping, boolean meddra2Snomed) {
        var relation = meddra2Snomed ? "Maps to" : "Mapped from";
        var reverse = meddra2Snomed ? "Mapped from" : "Maps to";
        Concept meddraConcept = cdm.getConceptByCode(mapping.getMeddraCode(), MEDDRA);
        if (meddraConcept != null) {
            Concept snomedConcept = cdm.getConceptByCode(mapping.getSnomedCode(), SNOMED);
            if (snomedConcept != null) {
                cdm.addMapping(meddraConcept, snomedConcept, relation, reverse, null, WEBRADR);
            } else {
                logger.debug("Could not find SNOMED concept {} ({})", mapping.getSnomedName(), mapping.getSnomedCode());
            }
        } else {
            logger.warn("Could not find MedDRA concept {} ({})", mapping.getMeddraName(), mapping.getMeddraCode());
        }
    }

    private List<MeddraSnomedPair> readMedDRA2SnomedMappingFromWebRADR(String filename) {
        List<MeddraSnomedPair> result = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(new File(filename))) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.rowIterator();

            /* skip header */
            rowIterator.next();

            DataFormatter fmt = new DataFormatter();

            while (rowIterator.hasNext()) {

                Row row = rowIterator.next();

                MeddraSnomedPair mapping = new MeddraSnomedPair();

                mapping.setMeddraCode(fmt.formatCellValue(row.getCell(0)));
                mapping.setMeddraName(row.getCell(1).getStringCellValue());
                mapping.setSnomedCode(fmt.formatCellValue(row.getCell(2)));
                mapping.setSnomedName(row.getCell(3).getStringCellValue());
                result.add(mapping);
            }
        } catch (EncryptedDocumentException | IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    // Second sheet has reverse mappings
    private List<MeddraSnomedPair> readSnomed2MeddraMappingFromWebRADR(String filename) {
        List<MeddraSnomedPair> result = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(new File(filename))) {

            Sheet sheet = workbook.getSheetAt(1);
            Iterator<Row> rowIterator = sheet.rowIterator();

            /* skip header */
            rowIterator.next();

            DataFormatter fmt = new DataFormatter();

            while (rowIterator.hasNext()) {

                Row row = rowIterator.next();

                MeddraSnomedPair mapping = new MeddraSnomedPair();

                mapping.setSnomedCode(fmt.formatCellValue(row.getCell(0)));
                mapping.setSnomedName(row.getCell(1).getStringCellValue());
                mapping.setMeddraCode(fmt.formatCellValue(row.getCell(2)));
                mapping.setMeddraName(row.getCell(3).getStringCellValue());
                result.add(mapping);
            }
        } catch (EncryptedDocumentException | IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

}
