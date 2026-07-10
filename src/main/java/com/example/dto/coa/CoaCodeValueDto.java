package com.example.dto.coa;

public class CoaCodeValueDto {
    private String codeValue;
    private String description;

    public CoaCodeValueDto() {}
    public CoaCodeValueDto(String codeValue, String description) {
        this.codeValue = codeValue;
        this.description = description;
    }

    public String getCodeValue() { return codeValue; }
    public void setCodeValue(String v) { this.codeValue = v; }

    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
}
