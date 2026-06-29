package com.example.dto.security;

public class InquiryDto {
    private String inquiryScreenNameGuid;
    private Boolean selected;

    public Boolean getSelected() { return selected; }
    public void setSelected(Boolean selected) { this.selected = selected; }

    public InquiryDto() {}

    public InquiryDto(String inquiryScreenNameGuid) {
        this.inquiryScreenNameGuid = inquiryScreenNameGuid;
    }

    public String getInquiryScreenNameGuid() { return inquiryScreenNameGuid; }
    public void setInquiryScreenNameGuid(String inquiryScreenNameGuid) { this.inquiryScreenNameGuid = inquiryScreenNameGuid; }
}
