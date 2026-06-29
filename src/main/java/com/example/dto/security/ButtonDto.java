package com.example.dto.security;

public class ButtonDto {
    private String buttonGuid;
    private Boolean selected;

    public Boolean getSelected() { return selected; }
    public void setSelected(Boolean selected) { this.selected = selected; }

    public ButtonDto() {}

    public ButtonDto(String buttonGuid) {
        this.buttonGuid = buttonGuid;
    }

    public String getButtonGuid() { return buttonGuid; }
    public void setButtonGuid(String buttonGuid) { this.buttonGuid = buttonGuid; }
}
