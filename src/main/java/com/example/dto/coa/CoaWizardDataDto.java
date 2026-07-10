package com.example.dto.coa;

import java.util.List;

public class CoaWizardDataDto {
    private String primaryCompany;
    private String accountNumber;
    private String accountDescription;
    private String transaction;
    private Boolean suspense;
    private String creditDebit;
    private String type;
    private String accountingAmount;
    private String fundType;
    private String effectiveFromDate;
    private String effectiveToDate;
    private String entryDescription;
    private String originalDisbursementStatus;
    private Boolean gainLoss;
    private Boolean flipOnNegative;
    private Boolean doReversalAccounting;
    private Boolean writeAccounting;
    private CoaResultSelectionsDto resultSelections;
    private Boolean isNewEntityOnly;
    private String existingAccountGuid;
    private Boolean editMode;
    private String coaGuid;
    private String entityGuid;
    private String entryGuid;
    private Integer lastConfiguredStep;

    public CoaWizardDataDto() {}

    public Integer getLastConfiguredStep() { return lastConfiguredStep; }
    public void setLastConfiguredStep(Integer lastConfiguredStep) { this.lastConfiguredStep = lastConfiguredStep; }

    public Boolean getIsNewEntityOnly() { return isNewEntityOnly; }
    public void setIsNewEntityOnly(Boolean isNewEntityOnly) { this.isNewEntityOnly = isNewEntityOnly; }

    public String getExistingAccountGuid() { return existingAccountGuid; }
    public void setExistingAccountGuid(String existingAccountGuid) { this.existingAccountGuid = existingAccountGuid; }

    public Boolean getEditMode() { return editMode; }
    public void setEditMode(Boolean editMode) { this.editMode = editMode; }

    public String getCoaGuid() { return coaGuid; }
    public void setCoaGuid(String coaGuid) { this.coaGuid = coaGuid; }

    public String getEntityGuid() { return entityGuid; }
    public void setEntityGuid(String entityGuid) { this.entityGuid = entityGuid; }

    public String getEntryGuid() { return entryGuid; }
    public void setEntryGuid(String entryGuid) { this.entryGuid = entryGuid; }

    public String getPrimaryCompany() {
        return primaryCompany;
    }

    public void setPrimaryCompany(String primaryCompany) {
        this.primaryCompany = primaryCompany;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountDescription() {
        return accountDescription;
    }

    public void setAccountDescription(String accountDescription) {
        this.accountDescription = accountDescription;
    }

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public Boolean getSuspense() {
        return suspense;
    }

    public void setSuspense(Boolean suspense) {
        this.suspense = suspense;
    }

    public String getCreditDebit() {
        return creditDebit;
    }

    public void setCreditDebit(String creditDebit) {
        this.creditDebit = creditDebit;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAccountingAmount() {
        return accountingAmount;
    }

    public void setAccountingAmount(String accountingAmount) {
        this.accountingAmount = accountingAmount;
    }

    public String getFundType() {
        return fundType;
    }

    public void setFundType(String fundType) {
        this.fundType = fundType;
    }

    public String getEffectiveFromDate() {
        return effectiveFromDate;
    }

    public void setEffectiveFromDate(String effectiveFromDate) {
        this.effectiveFromDate = effectiveFromDate;
    }

    public String getEffectiveToDate() {
        return effectiveToDate;
    }

    public void setEffectiveToDate(String effectiveToDate) {
        this.effectiveToDate = effectiveToDate;
    }

    public String getEntryDescription() {
        return entryDescription;
    }

    public void setEntryDescription(String entryDescription) {
        this.entryDescription = entryDescription;
    }

    public String getOriginalDisbursementStatus() {
        return originalDisbursementStatus;
    }

    public void setOriginalDisbursementStatus(String originalDisbursementStatus) {
        this.originalDisbursementStatus = originalDisbursementStatus;
    }

    public Boolean getGainLoss() {
        return gainLoss;
    }

    public void setGainLoss(Boolean gainLoss) {
        this.gainLoss = gainLoss;
    }

    public Boolean getFlipOnNegative() {
        return flipOnNegative;
    }

    public void setFlipOnNegative(Boolean flipOnNegative) {
        this.flipOnNegative = flipOnNegative;
    }

    public Boolean getDoReversalAccounting() {
        return doReversalAccounting;
    }

    public void setDoReversalAccounting(Boolean doReversalAccounting) {
        this.doReversalAccounting = doReversalAccounting;
    }

    public Boolean getWriteAccounting() {
        return writeAccounting;
    }

    public void setWriteAccounting(Boolean writeAccounting) {
        this.writeAccounting = writeAccounting;
    }

    public CoaResultSelectionsDto getResultSelections() {
        return resultSelections;
    }

    public void setResultSelections(CoaResultSelectionsDto resultSelections) {
        this.resultSelections = resultSelections;
    }
}
