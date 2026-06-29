package com.example.dto.security;

import java.util.ArrayList;
import java.util.List;

public class GenerateScriptsResponseDto {
    private String securityGroupGuid;
    private String groupName;
    private List<String> scripts = new ArrayList<>();
    private List<MigrationScriptDto> migrationScripts = new ArrayList<>();

    public GenerateScriptsResponseDto() {}

    public GenerateScriptsResponseDto(String securityGroupGuid, String groupName, List<String> scripts) {
        this.securityGroupGuid = securityGroupGuid;
        this.groupName = groupName;
        this.scripts = scripts;
    }

    public GenerateScriptsResponseDto(String securityGroupGuid, String groupName, List<String> scripts, List<MigrationScriptDto> migrationScripts) {
        this.securityGroupGuid = securityGroupGuid;
        this.groupName = groupName;
        this.scripts = scripts;
        this.migrationScripts = migrationScripts;
    }

    public String getSecurityGroupGuid() { return securityGroupGuid; }
    public void setSecurityGroupGuid(String securityGroupGuid) { this.securityGroupGuid = securityGroupGuid; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public List<String> getScripts() { return scripts; }
    public void setScripts(List<String> scripts) { this.scripts = scripts; }
    public List<MigrationScriptDto> getMigrationScripts() { return migrationScripts; }
    public void setMigrationScripts(List<MigrationScriptDto> migrationScripts) { this.migrationScripts = migrationScripts; }
}
