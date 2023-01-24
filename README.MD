## eTransafe Rosetta Stone Database

**Java code to construct the database for the eTransafe Rosette Stone.**

### REQUIREMENTS

- MySQL version 8
- Java 17+
- Maven 3.8
- OMOP CDM Vocabulary from Athena
- UMLS MRCONSO.RRF file
- MedDRA ascii files
- An internet connection to load the mappings from [this repo](https://github.com/mi-erasmusmc/send-snomed-mappings)

### HOW TO BUILD THE DB

1. Download the OMOP CDM vocabulary from Athena, when downloading select only MedDRA and SNOMED as vocabularies, you
   don't need the rest. Unzip it and place the csvs in the ./data/vocabulary folder.
2. Download the UMLS, MRCONSO.RRF file from NLM, place this file in the ./data/umls folder.
3. And add the MedDRA ascii files to the ./data/meddra folder.
4. Create empty schemas for ULMS, MedDRA and your Rosetta Stone database.
5. Build the app `mvn clean package assembly:single`
6. Run the app from the project root adding your db connection
   details `java -jar target/DatabaseCreator-1.0-SNAPSHOT-jar-with-dependencies.jar -meddra <your meddra db> -umls <your umls db> -database <your rosetta stone db> -server <e.g. localhost> -user <your db user> -password <your db password>.`

Notes:

- The loading process will fail if mappings are out of sync with the source terminologies, error
  messages that should be somehow useful will be displayed
- Loading the DB may take up to several hours, be patient.

- By default, this code will add the Mouse Anatomy vocabulary as found
  on [Bioportal](https://bioportal.bioontology.org/ontologies/MA). This version is different from (and incompatible
  with) the extended Mouse Anatomy vocabulary as used in the eTox database. To load the eTox version pass -etox true as
  additional argument

### WHAT IS NEXT?

When you have the database ready to go, you can run the eTransafe Rosetta Stone application which is
available [here](https://github.com/mi-erasmusmc/ets-rosetta-stone)

### CONTRIBUTIONS

Contributions are welcome, please get in touch.
