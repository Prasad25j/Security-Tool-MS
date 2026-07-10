package com.example.service.coa;

import com.example.dto.coa.*;
import com.example.entity.AsCompany;
import com.example.entity.coa.*;
import com.example.secondaryDev.repository.AsCompanySecondaryDevRepository;
import com.example.secondaryDev.repository.coa.*;
import com.example.secondaryDev.repository.AsTransactionSecondaryDevRepository;
import com.example.secondaryDev.repository.AsProductSecondaryDevRepository;
import com.example.secondaryDev.repository.AsPlanSecondaryDevRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChartOfAccountsService {

    @Autowired
    private AsCompanySecondaryDevRepository companyRepository;

    @Autowired
    private AsChartOfAccountsRepository accountsRepository;

    @Autowired
    private AsChartOfAccountsEntityRepository entityRepository;

    @Autowired
    private AsChartOfAccountsEntryRepository entryRepository;

    @Autowired
    private AsChartOfAccountsCriteriaRepository criteriaRepository;

    @Autowired
    private AsChartOfAccountsResultRepository resultRepository;

    @Autowired
    private AsTransactionSecondaryDevRepository transactionRepository;

    @Autowired
    private AsProductSecondaryDevRepository productRepository;

    @Autowired
    private AsPlanSecondaryDevRepository planRepository;

    public List<CoaTreeNodeDto> getHierarchyTree() {
        System.out.println(">>> Starting getHierarchyTree execution...");
        long start = System.currentTimeMillis();
        try {
            String targetCompanyGuid = "DD92D03C-8F7F-4C97-A483-A6BA4C1AE802";
            List<Object[]> rows = accountsRepository.fetchHierarchyData(targetCompanyGuid);
            if (rows.isEmpty()) {
                return getFallbackMockHierarchy();
            }

            CoaTreeNodeDto rootNode = new CoaTreeNodeDto("ROOT-1", "Chart Of Accounts", "root", "account_balance_wallet");
            List<CoaTreeNodeDto> rootChildren = new ArrayList<>();

            CoaTreeNodeDto companyNode = null;
            Map<String, CoaTreeNodeDto> accountNodesMap = new LinkedHashMap<>();
            Map<String, CoaTreeNodeDto> transactionNodesMap = new LinkedHashMap<>();

            for (Object[] row : rows) {
                String compGuid = (String) row[0];
                String compName = (String) row[1];
                
                if (companyNode == null) {
                    companyNode = new CoaTreeNodeDto(compGuid, compName != null ? compName : "Britam Holdings", "company", "domain");
                    companyNode.setChildren(new ArrayList<>());
                    rootChildren.add(companyNode);
                }

                String accGuid = (String) row[2];
                String accNum = (String) row[3];
                String accDesc = (String) row[4];
                
                if (accGuid == null) {
                    continue;
                }

                CoaTreeNodeDto accNode = accountNodesMap.get(accGuid);
                if (accNode == null) {
                    String accLabel = accNum + " - " + (accDesc != null ? accDesc : "");
                    accNode = new CoaTreeNodeDto(accGuid, accLabel, "account", "receipt_long");
                    
                    Map<String, Object> details = new HashMap<>();
                    details.put("accountNumber", accNum);
                    details.put("description", accDesc);
                    details.put("coaGuid", accGuid);
                    accNode.setDetails(details);
                    accNode.setChildren(new ArrayList<>());
                    
                    accountNodesMap.put(accGuid, accNode);
                    companyNode.getChildren().add(accNode);
                }

                String entGuid = (String) row[5];
                String entCode = (String) row[6];
                String txnName = (String) row[7];

                if (entGuid == null) {
                    continue;
                }

                CoaTreeNodeDto txnNode = transactionNodesMap.get(entGuid);
                if (txnNode == null) {
                    String txnLabel = txnName != null ? txnName : "Transaction";
                    txnNode = new CoaTreeNodeDto(entGuid, txnLabel, "transaction", "swap_horiz");
                    
                    Map<String, Object> details = new HashMap<>();
                    details.put("entityGuid", entGuid);
                    details.put("entityCode", entCode);
                    details.put("transactionName", txnName);
                    details.put("suspense", false);
                    txnNode.setDetails(details);
                    txnNode.setChildren(new ArrayList<>());
                    
                    transactionNodesMap.put(entGuid, txnNode);
                    accNode.getChildren().add(txnNode);
                }

                String entryGuid = (String) row[8];
                String debitCreditCode = (String) row[9];
                String entryDesc = (String) row[10];
                String accountingTypeCode = (String) row[11];
                String accountingAmountField = (String) row[12];
                String fundTypeCode = (String) row[13];
                String originalDisbursementStatusCode = (String) row[14];

                if (entryGuid == null) {
                    continue;
                }

                String typeCode = accountingTypeCode != null ? accountingTypeCode.trim() : "";
                String enLabel = null;
                if ("03".equals(typeCode)) {
                    enLabel = accountingAmountField;
                } else if ("01".equals(typeCode)) {
                    enLabel = fundTypeCode != null ? "Fund Type: " + fundTypeCode.trim() : null;
                } else if ("02".equals(typeCode)) {
                    enLabel = originalDisbursementStatusCode != null ? "Disbursement Status: " + originalDisbursementStatusCode.trim() : null;
                }
                if (enLabel == null || enLabel.trim().isEmpty()) {
                    enLabel = entryDesc != null ? entryDesc : "Accounting Entry";
                }

                CoaTreeNodeDto enNode = new CoaTreeNodeDto(entryGuid, enLabel, "entry", "analytics");
                Map<String, Object> entryDetails = new HashMap<>();
                entryDetails.put("accountingTypeCode", typeCode);
                enNode.setDetails(entryDetails);
                txnNode.getChildren().add(enNode);
            }

            rootNode.setChildren(rootChildren);
            List<CoaTreeNodeDto> treeNodes = new ArrayList<>();
            treeNodes.add(rootNode);
            System.out.println(">>> Optimized single-query hierarchy tree built in " + (System.currentTimeMillis() - start) + "ms");
            return treeNodes;
        } catch (Exception e) {
            System.err.println(">>> Error in getHierarchyTree: " + e.getMessage());
            e.printStackTrace();
            return getFallbackMockHierarchy();
        }
    }

    private List<CoaTreeNodeDto> buildAccountNodesBulk(
            List<AsChartOfAccounts> accounts,
            Map<String, List<AsChartOfAccountsEntity>> entitiesByCoaMap,
            Map<String, List<AsChartOfAccountsEntry>> entriesByEntityMap) {
        
        List<CoaTreeNodeDto> accNodes = new ArrayList<>();
        for (AsChartOfAccounts acc : accounts) {
            String label = (acc.getACCOUNTNUMBER() != null ? acc.getACCOUNTNUMBER() : "GL") + 
                           (acc.getACCOUNTDESCRIPTION() != null ? " - " + acc.getACCOUNTDESCRIPTION() : "");
            CoaTreeNodeDto accNode = new CoaTreeNodeDto(acc.getCHARTOFACCOUNTSGUID(), label, "account", "receipt_long");
            
            Map<String, Object> details = new HashMap<>();
            details.put("accountNumber", acc.getACCOUNTNUMBER());
            details.put("description", acc.getACCOUNTDESCRIPTION());
            details.put("coaGuid", acc.getCHARTOFACCOUNTSGUID());
            accNode.setDetails(details);

            List<AsChartOfAccountsEntity> entities = entitiesByCoaMap.getOrDefault(acc.getCHARTOFACCOUNTSGUID(), Collections.emptyList());
            List<CoaTreeNodeDto> txnChildren = new ArrayList<>();
            for (AsChartOfAccountsEntity ent : entities) {
                // Transaction node directly under account (representing the entity link)
                String entityCode = ent.getCHARTOFACCOUNTSENTITYCODE() != null ? ent.getCHARTOFACCOUNTSENTITYCODE() : "01";
                String txnLabel = ent.getTRANSACTIONNAME() != null ? ent.getTRANSACTIONNAME() : "Transaction";
                CoaTreeNodeDto txnNode = new CoaTreeNodeDto(
                    ent.getCHARTOFACCOUNTSENTITYGUID(), txnLabel, "transaction", "swap_horiz");
                
                Map<String, Object> txnDetails = new HashMap<>();
                txnDetails.put("entityGuid", ent.getCHARTOFACCOUNTSENTITYGUID());
                txnDetails.put("entityCode", entityCode);
                txnDetails.put("transactionName", ent.getTRANSACTIONNAME());
                txnDetails.put("suspense", false);
                txnNode.setDetails(txnDetails);

                 List<AsChartOfAccountsEntry> entries = entriesByEntityMap.getOrDefault(ent.getCHARTOFACCOUNTSENTITYGUID(), Collections.emptyList());
                 List<CoaTreeNodeDto> entryChildren = new ArrayList<>();
                 for (AsChartOfAccountsEntry en : entries) {
                     String typeCode = en.getACCOUNTINGTYPECODE() != null ? en.getACCOUNTINGTYPECODE().trim() : "";
                     String enLabel = null;
                     if ("03".equals(typeCode)) {
                         enLabel = en.getACCOUNTINGAMOUNTFIELD();
                     } else if ("01".equals(typeCode)) {
                         enLabel = en.getFUNDTYPECODE() != null ? "Fund Type: " + en.getFUNDTYPECODE().trim() : null;
                     } else if ("02".equals(typeCode)) {
                         enLabel = en.getORIGINALDISBURSEMENTSTATUSCODE() != null ? "Disbursement Status: " + en.getORIGINALDISBURSEMENTSTATUSCODE().trim() : null;
                     }
                     if (enLabel == null || enLabel.trim().isEmpty()) {
                         enLabel = en.getENTRYDESCRIPTION() != null ? en.getENTRYDESCRIPTION() : "Accounting Entry";
                     }
                     CoaTreeNodeDto enNode = new CoaTreeNodeDto(en.getCHARTOFACCOUNTSENTRYGUID(), enLabel, "entry", "analytics");
                     entryChildren.add(enNode);
                 }
                txnNode.setChildren(entryChildren);
                txnChildren.add(txnNode);
            }
            accNode.setChildren(txnChildren);
            accNodes.add(accNode);
        }
        return accNodes;
    }

    private String formatOracleTimestamp(String dateStr, String defaultValue) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            java.time.temporal.TemporalAccessor ta = java.time.format.DateTimeFormatter.ISO_DATE_TIME.parseBest(
                dateStr, 
                java.time.ZonedDateTime::from, 
                java.time.LocalDateTime::from, 
                java.time.LocalDate::from
            );
            
            java.time.LocalDateTime ldt;
            if (ta instanceof java.time.ZonedDateTime) {
                ldt = ((java.time.ZonedDateTime) ta).withZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDateTime();
            } else if (ta instanceof java.time.LocalDateTime) {
                ldt = (java.time.LocalDateTime) ta;
            } else {
                ldt = ((java.time.LocalDate) ta).atStartOfDay();
            }

            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yy hh:mm:ss.000000000 a", Locale.ENGLISH);
            String formattedDate = ldt.format(formatter).toUpperCase();
            return String.format("to_timestamp('%s','DD-MM-RR fmHH12:fmMI:SSXFF AM')", formattedDate);
        } catch (Exception e) {
            if (dateStr.toLowerCase().contains("to_timestamp") || "null".equalsIgnoreCase(dateStr)) {
                return dateStr;
            }
            return String.format("to_timestamp('%s','DD-MM-RR fmHH12:fmMI:SSXFF AM')", esc(dateStr));
        }
    }

    private java.sql.Timestamp parseTimestamp(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty() || "null".equalsIgnoreCase(dateStr)) {
            return null;
        }
        try {
            java.time.temporal.TemporalAccessor ta = java.time.format.DateTimeFormatter.ISO_DATE_TIME.parseBest(
                dateStr, 
                java.time.ZonedDateTime::from, 
                java.time.LocalDateTime::from, 
                java.time.LocalDate::from
            );
            
            java.time.LocalDateTime ldt;
            if (ta instanceof java.time.ZonedDateTime) {
                ldt = ((java.time.ZonedDateTime) ta).withZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDateTime();
            } else if (ta instanceof java.time.LocalDateTime) {
                ldt = (java.time.LocalDateTime) ta;
            } else {
                ldt = ((java.time.LocalDate) ta).atStartOfDay();
            }
            return java.sql.Timestamp.valueOf(ldt);
        } catch (Exception e) {
            return null;
        }
    }

    private String valOrNull(String val) {
        if (val == null || val.trim().isEmpty() || "null".equalsIgnoreCase(val)) {
            return "null";
        }
        return "'" + esc(val) + "'";
    }

    public CoaSaveResponseDto generateSaveScripts(CoaWizardDataDto dto) {
        List<String> scripts = new ArrayList<>();
        boolean isEdit = Boolean.TRUE.equals(dto.getEditMode());
        
        String coaGuid = isEdit ? dto.getCoaGuid() : UUID.randomUUID().toString().toUpperCase();
        String entityGuid = isEdit ? dto.getEntityGuid() : UUID.randomUUID().toString().toUpperCase();
        String entryGuid = isEdit ? dto.getEntryGuid() : UUID.randomUUID().toString().toUpperCase();

        String companyGuid = "DD92D03C-8F7F-4C97-A483-A6BA4C1AE802"; // For Britam Holdings (keep for now)
        String debitCreditCode = "Credit".equalsIgnoreCase(dto.getCreditDebit()) ? "02" : "01";
        
        String transactionName = dto.getTransaction();
        if (transactionName != null && transactionName.contains(" (")) {
            transactionName = transactionName.substring(0, transactionName.indexOf(" (")).trim();
        }
        
        String accountingTypeCode = "01";
        String accountingAmount = null;
        String fundType = null;
        String originalDisbursementStatus = null;

        if ("MathVariable".equalsIgnoreCase(dto.getType())) {
            accountingTypeCode = "03";
            accountingAmount = dto.getAccountingAmount();
        } else if ("By Fund".equalsIgnoreCase(dto.getType())) {
            accountingTypeCode = "01";
            fundType = dto.getFundType();
        } else if ("Disbursement".equalsIgnoreCase(dto.getType())) {
            accountingTypeCode = "02";
            originalDisbursementStatus = dto.getOriginalDisbursementStatus();
        } else if ("GeneralLedger".equalsIgnoreCase(dto.getType())) {
            accountingTypeCode = "04";
        }

        String gainLoss = Boolean.TRUE.equals(dto.getGainLoss()) ? "1" : "0";
        String flipOnNeg = Boolean.TRUE.equals(dto.getFlipOnNegative()) ? "1" : "0";
        String revAcc = Boolean.TRUE.equals(dto.getDoReversalAccounting()) ? "1" : "0";
        String suspense = Boolean.TRUE.equals(dto.getSuspense()) ? "1" : "0";

        String effFrom = formatOracleTimestamp(dto.getEffectiveFromDate(), "to_timestamp('01-01-90 12:00:00.000000000 AM','DD-MM-RR fmHH12:fmMI:SSXFF AM')");
        String effTo = formatOracleTimestamp(dto.getEffectiveToDate(), "null");

        if (!isEdit) {
            // Check if the account number already exists
            boolean accountExists = false;
            if (dto.getAccountNumber() != null && !dto.getAccountNumber().isEmpty()) {
                java.util.Optional<AsChartOfAccounts> existing = accountsRepository.findByACCOUNTNUMBER(dto.getAccountNumber());
                if (existing.isPresent()) {
                    coaGuid = existing.get().getCHARTOFACCOUNTSGUID();
                    accountExists = true;
                }
            } else if (dto.getExistingAccountGuid() != null && !dto.getExistingAccountGuid().isEmpty()) {
                coaGuid = dto.getExistingAccountGuid();
                accountExists = true;
            }

            int lastStep = dto.getLastConfiguredStep() != null ? dto.getLastConfiguredStep() : 5;

            if (!accountExists) {
                scripts.add(String.format("INSERT INTO ASCHARTOFACCOUNTS (CHARTOFACCOUNTSGUID, COMPANYGUID, ACCOUNTNUMBER, ACCOUNTDESCRIPTION) VALUES ('%s', '%s', '%s', '%s');", esc(coaGuid), esc(companyGuid), esc(dto.getAccountNumber()), esc(dto.getAccountDescription())));
            }

            if (lastStep >= 2) {
                scripts.add(String.format("INSERT INTO ASCHARTOFACCOUNTSENTITY (CHARTOFACCOUNTSENTITYGUID, CHARTOFACCOUNTSGUID, CHARTOFACCOUNTSENTITYCODE, TRANSACTIONNAME) VALUES ('%s', '%s', '01', '%s');", esc(entityGuid), esc(coaGuid), esc(transactionName)));
            }

            if (lastStep >= 3) {
                scripts.add(String.format(
                    "INSERT INTO ASCHARTOFACCOUNTSENTRY (CHARTOFACCOUNTSENTRYGUID, CHARTOFACCOUNTSENTITYGUID, DEBITCREDITCODE, ENTRYDESCRIPTION, ACCOUNTINGTYPECODE, ACCOUNTINGAMOUNTFIELD, GAINLOSSFLAG, FLIPONNEGATIVEFLAG, EFFECTIVEFROMDATE, EFFECTIVETODATE, DOREVERSALACCOUNTINGFLAG, ORIGINALDISBURSEMENTSTATUSCODE, FUNDTYPECODE, ACCOUNTNUMBERFORMAT, LINKSUSPENSEFLAG) " +
                    "VALUES ('%s', '%s', '%s', '%s', '%s', %s, '%s', '%s', %s, %s, '%s', %s, %s, null, '%s');",
                    esc(entryGuid), esc(entityGuid), esc(debitCreditCode), esc(dto.getEntryDescription()), esc(accountingTypeCode), valOrNull(accountingAmount), 
                    esc(gainLoss), esc(flipOnNeg), effFrom, effTo, esc(revAcc), valOrNull(originalDisbursementStatus), valOrNull(fundType), esc(suspense)
                ));
            }
            
            if (lastStep >= 4) {
                String criteriaValue = Boolean.TRUE.equals(dto.getWriteAccounting()) ? "01" : "00";
                scripts.add(String.format("INSERT INTO ASCHARTOFACCOUNTSCRITERIA (CHARTOFACCOUNTSENTRYGUID, CRITERIANAME, CRITERIAVALUE) VALUES ('%s', 'WriteAccounting', '%s');", esc(entryGuid), criteriaValue));
            }

            if (lastStep >= 5 && dto.getResultSelections() != null) {
                CoaResultSelectionsDto rDto = dto.getResultSelections();
                List<String> resultsToSave = new ArrayList<>();
                if (rDto.getBranchSection() != null && !rDto.getBranchSection().isEmpty()) resultsToSave.add(rDto.getBranchSection());
                if (rDto.getDepartmentSection() != null && !rDto.getDepartmentSection().isEmpty()) resultsToSave.add(rDto.getDepartmentSection());
                if (rDto.getProductSection() != null && !rDto.getProductSection().isEmpty()) resultsToSave.add(rDto.getProductSection());
                if (rDto.getChannelSection() != null && !rDto.getChannelSection().isEmpty()) resultsToSave.add(rDto.getChannelSection());
                if (rDto.getLobSection() != null && !rDto.getLobSection().isEmpty()) resultsToSave.add(rDto.getLobSection());
                if (Boolean.TRUE.equals(rDto.getCompany())) resultsToSave.add("Company");
                if (Boolean.TRUE.equals(rDto.getDefaultBranchID())) resultsToSave.add("DefaultBranchID");

                for (String rName : resultsToSave) {
                    scripts.add(String.format("INSERT INTO ASCHARTOFACCOUNTSRESULT (CHARTOFACCOUNTSENTRYGUID, RESULTNAME) VALUES ('%s', '%s');", esc(entryGuid), esc(rName)));
                }
            }
        } else {
            java.util.Optional<AsChartOfAccounts> existingAccOpt = accountsRepository.findById(coaGuid);
            if (existingAccOpt.isPresent()) {
                AsChartOfAccounts ex = existingAccOpt.get();
                if (!Objects.equals(ex.getACCOUNTNUMBER(), dto.getAccountNumber()) || !Objects.equals(ex.getACCOUNTDESCRIPTION(), dto.getAccountDescription())) {
                    scripts.add(String.format("UPDATE ASCHARTOFACCOUNTS SET ACCOUNTNUMBER='%s', ACCOUNTDESCRIPTION='%s' WHERE CHARTOFACCOUNTSGUID='%s';", esc(dto.getAccountNumber()), esc(dto.getAccountDescription()), esc(coaGuid)));
                }
            }

            java.util.Optional<AsChartOfAccountsEntity> existingEntOpt = entityRepository.findById(entityGuid);
            if (existingEntOpt.isPresent()) {
                AsChartOfAccountsEntity ex = existingEntOpt.get();
                if (!Objects.equals(ex.getTRANSACTIONNAME(), transactionName)) {
                    scripts.add(String.format("UPDATE ASCHARTOFACCOUNTSENTITY SET TRANSACTIONNAME='%s' WHERE CHARTOFACCOUNTSENTITYGUID='%s';", esc(transactionName), esc(entityGuid)));
                }
            }

            java.util.Optional<AsChartOfAccountsEntry> existingEntryOpt = entryRepository.findById(entryGuid);
            if (existingEntryOpt.isPresent()) {
                AsChartOfAccountsEntry ex = existingEntryOpt.get();
                
                java.sql.Timestamp newEffFrom = parseTimestamp(dto.getEffectiveFromDate());
                if (newEffFrom == null) {
                    newEffFrom = java.sql.Timestamp.valueOf(java.time.LocalDateTime.of(1990, 1, 1, 0, 0));
                }
                java.sql.Timestamp newEffTo = parseTimestamp(dto.getEffectiveToDate());

                boolean changed = !Objects.equals(ex.getDEBITCREDITCODE(), debitCreditCode)
                               || !Objects.equals(ex.getENTRYDESCRIPTION(), dto.getEntryDescription())
                               || !Objects.equals(ex.getACCOUNTINGTYPECODE(), accountingTypeCode)
                               || !Objects.equals(ex.getACCOUNTINGAMOUNTFIELD(), accountingAmount)
                               || !Objects.equals(ex.getGAINLOSSFLAG(), gainLoss)
                               || !Objects.equals(ex.getFLIPONNEGATIVEFLAG(), flipOnNeg)
                               || !Objects.equals(ex.getDOREVERSALACCOUNTINGFLAG(), revAcc)
                               || !Objects.equals(ex.getORIGINALDISBURSEMENTSTATUSCODE(), originalDisbursementStatus)
                               || !Objects.equals(ex.getFUNDTYPECODE(), fundType)
                               || !Objects.equals(ex.getLINKSUSPENSEFLAG(), suspense)
                               || !Objects.equals(ex.getEFFECTIVEFROMDATE(), newEffFrom)
                               || !Objects.equals(ex.getEFFECTIVETODATE(), newEffTo);

                if (changed) {
                    scripts.add(String.format(
                        "UPDATE ASCHARTOFACCOUNTSENTRY SET DEBITCREDITCODE='%s', ENTRYDESCRIPTION='%s', ACCOUNTINGTYPECODE='%s', ACCOUNTINGAMOUNTFIELD=%s, GAINLOSSFLAG='%s', FLIPONNEGATIVEFLAG='%s', EFFECTIVEFROMDATE=%s, EFFECTIVETODATE=%s, DOREVERSALACCOUNTINGFLAG='%s', ORIGINALDISBURSEMENTSTATUSCODE=%s, FUNDTYPECODE=%s, LINKSUSPENSEFLAG='%s' WHERE CHARTOFACCOUNTSENTRYGUID='%s';", 
                        esc(debitCreditCode), esc(dto.getEntryDescription()), esc(accountingTypeCode), valOrNull(accountingAmount), esc(gainLoss), esc(flipOnNeg), effFrom, effTo, esc(revAcc), valOrNull(originalDisbursementStatus), valOrNull(fundType), esc(suspense), esc(entryGuid)
                    ));
                }
            }
            
            List<AsChartOfAccountsCriteria> currentCriteria = criteriaRepository.findByCHARTOFACCOUNTSENTRYGUID(entryGuid);
            boolean hasWriteAcc = currentCriteria.stream().anyMatch(c -> "WriteAccounting".equals(c.getCRITERIANAME()));
            String criteriaValue = Boolean.TRUE.equals(dto.getWriteAccounting()) ? "01" : "00";
            
            if (hasWriteAcc) {
                boolean isDifferentValue = currentCriteria.stream()
                    .anyMatch(c -> "WriteAccounting".equals(c.getCRITERIANAME()) && !criteriaValue.equals(c.getCRITERIAVALUE()));
                if (isDifferentValue) {
                    scripts.add(String.format("UPDATE ASCHARTOFACCOUNTSCRITERIA SET CRITERIAVALUE='%s' WHERE CHARTOFACCOUNTSENTRYGUID='%s' AND CRITERIANAME='WriteAccounting';", criteriaValue, esc(entryGuid)));
                }
            } else {
                scripts.add(String.format("INSERT INTO ASCHARTOFACCOUNTSCRITERIA (CHARTOFACCOUNTSENTRYGUID, CRITERIANAME, CRITERIAVALUE) VALUES ('%s', 'WriteAccounting', '%s');", esc(entryGuid), criteriaValue));
            }

            List<AsChartOfAccountsResult> currentResults = resultRepository.findByCHARTOFACCOUNTSENTRYGUID(entryGuid);
            Set<String> currentResultNames = new HashSet<>();
            for (AsChartOfAccountsResult r : currentResults) {
                if (r.getRESULTNAME() != null) {
                    currentResultNames.add(r.getRESULTNAME());
                }
            }

            Set<String> resultsToSave = new HashSet<>();
            if (dto.getResultSelections() != null) {
                CoaResultSelectionsDto rDto = dto.getResultSelections();
                if (rDto.getBranchSection() != null && !rDto.getBranchSection().isEmpty()) resultsToSave.add(rDto.getBranchSection());
                if (rDto.getDepartmentSection() != null && !rDto.getDepartmentSection().isEmpty()) resultsToSave.add(rDto.getDepartmentSection());
                if (rDto.getProductSection() != null && !rDto.getProductSection().isEmpty()) resultsToSave.add(rDto.getProductSection());
                if (rDto.getChannelSection() != null && !rDto.getChannelSection().isEmpty()) resultsToSave.add(rDto.getChannelSection());
                if (rDto.getLobSection() != null && !rDto.getLobSection().isEmpty()) resultsToSave.add(rDto.getLobSection());
                if (Boolean.TRUE.equals(rDto.getCompany())) resultsToSave.add("Company");
                if (Boolean.TRUE.equals(rDto.getDefaultBranchID())) resultsToSave.add("DefaultBranchID");
            }

            for (String currentRes : currentResultNames) {
                if (!resultsToSave.contains(currentRes)) {
                    scripts.add(String.format("DELETE FROM ASCHARTOFACCOUNTSRESULT WHERE CHARTOFACCOUNTSENTRYGUID='%s' AND RESULTNAME='%s';", esc(entryGuid), esc(currentRes)));
                }
            }
            for (String targetRes : resultsToSave) {
                if (!currentResultNames.contains(targetRes)) {
                    scripts.add(String.format("INSERT INTO ASCHARTOFACCOUNTSRESULT (CHARTOFACCOUNTSENTRYGUID, RESULTNAME) VALUES ('%s', '%s');", esc(entryGuid), esc(targetRes)));
                }
            }
        }
        return new CoaSaveResponseDto(scripts, "SQL scripts generated successfully.");
    }

    public List<AsChartOfAccountsEntity> getEntitiesByTransactionName(String transactionName) {
        return entityRepository.findByTRANSACTIONNAME(transactionName);
    }

    public boolean checkAccountExists(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return false;
        }
        return accountsRepository.findByACCOUNTNUMBER(accountNumber.trim()).isPresent();
    }

    public List<CoaCodeValueDto> getTransactions() {
        List<com.example.entity.AsProduct> products = productRepository.findAll();
        Map<String, String> productMap = new HashMap<>();
        for (com.example.entity.AsProduct p : products) {
            if (p.getPRODUCTGUID() != null && p.getPRODUCTNAME() != null) {
                productMap.put(p.getPRODUCTGUID(), p.getPRODUCTNAME());
            }
        }

        List<com.example.entity.AsPlan> plans = planRepository.findAll();
        Map<String, String> planMap = new HashMap<>();
        for (com.example.entity.AsPlan p : plans) {
            if (p.getPLANGUID() != null && p.getPLANNAME() != null) {
                planMap.put(p.getPLANGUID(), p.getPLANNAME());
            }
        }

        return transactionRepository.findAll().stream()
                .map(t -> {
                    String displayName = t.getTRANSACTIONNAME();
                    if (t.getPLANGUID() != null && planMap.containsKey(t.getPLANGUID())) {
                        displayName += " (" + planMap.get(t.getPLANGUID()) + ")";
                    } else if (t.getPRODUCTGUID() != null && productMap.containsKey(t.getPRODUCTGUID())) {
                        displayName += " (" + productMap.get(t.getPRODUCTGUID()) + ")";
                    }
                    return new CoaCodeValueDto(t.getTRANSACTIONGUID(), displayName);
                })
                .collect(java.util.stream.Collectors.toList());
    }

    public CoaFullConfigDto getFullConfig(String entryGuid) {
        CoaFullConfigDto dto = new CoaFullConfigDto();
        dto.setEntryGuid(entryGuid);
        
        Optional<AsChartOfAccountsEntry> entryOpt = entryRepository.findById(entryGuid);
        if (entryOpt.isPresent()) {
            AsChartOfAccountsEntry en = entryOpt.get();
            dto.setEntityGuid(en.getCHARTOFACCOUNTSENTITYGUID());
            dto.setDebitCreditCode(en.getDEBITCREDITCODE());
            dto.setDebitCreditLabel("02".equals(en.getDEBITCREDITCODE()) ? "Credit" : "Debit");
            dto.setEntryDescription(en.getENTRYDESCRIPTION());
            dto.setAccountingTypeCode(en.getACCOUNTINGTYPECODE());
            String label = "By Fund";
            if ("03".equals(en.getACCOUNTINGTYPECODE())) {
                label = "MathVariable";
            } else if ("02".equals(en.getACCOUNTINGTYPECODE())) {
                label = "Disbursement";
            } else if ("04".equals(en.getACCOUNTINGTYPECODE())) {
                label = "GeneralLedger";
            }
            dto.setAccountingTypeLabel(label);
            dto.setAccountingAmountField(en.getACCOUNTINGAMOUNTFIELD());
            dto.setGainLossFlag(en.getGAINLOSSFLAG());
            dto.setFlipOnNegativeFlag(en.getFLIPONNEGATIVEFLAG());
            dto.setDoReversalAccountingFlag(en.getDOREVERSALACCOUNTINGFLAG());
            dto.setOriginalDisbursementStatusCode(en.getORIGINALDISBURSEMENTSTATUSCODE());
            dto.setFundTypeCode(en.getFUNDTYPECODE());
            dto.setLinkSuspenseFlag(en.getLINKSUSPENSEFLAG());
            dto.setEffectiveFromDate(en.getEFFECTIVEFROMDATE() != null ? en.getEFFECTIVEFROMDATE().toString() : null);
            dto.setEffectiveToDate(en.getEFFECTIVETODATE() != null ? en.getEFFECTIVETODATE().toString() : null);
            
            Optional<AsChartOfAccountsEntity> entityOpt = entityRepository.findById(en.getCHARTOFACCOUNTSENTITYGUID());
            if (entityOpt.isPresent()) {
                AsChartOfAccountsEntity ent = entityOpt.get();
                dto.setCoaGuid(ent.getCHARTOFACCOUNTSGUID());
                dto.setEntityCode(ent.getCHARTOFACCOUNTSENTITYCODE());
                dto.setTransactionName(ent.getTRANSACTIONNAME());
                
                Optional<AsChartOfAccounts> coaOpt = accountsRepository.findById(ent.getCHARTOFACCOUNTSGUID());
                if (coaOpt.isPresent()) {
                    AsChartOfAccounts coa = coaOpt.get();
                    dto.setAccountNumber(coa.getACCOUNTNUMBER());
                    dto.setAccountDescription(coa.getACCOUNTDESCRIPTION());
                    dto.setCompanyGuid(coa.getCOMPANYGUID());
                }
            }
            
            List<AsChartOfAccountsCriteria> criteria = criteriaRepository.findByCHARTOFACCOUNTSENTRYGUID(entryGuid);
            List<CoaCriteriaDto> criteriaDtos = criteria.stream().map(c -> {
                CoaCriteriaDto cDto = new CoaCriteriaDto();
                cDto.setCriteria(c.getCRITERIANAME());
                cDto.setValue(c.getCRITERIAVALUE());
                return cDto;
            }).collect(java.util.stream.Collectors.toList());
            dto.setCriteriaList(criteriaDtos);
            
            List<AsChartOfAccountsResult> results = resultRepository.findByCHARTOFACCOUNTSENTRYGUID(entryGuid);
            dto.setResultList(results.stream().map(AsChartOfAccountsResult::getRESULTNAME).collect(java.util.stream.Collectors.toList()));
        }
        return dto;
    }

    public CoaFullConfigDto getFullConfigByEntity(String entityGuid) {
        CoaFullConfigDto dto = new CoaFullConfigDto();
        dto.setEntityGuid(entityGuid);
        
        Optional<AsChartOfAccountsEntity> entityOpt = entityRepository.findById(entityGuid);
        if (entityOpt.isPresent()) {
            AsChartOfAccountsEntity ent = entityOpt.get();
            dto.setCoaGuid(ent.getCHARTOFACCOUNTSGUID());
            dto.setEntityCode(ent.getCHARTOFACCOUNTSENTITYCODE());
            dto.setTransactionName(ent.getTRANSACTIONNAME());
            
            Optional<AsChartOfAccounts> coaOpt = accountsRepository.findById(ent.getCHARTOFACCOUNTSGUID());
            if (coaOpt.isPresent()) {
                AsChartOfAccounts coa = coaOpt.get();
                dto.setAccountNumber(coa.getACCOUNTNUMBER());
                dto.setAccountDescription(coa.getACCOUNTDESCRIPTION());
                dto.setCompanyGuid(coa.getCOMPANYGUID());
            }
            
            List<AsChartOfAccountsEntry> entries = entryRepository.findByCHARTOFACCOUNTSENTITYGUID(entityGuid);
            if (entries != null && !entries.isEmpty()) {
                AsChartOfAccountsEntry en = entries.get(0);
                String entryGuid = en.getCHARTOFACCOUNTSENTRYGUID();
                dto.setEntryGuid(entryGuid);
                dto.setDebitCreditCode(en.getDEBITCREDITCODE());
                dto.setDebitCreditLabel("02".equals(en.getDEBITCREDITCODE()) ? "Credit" : "Debit");
                dto.setEntryDescription(en.getENTRYDESCRIPTION());
                dto.setAccountingTypeCode(en.getACCOUNTINGTYPECODE());
                String label = "By Fund";
                if ("03".equals(en.getACCOUNTINGTYPECODE())) {
                    label = "MathVariable";
                } else if ("02".equals(en.getACCOUNTINGTYPECODE())) {
                    label = "Disbursement";
                } else if ("04".equals(en.getACCOUNTINGTYPECODE())) {
                    label = "GeneralLedger";
                }
                dto.setAccountingTypeLabel(label);
                dto.setAccountingAmountField(en.getACCOUNTINGAMOUNTFIELD());
                dto.setGainLossFlag(en.getGAINLOSSFLAG());
                dto.setFlipOnNegativeFlag(en.getFLIPONNEGATIVEFLAG());
                dto.setDoReversalAccountingFlag(en.getDOREVERSALACCOUNTINGFLAG());
                dto.setOriginalDisbursementStatusCode(en.getORIGINALDISBURSEMENTSTATUSCODE());
                dto.setFundTypeCode(en.getFUNDTYPECODE());
                dto.setLinkSuspenseFlag(en.getLINKSUSPENSEFLAG());
                dto.setEffectiveFromDate(en.getEFFECTIVEFROMDATE() != null ? en.getEFFECTIVEFROMDATE().toString() : null);
                dto.setEffectiveToDate(en.getEFFECTIVETODATE() != null ? en.getEFFECTIVETODATE().toString() : null);
                
                List<AsChartOfAccountsCriteria> criteria = criteriaRepository.findByCHARTOFACCOUNTSENTRYGUID(entryGuid);
                List<CoaCriteriaDto> criteriaDtos = criteria.stream().map(c -> {
                    CoaCriteriaDto cDto = new CoaCriteriaDto();
                    cDto.setCriteria(c.getCRITERIANAME());
                    cDto.setValue(c.getCRITERIAVALUE());
                    return cDto;
                }).collect(java.util.stream.Collectors.toList());
                dto.setCriteriaList(criteriaDtos);
                
                List<AsChartOfAccountsResult> results = resultRepository.findByCHARTOFACCOUNTSENTRYGUID(entryGuid);
                dto.setResultList(results.stream().map(AsChartOfAccountsResult::getRESULTNAME).collect(java.util.stream.Collectors.toList()));
            }
        }
        return dto;
    }

    private String esc(String val) {
        if (val == null) return "";
        return val.replace("'", "''");
    }

    private List<CoaTreeNodeDto> getFallbackMockHierarchy() {
        List<CoaTreeNodeDto> treeNodes = new ArrayList<>();
        CoaTreeNodeDto rootNode = new CoaTreeNodeDto("ROOT-1", "Chart Of Accounts", "root", "account_balance_wallet");

        CoaTreeNodeDto companyNode = new CoaTreeNodeDto("COMP-1", "Britam Holdings", "company", "domain");
        CoaTreeNodeDto accNode = new CoaTreeNodeDto("ACC-510116", "510116 - Premium Waiver", "account", "receipt_long");
        
        Map<String, Object> accDetails = new HashMap<>();
        accDetails.put("accountNumber", "510116");
        accDetails.put("description", "Premium Waiver Account");
        accDetails.put("coaGuid", "ACC-510116");
        accNode.setDetails(accDetails);

        // Transaction node directly under account (representing the entity link)
        CoaTreeNodeDto txnNode = new CoaTreeNodeDto("ENT-1", "WoPPayment", "transaction", "swap_horiz");
        Map<String, Object> txnDetails = new HashMap<>();
        txnDetails.put("entityGuid", "ENT-1");
        txnDetails.put("entityCode", "01");
        txnDetails.put("transactionName", "WoPPayment");
        txnDetails.put("suspense", false);
        txnNode.setDetails(txnDetails);

        CoaTreeNodeDto enNode = new CoaTreeNodeDto("EN-1", "Premium Tax Credit Entry", "entry", "analytics");

        txnNode.setChildren(Collections.singletonList(enNode));
        accNode.setChildren(Collections.singletonList(txnNode));
        companyNode.setChildren(Collections.singletonList(accNode));
        rootNode.setChildren(Collections.singletonList(companyNode));

        treeNodes.add(rootNode);
        return treeNodes;
    }
}
