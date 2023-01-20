package nl.erasmusmc.biosemantics.etransafe.repo;

public class IDGenerator {


    private static Integer id = null;

    private IDGenerator() {
    }


    public static Integer generateId() {
        id += 1;
        return id;
    }

    public static void setId(Integer value) {
        id = value;
    }


}
