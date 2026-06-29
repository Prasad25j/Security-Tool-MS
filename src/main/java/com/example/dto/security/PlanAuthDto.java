package com.example.dto.security;

import java.util.ArrayList;
import java.util.List;

public class PlanAuthDto {
    private String planGuid;
    private Boolean selected;

    public Boolean getSelected() { return selected; }
    public void setSelected(Boolean selected) { this.selected = selected; }
    private List<PageDto> planPages = new ArrayList<>();
    private List<TransactionDto> planTransactions = new ArrayList<>();
    private List<InquiryDto> planInquiries = new ArrayList<>();

    public PlanAuthDto() {}

    public PlanAuthDto(String planGuid) {
        this.planGuid = planGuid;
    }

    public String getPlanGuid() { return planGuid; }
    public void setPlanGuid(String planGuid) { this.planGuid = planGuid; }
    public List<PageDto> getPlanPages() { return planPages; }
    public void setPlanPages(List<PageDto> planPages) { this.planPages = planPages; }
    public List<TransactionDto> getPlanTransactions() { return planTransactions; }
    public void setPlanTransactions(List<TransactionDto> planTransactions) { this.planTransactions = planTransactions; }
    public List<InquiryDto> getPlanInquiries() { return planInquiries; }
    public void setPlanInquiries(List<InquiryDto> planInquiries) { this.planInquiries = planInquiries; }
}
