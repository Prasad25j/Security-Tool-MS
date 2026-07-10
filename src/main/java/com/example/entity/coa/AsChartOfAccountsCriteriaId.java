package com.example.entity.coa;

import java.io.Serializable;
import java.util.Objects;

public class AsChartOfAccountsCriteriaId implements Serializable {
    private String CHARTOFACCOUNTSENTRYGUID;
    private String CRITERIANAME;

    public AsChartOfAccountsCriteriaId() {}

    public AsChartOfAccountsCriteriaId(String CHARTOFACCOUNTSENTRYGUID, String CRITERIANAME) {
        this.CHARTOFACCOUNTSENTRYGUID = CHARTOFACCOUNTSENTRYGUID;
        this.CRITERIANAME = CRITERIANAME;
    }

    public String getCHARTOFACCOUNTSENTRYGUID() { return CHARTOFACCOUNTSENTRYGUID; }
    public void setCHARTOFACCOUNTSENTRYGUID(String v) { this.CHARTOFACCOUNTSENTRYGUID = v; }

    public String getCRITERIANAME() { return CRITERIANAME; }
    public void setCRITERIANAME(String v) { this.CRITERIANAME = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AsChartOfAccountsCriteriaId that = (AsChartOfAccountsCriteriaId) o;
        return Objects.equals(CHARTOFACCOUNTSENTRYGUID, that.CHARTOFACCOUNTSENTRYGUID) &&
               Objects.equals(CRITERIANAME, that.CRITERIANAME);
    }

    @Override
    public int hashCode() {
        return Objects.hash(CHARTOFACCOUNTSENTRYGUID, CRITERIANAME);
    }
}
