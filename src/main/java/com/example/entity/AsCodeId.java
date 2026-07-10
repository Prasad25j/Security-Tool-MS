package com.example.entity;

import java.io.Serializable;
import java.util.Objects;

public class AsCodeId implements Serializable {
    private String CODENAME;
    private String CODEVALUE;

    public AsCodeId() {}
    public AsCodeId(String CODENAME, String CODEVALUE) { this.CODENAME = CODENAME; this.CODEVALUE = CODEVALUE; }

    public String getCODENAME() { return CODENAME; }
    public String getCODEVALUE() { return CODEVALUE; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AsCodeId)) return false;
        AsCodeId that = (AsCodeId) o;
        return Objects.equals(CODENAME, that.CODENAME) && Objects.equals(CODEVALUE, that.CODEVALUE);
    }

    @Override
    public int hashCode() { return Objects.hash(CODENAME, CODEVALUE); }
}
