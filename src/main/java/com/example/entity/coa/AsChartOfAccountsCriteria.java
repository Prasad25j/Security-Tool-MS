package com.example.entity.coa;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = "ASCHARTOFACCOUNTSCRITERIA")
@IdClass(AsChartOfAccountsCriteriaId.class)
public class AsChartOfAccountsCriteria {

    @Id
    private String CHARTOFACCOUNTSENTRYGUID;
    @Id
    private String CRITERIANAME;
    private String CRITERIAVALUE;

    public String getCHARTOFACCOUNTSENTRYGUID() { return CHARTOFACCOUNTSENTRYGUID; }
    public void setCHARTOFACCOUNTSENTRYGUID(String v) { this.CHARTOFACCOUNTSENTRYGUID = v; }

    public String getCRITERIANAME() { return CRITERIANAME; }
    public void setCRITERIANAME(String v) { this.CRITERIANAME = v; }

    public String getCRITERIAVALUE() { return CRITERIAVALUE; }
    public void setCRITERIAVALUE(String v) { this.CRITERIAVALUE = v; }
}
