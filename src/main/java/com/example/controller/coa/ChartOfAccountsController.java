package com.example.controller.coa;

import com.example.dto.coa.CoaSaveResponseDto;
import com.example.dto.coa.CoaTreeNodeDto;
import com.example.dto.coa.CoaWizardDataDto;
import com.example.entity.coa.AsChartOfAccountsEntity;
import com.example.service.coa.ChartOfAccountsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chart-of-accounts")
public class ChartOfAccountsController {

    @Autowired
    private ChartOfAccountsService chartOfAccountsService;

    @GetMapping("/hierarchy")
    public ResponseEntity<List<CoaTreeNodeDto>> getHierarchyTree() {
        List<CoaTreeNodeDto> tree = chartOfAccountsService.getHierarchyTree();
        return ResponseEntity.ok(tree);
    }

    @PostMapping
    public ResponseEntity<CoaSaveResponseDto> createConfiguration(@RequestBody CoaWizardDataDto requestDto) {
        CoaSaveResponseDto response = chartOfAccountsService.generateSaveScripts(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/entity-by-transaction")
    public ResponseEntity<List<AsChartOfAccountsEntity>> getEntitiesByTransactionName(@RequestParam("transactionName") String transactionName) {
        List<AsChartOfAccountsEntity> entities = chartOfAccountsService.getEntitiesByTransactionName(transactionName);
        return ResponseEntity.ok(entities);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<com.example.dto.coa.CoaCodeValueDto>> getTransactions() {
        return ResponseEntity.ok(chartOfAccountsService.getTransactions());
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkAccountExists(@RequestParam("accountNumber") String accountNumber) {
        return ResponseEntity.ok(chartOfAccountsService.checkAccountExists(accountNumber));
    }

    @GetMapping("/config/{entryGuid}")
    public ResponseEntity<com.example.dto.coa.CoaFullConfigDto> getFullConfig(@PathVariable("entryGuid") String entryGuid) {
        return ResponseEntity.ok(chartOfAccountsService.getFullConfig(entryGuid));
    }

    @GetMapping("/config-by-entity/{entityGuid}")
    public ResponseEntity<com.example.dto.coa.CoaFullConfigDto> getFullConfigByEntity(@PathVariable("entityGuid") String entityGuid) {
        return ResponseEntity.ok(chartOfAccountsService.getFullConfigByEntity(entityGuid));
    }
}
