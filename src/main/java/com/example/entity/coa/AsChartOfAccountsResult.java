package com.example.entity.coa;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = "ASCHARTOFACCOUNTSRESULT")
@IdClass(AsChartOfAccountsResultId.class)
public class AsChartOfAccountsResult {

    @Id
    private String CHARTOFACCOUNTSENTRYGUID;
    @Id
    private String RESULTNAME;

    public String getCHARTOFACCOUNTSENTRYGUID() { return CHARTOFACCOUNTSENTRYGUID; }
    public void setCHARTOFACCOUNTSENTRYGUID(String v) { this.CHARTOFACCOUNTSENTRYGUID = v; }

    public String getRESULTNAME() { return RESULTNAME; }
    public void setRESULTNAME(String v) { this.RESULTNAME = v; }
}
