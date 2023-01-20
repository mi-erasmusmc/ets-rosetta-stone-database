package nl.erasmusmc.biosemantics.etransafe.repo;

public interface Database {

    String DB_CONNECTION_PARAMS = "&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8&failOverReadOnly=true&maxReconnects=10&useSSL=false&verifyServerCertificate=false&allowLoadLocalInfile=true&allowPublicKeyRetrieval=true";

    void executeSQL(String sql);

    String getName();

}
