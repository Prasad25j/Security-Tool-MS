package com.example.dto.security;

public class WebServiceDto {
    private String webServiceGuid;
    private Boolean selected;

    public Boolean getSelected() { return selected; }
    public void setSelected(Boolean selected) { this.selected = selected; }

    public WebServiceDto() {}

    public WebServiceDto(String webServiceGuid) {
        this.webServiceGuid = webServiceGuid;
    }

    public String getWebServiceGuid() { return webServiceGuid; }
    public void setWebServiceGuid(String webServiceGuid) { this.webServiceGuid = webServiceGuid; }
}
