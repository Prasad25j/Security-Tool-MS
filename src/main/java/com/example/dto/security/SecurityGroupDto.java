package com.example.dto.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class SecurityGroupDto {
    private String securityGroupGuid;
    private String groupName;
    private List<CompanyAuthDto> companies = new ArrayList<>();
    
    @JsonIgnore
    private Map<String, String> authGuidMap = new HashMap<>();

    public SecurityGroupDto() {}

    public SecurityGroupDto(String securityGroupGuid, String groupName) {
        this.securityGroupGuid = securityGroupGuid;
        this.groupName = groupName;
    }

    public String getSecurityGroupGuid() { return securityGroupGuid; }
    public void setSecurityGroupGuid(String securityGroupGuid) { this.securityGroupGuid = securityGroupGuid; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public List<CompanyAuthDto> getCompanies() { return companies; }
    public void setCompanies(List<CompanyAuthDto> companies) { this.companies = companies; }
    
    public Map<String, String> getAuthGuidMap() { return authGuidMap; }
    public void setAuthGuidMap(Map<String, String> authGuidMap) { this.authGuidMap = authGuidMap; }
}

