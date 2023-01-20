package nl.erasmusmc.biosemantics.etransafe.parser;

import nl.erasmusmc.biosemantics.etransafe.exception.DBConstructionFailureException;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateParser {

    public static final Date VALID_START_DATE = new Date(0L);
    public static final Date VALID_END_DATE = Date.valueOf("2099-12-31");
    public static final Date NOW = Date.valueOf(java.time.LocalDate.now());


    private DateParser() {
    }

    public static Date parse(String dateStr) {
        try {
            return new Date(new SimpleDateFormat("dd-MM-yyyy").parse(dateStr).getTime());
        } catch (ParseException e) {
            throw new DBConstructionFailureException(e);
        }
    }

}
