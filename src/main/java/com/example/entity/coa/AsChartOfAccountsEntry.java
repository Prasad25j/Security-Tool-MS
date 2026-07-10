package com.example.entity.coa;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "ASCHARTOFACCOUNTSENTRY")
public class AsChartOfAccountsEntry {

    @Id
    private String CHARTOFACCOUNTSENTRYGUID;
    private String CHARTOFACCOUNTSENTITYGUID;
    private String DEBITCREDITCODE;
    private String ENTRYDESCRIPTION;
    private String ACCOUNTINGTYPECODE;
    private String ACCOUNTINGAMOUNTFIELD;
    private String GAINLOSSFLAG;
    private String FLIPONNEGATIVEFLAG;
    private Timestamp EFFECTIVEFROMDATE;
    private Timestamp EFFECTIVETODATE;
    private String DOREVERSALACCOUNTINGFLAG;
    private String ORIGINALDISBURSEMENTSTATUSCODE;
    private String FUNDTYPECODE;
    private String ACCOUNTNUMBERFORMAT;
    private String LINKSUSPENSEFLAG;

    public String getCHARTOFACCOUNTSENTRYGUID() { return CHARTOFACCOUNTSENTRYGUID; }
    public void setCHARTOFACCOUNTSENTRYGUID(String v) { this.CHARTOFACCOUNTSENTRYGUID = v; }

    public String getCHARTOFACCOUNTSENTITYGUID() { return CHARTOFACCOUNTSENTITYGUID; }
    public void setCHARTOFACCOUNTSENTITYGUID(String v) { this.CHARTOFACCOUNTSENTITYGUID = v; }

    public String getDEBITCREDITCODE() { return DEBITCREDITCODE; }
    public void setDEBITCREDITCODE(String v) { this.DEBITCREDITCODE = v; }

    public String getENTRYDESCRIPTION() { return ENTRYDESCRIPTION; }
    public void setENTRYDESCRIPTION(String v) { this.ENTRYDESCRIPTION = v; }

    public String getACCOUNTINGTYPECODE() { return ACCOUNTINGTYPECODE; }
    public void setACCOUNTINGTYPECODE(String v) { this.ACCOUNTINGTYPECODE = v; }

    public String getACCOUNTINGAMOUNTFIELD() { return ACCOUNTINGAMOUNTFIELD; }
    public void setACCOUNTINGAMOUNTFIELD(String v) { this.ACCOUNTINGAMOUNTFIELD = v; }

    public String getGAINLOSSFLAG() { return GAINLOSSFLAG; }
    public void setGAINLOSSFLAG(String v) { this.GAINLOSSFLAG = v; }

    public String getFLIPONNEGATIVEFLAG() { return FLIPONNEGATIVEFLAG; }
    public void setFLIPONNEGATIVEFLAG(String v) { this.FLIPONNEGATIVEFLAG = v; }

    public Timestamp getEFFECTIVEFROMDATE() { return EFFECTIVEFROMDATE; }
    public void setEFFECTIVEFROMDATE(Timestamp v) { this.EFFECTIVEFROMDATE = v; }

    public Timestamp getEFFECTIVETODATE() { return EFFECTIVETODATE; }
    public void setEFFECTIVETODATE(Timestamp v) { this.EFFECTIVETODATE = v; }

    public String getDOREVERSALACCOUNTINGFLAG() { return DOREVERSALACCOUNTINGFLAG; }
    public void setDOREVERSALACCOUNTINGFLAG(String v) { this.DOREVERSALACCOUNTINGFLAG = v; }

    public String getORIGINALDISBURSEMENTSTATUSCODE() { return ORIGINALDISBURSEMENTSTATUSCODE; }
    public void setORIGINALDISBURSEMENTSTATUSCODE(String v) { this.ORIGINALDISBURSEMENTSTATUSCODE = v; }

    public String getFUNDTYPECODE() { return FUNDTYPECODE; }
    public void setFUNDTYPECODE(String v) { this.FUNDTYPECODE = v; }

    public String getACCOUNTNUMBERFORMAT() { return ACCOUNTNUMBERFORMAT; }
    public void setACCOUNTNUMBERFORMAT(String v) { this.ACCOUNTNUMBERFORMAT = v; }

    public String getLINKSUSPENSEFLAG() { return LINKSUSPENSEFLAG; }
    public void setLINKSUSPENSEFLAG(String v) { this.LINKSUSPENSEFLAG = v; }
}
