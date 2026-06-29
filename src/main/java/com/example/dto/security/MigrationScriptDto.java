package com.example.dto.security;

public class MigrationScriptDto {
    private String companyGuid;
    private String productGuid;
    private String planGuid;
    private String entityType;
    private String entityGuid;
    private String script;

    public MigrationScriptDto() {}

    public MigrationScriptDto(String companyGuid, String productGuid, String planGuid, String entityType, String entityGuid, String script) {
        this.companyGuid = companyGuid;
        this.productGuid = productGuid;
        this.planGuid = planGuid;
        this.entityType = entityType;
        this.entityGuid = entityGuid;
        this.script = script;
    }

    public String getCompanyGuid() { return companyGuid; }
    public void setCompanyGuid(String companyGuid) { this.companyGuid = companyGuid; }

    public String getProductGuid() { return productGuid; }
    public void setProductGuid(String productGuid) { this.productGuid = productGuid; }

    public String getPlanGuid() { return planGuid; }
    public void setPlanGuid(String planGuid) { this.planGuid = planGuid; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityGuid() { return entityGuid; }
    public void setEntityGuid(String entityGuid) { this.entityGuid = entityGuid; }

    public String getScript() { return script; }
    public void setScript(String script) { this.script = script; }
}
