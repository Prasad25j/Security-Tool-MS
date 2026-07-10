package com.example.dto.coa;

import java.util.ArrayList;
import java.util.List;

public class CoaSaveResponseDto {
    private List<String> scripts = new ArrayList<>();
    private String message;

    public CoaSaveResponseDto() {}

    public CoaSaveResponseDto(List<String> scripts, String message) {
        this.scripts = scripts;
        this.message = message;
    }

    public List<String> getScripts() {
        return scripts;
    }

    public void setScripts(List<String> scripts) {
        this.scripts = scripts;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
