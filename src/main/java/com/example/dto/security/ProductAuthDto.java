package com.example.dto.security;

import java.util.ArrayList;
import java.util.List;

public class ProductAuthDto {
    private String productGuid;
    private Boolean selected;

    public Boolean getSelected() { return selected; }
    public void setSelected(Boolean selected) { this.selected = selected; }
    private List<PageDto> productPages = new ArrayList<>();
    private List<TransactionDto> productTransactions = new ArrayList<>();

    public ProductAuthDto() {}

    public ProductAuthDto(String productGuid) {
        this.productGuid = productGuid;
    }

    public String getProductGuid() { return productGuid; }
    public void setProductGuid(String productGuid) { this.productGuid = productGuid; }
    public List<PageDto> getProductPages() { return productPages; }
    public void setProductPages(List<PageDto> productPages) { this.productPages = productPages; }
    public List<TransactionDto> getProductTransactions() { return productTransactions; }
    public void setProductTransactions(List<TransactionDto> productTransactions) { this.productTransactions = productTransactions; }
}
