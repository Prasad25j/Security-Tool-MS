package com.example.dto.security;

import java.util.ArrayList;
import java.util.List;

public class TransactionDto {
    private String transactionGuid;
    private Boolean selected;

    public Boolean getSelected() { return selected; }
    public void setSelected(Boolean selected) { this.selected = selected; }
    private List<ButtonDto> buttons = new ArrayList<>();

    public TransactionDto() {}

    public TransactionDto(String transactionGuid) {
        this.transactionGuid = transactionGuid;
    }

    public String getTransactionGuid() { return transactionGuid; }
    public void setTransactionGuid(String transactionGuid) { this.transactionGuid = transactionGuid; }
    public List<ButtonDto> getButtons() { return buttons; }
    public void setButtons(List<ButtonDto> buttons) { this.buttons = buttons; }
}
