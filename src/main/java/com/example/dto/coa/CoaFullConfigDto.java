package com.example.dto.coa;

import java.util.List;

public class CoaFullConfigDto {
    // Account
    private String coaGuid;
    private String accountNumber;
    private String accountDescription;
    private String companyGuid;

    // Entity
    private String entityGuid;
    private String entityCode;
    private String transactionName;
    private boolean suspense;

    // Entry
    private String entryGuid;
    private String debitCreditCode;   // 01=Debit, 02=Credit
    private String debitCreditLabel;  // "Credit" / "Debit"
    private String entryDescription;
    private String accountingTypeCode;
    private String accountingTypeLabel;
    private String accountingAmountField;
    private String gainLossFlag;
    private String flipOnNegativeFlag;
    private String doReversalAccountingFlag;
    private String originalDisbursementStatusCode;
    private String fundTypeCode;
    private String linkSuspenseFlag;
    private String effectiveFromDate;
    private String effectiveToDate;

    // Criteria (criteriaValue '01' = checked)
    private List<CoaCriteriaDto> criteriaList;

    // Results
    private List<String> resultList;

    // Getters / Setters
    public String getCoaGuid() { return coaGuid; }
    public void setCoaGuid(String v) { this.coaGuid = v; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String v) { this.accountNumber = v; }

    public String getAccountDescription() { return accountDescription; }
    public void setAccountDescription(String v) { this.accountDescription = v; }

    public String getCompanyGuid() { return companyGuid; }
    public void setCompanyGuid(String v) { this.companyGuid = v; }

    public String getEntityGuid() { return entityGuid; }
    public void setEntityGuid(String v) { this.entityGuid = v; }

    public String getEntityCode() { return entityCode; }
    public void setEntityCode(String v) { this.entityCode = v; }

    public String getTransactionName() { return transactionName; }
    public void setTransactionName(String v) { this.transactionName = v; }

    public boolean isSuspense() { return suspense; }
    public void setSuspense(boolean v) { this.suspense = v; }

    public String getEntryGuid() { return entryGuid; }
    public void setEntryGuid(String v) { this.entryGuid = v; }

    public String getDebitCreditCode() { return debitCreditCode; }
    public void setDebitCreditCode(String v) { this.debitCreditCode = v; }

    public String getDebitCreditLabel() { return debitCreditLabel; }
    public void setDebitCreditLabel(String v) { this.debitCreditLabel = v; }

    public String getEntryDescription() { return entryDescription; }
    public void setEntryDescription(String v) { this.entryDescription = v; }

    public String getAccountingTypeCode() { return accountingTypeCode; }
    public void setAccountingTypeCode(String v) { this.accountingTypeCode = v; }

    public String getAccountingTypeLabel() { return accountingTypeLabel; }
    public void setAccountingTypeLabel(String v) { this.accountingTypeLabel = v; }

    public String getAccountingAmountField() { return accountingAmountField; }
    public void setAccountingAmountField(String v) { this.accountingAmountField = v; }

    public String getGainLossFlag() { return gainLossFlag; }
    public void setGainLossFlag(String v) { this.gainLossFlag = v; }

    public String getFlipOnNegativeFlag() { return flipOnNegativeFlag; }
    public void setFlipOnNegativeFlag(String v) { this.flipOnNegativeFlag = v; }

    public String getDoReversalAccountingFlag() { return doReversalAccountingFlag; }
    public void setDoReversalAccountingFlag(String v) { this.doReversalAccountingFlag = v; }

    public String getOriginalDisbursementStatusCode() { return originalDisbursementStatusCode; }
    public void setOriginalDisbursementStatusCode(String v) { this.originalDisbursementStatusCode = v; }

    public String getFundTypeCode() { return fundTypeCode; }
    public void setFundTypeCode(String v) { this.fundTypeCode = v; }

    public String getLinkSuspenseFlag() { return linkSuspenseFlag; }
    public void setLinkSuspenseFlag(String v) { this.linkSuspenseFlag = v; }

    public String getEffectiveFromDate() { return effectiveFromDate; }
    public void setEffectiveFromDate(String v) { this.effectiveFromDate = v; }

    public String getEffectiveToDate() { return effectiveToDate; }
    public void setEffectiveToDate(String v) { this.effectiveToDate = v; }

    public List<CoaCriteriaDto> getCriteriaList() { return criteriaList; }
    public void setCriteriaList(List<CoaCriteriaDto> v) { this.criteriaList = v; }

    public List<String> getResultList() { return resultList; }
    public void setResultList(List<String> v) { this.resultList = v; }
}
