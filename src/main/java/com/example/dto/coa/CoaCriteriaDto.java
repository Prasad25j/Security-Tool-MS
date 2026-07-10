package com.example.dto.coa;

public class CoaCriteriaDto {
    private String criteria;
    private String value;

    public CoaCriteriaDto() {}

    public CoaCriteriaDto(String criteria, String value) {
        this.criteria = criteria;
        this.value = value;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
