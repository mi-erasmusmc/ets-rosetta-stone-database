package nl.erasmusmc.biosemantics.etransafe.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeddraSnomedPair {

    private String meddraCode;
    private String meddraName;
    private String snomedCode;
    private String snomedName;
    private String reason;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MeddraSnomedPair that = (MeddraSnomedPair) o;

        if (!meddraCode.equals(that.meddraCode)) return false;
        return snomedCode.equals(that.snomedCode);
    }

    @Override
    public int hashCode() {
        int result = meddraCode.hashCode();
        result = 31 * result + snomedCode.hashCode();
        return result;
    }
}
