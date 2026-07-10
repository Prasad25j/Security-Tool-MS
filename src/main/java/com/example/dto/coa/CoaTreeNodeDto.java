package com.example.dto.coa;

import java.util.List;
import java.util.Map;

public class CoaTreeNodeDto {
    private String id;
    private String name;
    private String type;
    private String icon;
    private Map<String, Object> details;
    private List<CoaTreeNodeDto> children;

    public CoaTreeNodeDto() {}

    public CoaTreeNodeDto(String id, String name, String type, String icon) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public List<CoaTreeNodeDto> getChildren() {
        return children;
    }

    public void setChildren(List<CoaTreeNodeDto> children) {
        this.children = children;
    }
}
