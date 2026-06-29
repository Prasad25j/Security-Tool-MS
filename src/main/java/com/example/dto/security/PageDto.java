package com.example.dto.security;

import java.util.ArrayList;
import java.util.List;

public class PageDto {
    private String pageGuid;
    private Boolean selected;

    public Boolean getSelected() { return selected; }
    public void setSelected(Boolean selected) { this.selected = selected; }
    private List<ButtonDto> buttons = new ArrayList<>();

    public PageDto() {}

    public PageDto(String pageGuid) {
        this.pageGuid = pageGuid;
    }

    public String getPageGuid() { return pageGuid; }
    public void setPageGuid(String pageGuid) { this.pageGuid = pageGuid; }
    public List<ButtonDto> getButtons() { return buttons; }
    public void setButtons(List<ButtonDto> buttons) { this.buttons = buttons; }
}
