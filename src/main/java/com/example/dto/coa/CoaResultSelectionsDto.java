package com.example.dto.coa;

public class CoaResultSelectionsDto {
    private String branchSection;
    private String departmentSection;
    private String productSection;
    private String channelSection;
    private String lobSection;
    private Boolean company;
    private Boolean defaultBranchID;

    public CoaResultSelectionsDto() {}

    public String getBranchSection() {
        return branchSection;
    }

    public void setBranchSection(String branchSection) {
        this.branchSection = branchSection;
    }

    public String getDepartmentSection() {
        return departmentSection;
    }

    public void setDepartmentSection(String departmentSection) {
        this.departmentSection = departmentSection;
    }

    public String getProductSection() {
        return productSection;
    }

    public void setProductSection(String productSection) {
        this.productSection = productSection;
    }

    public String getChannelSection() {
        return channelSection;
    }

    public void setChannelSection(String channelSection) {
        this.channelSection = channelSection;
    }

    public String getLobSection() {
        return lobSection;
    }

    public void setLobSection(String lobSection) {
        this.lobSection = lobSection;
    }

    public Boolean getCompany() {
        return company;
    }

    public void setCompany(Boolean company) {
        this.company = company;
    }

    public Boolean getDefaultBranchID() {
        return defaultBranchID;
    }

    public void setDefaultBranchID(Boolean defaultBranchID) {
        this.defaultBranchID = defaultBranchID;
    }
}
