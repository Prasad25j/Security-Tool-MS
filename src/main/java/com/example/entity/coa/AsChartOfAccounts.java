package com.example.entity.coa;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ASCHARTOFACCOUNTS")
public class AsChartOfAccounts {

    @Id
    private String CHARTOFACCOUNTSGUID;
    private String COMPANYGUID;
    private String ACCOUNTNUMBER;
    private String ACCOUNTDESCRIPTION;

    public String getCHARTOFACCOUNTSGUID() { return CHARTOFACCOUNTSGUID; }
    public void setCHARTOFACCOUNTSGUID(String v) { this.CHARTOFACCOUNTSGUID = v; }

    public String getCOMPANYGUID() { return COMPANYGUID; }
    public void setCOMPANYGUID(String v) { this.COMPANYGUID = v; }

    public String getACCOUNTNUMBER() { return ACCOUNTNUMBER; }
    public void setACCOUNTNUMBER(String v) { this.ACCOUNTNUMBER = v; }

    public String getACCOUNTDESCRIPTION() { return ACCOUNTDESCRIPTION; }
    public void setACCOUNTDESCRIPTION(String v) { this.ACCOUNTDESCRIPTION = v; }
}
