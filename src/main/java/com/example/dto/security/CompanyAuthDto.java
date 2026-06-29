package com.example.dto.security;

import java.util.ArrayList;
import java.util.List;

public class CompanyAuthDto {
    private String companyGuid;
    private Boolean selected;

    public Boolean getSelected() { return selected; }
    public void setSelected(Boolean selected) { this.selected = selected; }
    private List<PageDto> companyPages = new ArrayList<>();
    private List<InquiryDto> companyInquiries = new ArrayList<>();
    private List<WebServiceDto> companyWebServices = new ArrayList<>();
    private List<ProductAuthDto> products = new ArrayList<>();
    private List<PlanAuthDto> plans = new ArrayList<>();

    public CompanyAuthDto() {}

    public CompanyAuthDto(String companyGuid) {
        this.companyGuid = companyGuid;
    }

    public String getCompanyGuid() { return companyGuid; }
    public void setCompanyGuid(String companyGuid) { this.companyGuid = companyGuid; }
    public List<PageDto> getCompanyPages() { return companyPages; }
    public void setCompanyPages(List<PageDto> companyPages) { this.companyPages = companyPages; }
    public List<InquiryDto> getCompanyInquiries() { return companyInquiries; }
    public void setCompanyInquiries(List<InquiryDto> companyInquiries) { this.companyInquiries = companyInquiries; }
    public List<WebServiceDto> getCompanyWebServices() { return companyWebServices; }
    public void setCompanyWebServices(List<WebServiceDto> companyWebServices) { this.companyWebServices = companyWebServices; }
    public List<ProductAuthDto> getProducts() { return products; }
    public void setProducts(List<ProductAuthDto> products) { this.products = products; }
    public List<PlanAuthDto> getPlans() { return plans; }
    public void setPlans(List<PlanAuthDto> plans) { this.plans = plans; }
}
