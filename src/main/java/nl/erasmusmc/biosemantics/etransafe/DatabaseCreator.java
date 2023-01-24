package nl.erasmusmc.biosemantics.etransafe;

import nl.erasmusmc.biosemantics.etransafe.loader.database.CDMDatabaseLoader;
import nl.erasmusmc.biosemantics.etransafe.loader.database.MedDRADatabaseLoader;
import nl.erasmusmc.biosemantics.etransafe.loader.database.UMLSDatabaseLoader;
import nl.erasmusmc.biosemantics.etransafe.loader.mapping.EMCMappingsLoader;
import nl.erasmusmc.biosemantics.etransafe.loader.mapping.UMLSMappingsLoader;
import nl.erasmusmc.biosemantics.etransafe.loader.mapping.WebRADRMappingsLoader;
import nl.erasmusmc.biosemantics.etransafe.loader.vocab.MedDRAVocabLoader;
import nl.erasmusmc.biosemantics.etransafe.loader.vocab.OBOVocabLoader;
import nl.erasmusmc.biosemantics.etransafe.loader.vocab.SENDListVocabLoader;
import nl.erasmusmc.biosemantics.etransafe.repo.CDM;
import nl.erasmusmc.biosemantics.etransafe.repo.MedDRA;
import nl.erasmusmc.biosemantics.etransafe.repo.UMLS;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatabaseCreator {

    private static final Logger logger = LogManager.getLogger();


    public static void main(String[] args) {
        Options options = getOptions();
        CommandLine cmd = getCommandLine(options, args);

        String server = cmd.getOptionValue("server");
        String database = cmd.getOptionValue("database");
        String user = cmd.getOptionValue("user");
        String password = cmd.getOptionValue("password");
        String umlsSchema = cmd.getOptionValue("umls");
        String meddraSchema = cmd.getOptionValue("meddra");
        boolean useEToxMA = Boolean.parseBoolean(cmd.getOptionValue("etox"));

        if (server != null && database != null && user != null && password != null && umlsSchema != null && meddraSchema != null) {
            CDM cdm = new CDM(server, database, user, password);
            UMLS umlsDb = new UMLS(server, umlsSchema, user, password);
            MedDRA medDRADb = new MedDRA(server, meddraSchema, user, password);
            setUpDbs(cdm, umlsDb, medDRADb);
            addVocabsToCDM(cdm, medDRADb, useEToxMA);
            addMappings(cdm, umlsDb, useEToxMA);
            cdm.cleanConceptRelationShipTable();
        } else {
            new HelpFormatter().printHelp(EMCMappingsLoader.class.getName(), options);
        }
        logger.info("Done!");
    }

    private static void addMappings(CDM cdm, UMLS umlsDb, boolean eToxMA) {
        new WebRADRMappingsLoader(cdm).loadMappings();
        new UMLSMappingsLoader(cdm, umlsDb).loadMappings();
        new EMCMappingsLoader(cdm, eToxMA).loadMappings();
    }

    private static void addVocabsToCDM(CDM cdm, MedDRA medDRADb, boolean eToxMA) {
        new MedDRAVocabLoader(medDRADb, cdm).loadVocab();
        new OBOVocabLoader(cdm, eToxMA).loadVocab();
        new SENDListVocabLoader(cdm).loadVocab();
    }

    private static void setUpDbs(CDM cdm, UMLS umlsDb, MedDRA medDRADb) {
        new CDMDatabaseLoader(cdm).loadDb();
        new UMLSDatabaseLoader(umlsDb).loadDb();
        new MedDRADatabaseLoader(medDRADb).loadDb();
    }


    private static Options getOptions() {
        return new Options()
                .addOption("server", true, "Database server")
                .addOption("database", true, "Target database")
                .addOption("umls", true, "UMLS database")
                .addOption("meddra", true, "MedDRA database")
                .addOption("user", true, "MySQL username")
                .addOption("password", true, "MySQL password")
                .addOption("etox", true, "Set true to load eTox Mouse Anatomy version");
    }

    private static CommandLine getCommandLine(Options options, String[] args) {
        try {
            return new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            logger.error("Failed to parse command line options");
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }
}

