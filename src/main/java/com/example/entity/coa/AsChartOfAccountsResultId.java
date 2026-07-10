package com.example.entity.coa;

import java.io.Serializable;
import java.util.Objects;

public class AsChartOfAccountsResultId implements Serializable {
    private String CHARTOFACCOUNTSENTRYGUID;
    private String RESULTNAME;

    public AsChartOfAccountsResultId() {}

    public AsChartOfAccountsResultId(String CHARTOFACCOUNTSENTRYGUID, String RESULTNAME) {
        this.CHARTOFACCOUNTSENTRYGUID = CHARTOFACCOUNTSENTRYGUID;
        this.RESULTNAME = RESULTNAME;
    }

    public String getCHARTOFACCOUNTSENTRYGUID() { return CHARTOFACCOUNTSENTRYGUID; }
    public void setCHARTOFACCOUNTSENTRYGUID(String v) { this.CHARTOFACCOUNTSENTRYGUID = v; }

    public String getRESULTNAME() { return RESULTNAME; }
    public void setRESULTNAME(String v) { this.RESULTNAME = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AsChartOfAccountsResultId that = (AsChartOfAccountsResultId) o;
        return Objects.equals(CHARTOFACCOUNTSENTRYGUID, that.CHARTOFACCOUNTSENTRYGUID) &&
               Objects.equals(RESULTNAME, that.RESULTNAME);
    }

    @Override
    public int hashCode() {
        return Objects.hash(CHARTOFACCOUNTSENTRYGUID, RESULTNAME);
    }
}
