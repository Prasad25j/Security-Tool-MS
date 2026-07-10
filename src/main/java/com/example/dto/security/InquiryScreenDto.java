package com.example.dto.security;

public class InquiryScreenDto {
    private String inquiryScreenGuid;
    private String screenName;
    private String typeCode;
    private String typeName;

    public InquiryScreenDto() {}

    public InquiryScreenDto(String inquiryScreenGuid, String screenName) {
        this.inquiryScreenGuid = inquiryScreenGuid;
        this.screenName = screenName;
    }

    public InquiryScreenDto(String inquiryScreenGuid, String screenName, String typeCode, String typeName) {
        this.inquiryScreenGuid = inquiryScreenGuid;
        this.screenName = screenName;
        this.typeCode = typeCode;
        this.typeName = typeName;
    }

    public String getInquiryScreenGuid() {
        return inquiryScreenGuid;
    }

    public void setInquiryScreenGuid(String inquiryScreenGuid) {
        this.inquiryScreenGuid = inquiryScreenGuid;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
