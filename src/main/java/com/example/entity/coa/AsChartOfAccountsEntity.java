package com.example.entity.coa;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ASCHARTOFACCOUNTSENTITY")
public class AsChartOfAccountsEntity {

    @Id
    private String CHARTOFACCOUNTSENTITYGUID;
    private String CHARTOFACCOUNTSGUID;
    private String CHARTOFACCOUNTSENTITYCODE;
    private String TRANSACTIONNAME;

    public String getCHARTOFACCOUNTSENTITYGUID() { return CHARTOFACCOUNTSENTITYGUID; }
    public void setCHARTOFACCOUNTSENTITYGUID(String v) { this.CHARTOFACCOUNTSENTITYGUID = v; }

    public String getCHARTOFACCOUNTSGUID() { return CHARTOFACCOUNTSGUID; }
    public void setCHARTOFACCOUNTSGUID(String v) { this.CHARTOFACCOUNTSGUID = v; }

    public String getCHARTOFACCOUNTSENTITYCODE() { return CHARTOFACCOUNTSENTITYCODE; }
    public void setCHARTOFACCOUNTSENTITYCODE(String v) { this.CHARTOFACCOUNTSENTITYCODE = v; }

    public String getTRANSACTIONNAME() { return TRANSACTIONNAME; }
    public void setTRANSACTIONNAME(String v) { this.TRANSACTIONNAME = v; }
}
