package com.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.security.*;
import com.example.entity.security.*;
import com.example.entity.AsCompany;
import com.example.entity.AsAuthPage;
import com.example.entity.AsAuthButton;
import com.example.entity.AsAuthWebService;
import com.example.entity.AsInquiryScreen;
import com.example.entity.AsProduct;
import com.example.entity.AsTransaction;
import com.example.entity.AsPlan;
import com.example.secondaryDev.repository.*;

/**
 * Read-only service for the OIPA Security Tool.
 * <p>
 * All reads target the secondarydev database exclusively.
 * No INSERT / UPDATE / DELETE is ever executed — the generate-scripts
 * endpoint only <b>returns</b> raw SQL strings.
 */
@Service
public class SecurityGroupService {

    private static final Logger log = LoggerFactory.getLogger(SecurityGroupService.class);

    // ── Repositories (all wired to secondarydev) ────────────────────────
    @Autowired private AsSecurityGroupRepository securityGroupRepo;
    @Autowired private AsCompanySecondaryDevRepository companyRepo;
    @Autowired private AsAuthPageSecondaryDevRepository pageRepo;
    @Autowired private AsAuthButtonSecondaryDevRepository buttonRepo;
    @Autowired private AsAuthWebServiceSecondaryDevRepository webServiceRepo;
    @Autowired private AsAuthCompanyRepository authCompanyRepo;
    @Autowired private AsAuthCompanyPageRepository companyPageRepo;
    @Autowired private AsAuthCompanyPageButtonRepository companyPageButtonRepo;
    @Autowired private AsAuthCompanyInquiryRepository companyInquiryRepo;
    @Autowired private AsAuthCompanyWebServiceRepository companyWebServiceRepo;
    @Autowired private AsAuthPlanRepository authPlanRepo;
    @Autowired private AsAuthPlanPageRepository planPageRepo;
    @Autowired private AsAuthPlanPageButtonRepository planPageButtonRepo;
    @Autowired private AsAuthTransactionRepository transactionRepo;
    @Autowired private AsAuthPlanInquiryRepository planInquiryRepo;
    @Autowired private AsAuthProductRepository authProductRepo;
    @Autowired private AsAuthProductPageRepository productPageRepo;
    @Autowired private AsAuthProductPageButtonRepository productPageButtonRepo;
    @Autowired private AsAuthProductTransactionRepository productTransactionRepo;
    @Autowired private AsAuthTransactionButtonRepository transactionButtonRepo;
    @Autowired private AsAuthProductTransactionButtonRepository productTransactionButtonRepo;
    @Autowired private AsInquiryScreenRepository inquiryScreenRepo;
    @Autowired private AsProductSecondaryDevRepository productSecondaryDevRepo;
    @Autowired private AsTransactionSecondaryDevRepository transactionSecondaryDevRepo;
    @Autowired private AsPlanSecondaryDevRepository planSecondaryDevRepo;
    @Autowired @Qualifier("secondaryDevDataSource")
    private DataSource secondaryDevDataSource;



    // ===================================================================
    // 0.  GET /api/companies  —  list all companies
    // ===================================================================
    @Transactional(value = "secondaryDevTransactionManager", readOnly = true)
    public List<CompanyDto> getAllCompanies() {
        return companyRepo.findAll().stream()
                .map(c -> new CompanyDto(c.getCOMPANYGUID(), c.getCOMPANYNAME()))
                .collect(Collectors.toList());
    }

    // ===================================================================
    // 0b. GET /api/pages  —  list all pages
    // ===================================================================
    @Transactional(value = "secondaryDevTransactionManager", readOnly = true)
    public List<PageInfoDto> getAllPages() {
        return pageRepo.findAll().stream()
                .map(p -> new PageInfoDto(p.getAUTHPAGEGUID(), p.getPAGENAME()))
                .collect(Collectors.toList());
    }

    // ===================================================================
    // 0c. GET /api/buttons  —  list all buttons
    // ===================================================================
    @Transactional(value = "secondaryDevTransactionManager", readOnly = true)
    public List<ButtonInfoDto> getAllButtons() {
        return buttonRepo.findAll().stream()
                .map(b -> new ButtonInfoDto(b.getAUTHBUTTONGUID(), b.getBUTTONNAME()))
                .collect(Collectors.toList());
    }

    // ===================================================================
    // 0d. GET /api/webservices  —  list all webservices
    // ===================================================================
    @Transactional(value = "secondaryDevTransactionManager", readOnly = true)
    public List<WebServiceInfoDto> getAllWebServices() {
        return webServiceRepo.findAll().stream()
                .map(w -> new WebServiceInfoDto(w.getAUTHWEBSERVICEGUID(), w.getWEBSERVICENAME()))
                .collect(Collectors.toList());
    }

    // ===================================================================
    // 0e. GET /api/inquiry-screens  —  list inquiry screens
    // ===================================================================
    @Transactional(value = "secondaryDevTransactionManager", readOnly = true)
    public List<InquiryScreenDto> getInquiryScreens(String companyGuid, String planGuid) {
        List<AsInquiryScreen> screens;
        if (companyGuid != null && !companyGuid.trim().isEmpty()) {
            screens = inquiryScreenRepo.findByCOMPANYGUID(companyGuid);
        } else if (planGuid != null && !planGuid.trim().isEmpty()) {
            screens = inquiryScreenRepo.findByPLANGUID(planGuid);
        } else {
            screens = inquiryScreenRepo.findAll();
        }
        return screens.stream()
                .map(s -> new InquiryScreenDto(
                        s.getINQUIRYSCREENNAMEGUID() != null && !s.getINQUIRYSCREENNAMEGUID().trim().isEmpty()
                                ? s.getINQUIRYSCREENNAMEGUID()
                                : s.getINQUIRYSCREENGUID(),
                        s.getSCREENNAME()))
                .collect(Collectors.toList());
    }

    // ===================================================================
    // 0f. GET /api/companies/{companyGuid}/products  —  list products by company
    // ===================================================================
    @Transactional(value = "secondaryDevTransactionManager", readOnly = true)
    public List<ProductDto> getProductsByCompany(String companyGuid) {
        return productSecondaryDevRepo.findByCOMPANYGUID(companyGuid).stream()
                .map(p -> new ProductDto(p.getPRODUCTGUID(), p.getPRODUCTNAME(), p.getCOMPANYGUID()))
                .collect(Collectors.toList());
    }

    // ===================================================================
    // 0g. GET /api/transactions  —  list transactions by product or plan
    // ===================================================================
    @Transactional(value = "secondaryDevTransactionManager", readOnly = true)
    public List<TransactionInfoDto> getTransactions(String productGuid, String planGuid) {
        List<AsTransaction> transactions;
        if (planGuid != null && !planGuid.trim().isEmpty()) {
            transactions = transactionSecondaryDevRepo.findByPLANGUID(planGuid);
        } else if (productGuid != null && !productGuid.trim().isEmpty()) {
            transactions = transactionSecondaryDevRepo.findByPRODUCTGUID(productGuid);
        } else {
            transactions = Collections.emptyList();
        }
        return transactions.stream()
                .map(t -> new TransactionInfoDto(t.getTRANSACTIONGUID(), t.getTRANSACTIONNAME()))
                .collect(Collectors.toList());
    }

    // ===================================================================
    // 0h. GET /api/companies/{companyGuid}/products/{productGuid}/plans  —  list plans by company and product
    // ===================================================================
    @Transactional(value = "secondaryDevTransactionManager", readOnly = true)
    public List<PlanDto> getPlansByCompanyAndProduct(String companyGuid, String productGuid) {
        return planSecondaryDevRepo.findByCOMPANYGUIDAndPRODUCTGUID(companyGuid, productGuid).stream()
                .map(p -> new PlanDto(p.getPLANGUID(), p.getPLANNAME(), p.getCOMPANYGUID(), p.getPRODUCTGUID()))
                .collect(Collectors.toList());
    }

    // ===================================================================
    // 0i. GET /api/companies/{companyGuid}/plans  —  list plans by company
    // ===================================================================
    @Transactional(value = "secondaryDevTransactionManager", readOnly = true)
    public List<PlanDto> getPlansByCompany(String companyGuid) {
        return planSecondaryDevRepo.findByCOMPANYGUID(companyGuid).stream()
                .map(p -> new PlanDto(p.getPLANGUID(), p.getPLANNAME(), p.getCOMPANYGUID(), p.getPRODUCTGUID()))
                .collect(Collectors.toList());
    }

    // ===================================================================
    // 1.  GET /api/security-groups  —  list all groups
    // ===================================================================
    @Transactional(value = "secondaryDevTransactionManager", readOnly = true)
    public List<SecurityGroupDto> getAllSecurityGroups() {
        return securityGroupRepo.findAll().stream()
                .map(g -> new SecurityGroupDto(g.getSECURITYGROUPGUID(), g.getGROUPNAME()))
                .collect(Collectors.toList());
    }

    // ===================================================================
    // 1b. POST /api/security-groups/create  —  generate GUID + INSERT SQL
    // ===================================================================
    public CreateGroupResponseDto createSecurityGroup(CreateGroupRequestDto request) {
        String groupName = request.getGroupName();
        log.info("Creating security group: {}", groupName);

        // Generate a GUID — SECURITYGROUPGUID is CHAR(144) in the DB
        String guid = java.util.UUID.randomUUID().toString().toUpperCase();

        // Build the INSERT script (never executed)
        String insertScript = String.format(
            "INSERT INTO ASSECURITYGROUP (SECURITYGROUPGUID, GROUPNAME) VALUES ('%s', '%s');",
            esc(guid), esc(groupName));

        log.info("Successfully created dry-run for security group {} with GUID {}", groupName, guid);
        return new CreateGroupResponseDto(guid, groupName, insertScript);
    }

    // ===================================================================
    // 2.  GET /api/security-group/{guid}  —  full nested config
    // ===================================================================
    @Transactional(value = "secondaryDevTransactionManager", readOnly = true)
    public SecurityGroupDto getSecurityConfiguration(String securityGroupGuid) {
        log.info("Fetching security configuration for group GUID: {}", securityGroupGuid);

        // Fetch the group header (or build an empty shell for new groups)
        Optional<AsSecurityGroup> groupOpt = securityGroupRepo.findById(securityGroupGuid);
        SecurityGroupDto dto = new SecurityGroupDto();
        dto.setSecurityGroupGuid(securityGroupGuid);
        dto.setGroupName(groupOpt.map(AsSecurityGroup::getGROUPNAME).orElse(""));

        if (!groupOpt.isPresent()) {
            log.warn("Security group not found with GUID: {}. Returning empty shell.", securityGroupGuid);
            return dto;
        }

        JdbcTemplate jdbc = new JdbcTemplate(secondaryDevDataSource);
        jdbc.setFetchSize(10000);

        class Row {
            private final Map<String, Object> map;
            Row(Map<String, Object> map) { this.map = map; }
            String get(String col) {
                Object val = map.get(col);
                return val == null ? "" : val.toString().trim();
            }
        }

        // ── Fetch all flat auth rows for this group hierarchically ─────────
        List<AsAuthCompany> companies = authCompanyRepo.findBySECURITYGROUPGUID(securityGroupGuid);

        List<Map<String, Object>> companyPagesRows = jdbc.queryForList(
            "select /*+ LEADING(c p) USE_NL(p) */ p.AUTHCOMPANYPAGEGUID, p.AUTHCOMPANYGUID, p.AUTHPAGEGUID " +
            "from ASAUTHCOMPANYPAGE p " +
            "join ASAUTHCOMPANY c on p.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "where c.SECURITYGROUPGUID = ?",
            securityGroupGuid
        );
        class CompanyPageProjection implements AsAuthCompanyPageRepository.Projection {
            private final Row r;
            CompanyPageProjection(Row r) { this.r = r; }
            public String getAUTHCOMPANYPAGEGUID() { return r.get("AUTHCOMPANYPAGEGUID"); }
            public String getAUTHCOMPANYGUID() { return r.get("AUTHCOMPANYGUID"); }
            public String getAUTHPAGEGUID() { return r.get("AUTHPAGEGUID"); }
        }
        List<AsAuthCompanyPageRepository.Projection> companyPages = companyPagesRows.stream()
                .map(m -> new CompanyPageProjection(new Row(m))).collect(Collectors.toList());

        List<Map<String, Object>> companyPageButtonsRows = jdbc.queryForList(
            "select /*+ LEADING(c p b) USE_NL(p b) */ b.AUTHCOMPANYPAGEGUID, b.AUTHBUTTONGUID " +
            "from ASAUTHCOMPANYPAGEBUTTON b " +
            "join ASAUTHCOMPANYPAGE p on b.AUTHCOMPANYPAGEGUID = p.AUTHCOMPANYPAGEGUID " +
            "join ASAUTHCOMPANY c on p.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "where c.SECURITYGROUPGUID = ?",
            securityGroupGuid
        );
        class CompanyPageButtonProjection implements AsAuthCompanyPageButtonRepository.Projection {
            private final Row r;
            CompanyPageButtonProjection(Row r) { this.r = r; }
            public String getAUTHCOMPANYPAGEGUID() { return r.get("AUTHCOMPANYPAGEGUID"); }
            public String getAUTHBUTTONGUID() { return r.get("AUTHBUTTONGUID"); }
        }
        List<AsAuthCompanyPageButtonRepository.Projection> companyPageButtons = companyPageButtonsRows.stream()
                .map(m -> new CompanyPageButtonProjection(new Row(m))).collect(Collectors.toList());

        List<Map<String, Object>> companyInquiriesRows = jdbc.queryForList(
            "select /*+ LEADING(c i) USE_NL(i) */ i.AUTHCOMPANYINQUIRYGUID, i.AUTHCOMPANYGUID, i.INQUIRYSCREENNAMEGUID " +
            "from ASAUTHCOMPANYINQUIRY i " +
            "join ASAUTHCOMPANY c on i.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "where c.SECURITYGROUPGUID = ?",
            securityGroupGuid
        );
        class CompanyInquiryProjection implements AsAuthCompanyInquiryRepository.Projection {
            private final Row r;
            CompanyInquiryProjection(Row r) { this.r = r; }
            public String getAUTHCOMPANYINQUIRYGUID() { return r.get("AUTHCOMPANYINQUIRYGUID"); }
            public String getAUTHCOMPANYGUID() { return r.get("AUTHCOMPANYGUID"); }
            public String getINQUIRYSCREENGUID() { return r.get("INQUIRYSCREENNAMEGUID"); }
        }
        List<AsAuthCompanyInquiryRepository.Projection> companyInquiries = companyInquiriesRows.stream()
                .map(m -> new CompanyInquiryProjection(new Row(m))).collect(Collectors.toList());

        List<Map<String, Object>> companyWebServicesRows = jdbc.queryForList(
            "select /*+ LEADING(c w) USE_NL(w) */ w.AUTHWEBSERVICEGUID, w.AUTHCOMPANYGUID " +
            "from ASAUTHCOMPANYWEBSERVICE w " +
            "join ASAUTHCOMPANY c on w.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "where c.SECURITYGROUPGUID = ?",
            securityGroupGuid
        );
        class CompanyWebServiceProjection implements AsAuthCompanyWebServiceRepository.Projection {
            private final Row r;
            CompanyWebServiceProjection(Row r) { this.r = r; }
            public String getAUTHCOMPANYGUID() { return r.get("AUTHCOMPANYGUID"); }
            public String getAUTHWEBSERVICEGUID() { return r.get("AUTHWEBSERVICEGUID"); }
        }
        List<AsAuthCompanyWebServiceRepository.Projection> companyWebServices = companyWebServicesRows.stream()
                .map(m -> new CompanyWebServiceProjection(new Row(m))).collect(Collectors.toList());

        List<Map<String, Object>> plansRows = jdbc.queryForList(
            "select /*+ LEADING(c p) USE_NL(p) */ p.AUTHPLANGUID, p.AUTHCOMPANYGUID, p.PLANGUID " +
            "from ASAUTHPLAN p " +
            "join ASAUTHCOMPANY c on p.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "where c.SECURITYGROUPGUID = ?",
            securityGroupGuid
        );
        class PlanProjection implements AsAuthPlanRepository.Projection {
            private final Row r;
            PlanProjection(Row r) { this.r = r; }
            public String getAUTHPLANGUID() { return r.get("AUTHPLANGUID"); }
            public String getAUTHCOMPANYGUID() { return r.get("AUTHCOMPANYGUID"); }
            public String getPLANGUID() { return r.get("PLANGUID"); }
        }
        List<AsAuthPlanRepository.Projection> plans = plansRows.stream()
                .map(m -> new PlanProjection(new Row(m))).collect(Collectors.toList());

        List<Map<String, Object>> planPagesRows = jdbc.queryForList(
            "select /*+ LEADING(c ap p) USE_NL(ap p) */ p.AUTHPLANPAGEGUID, p.AUTHPLANGUID, p.AUTHPAGEGUID " +
            "from ASAUTHPLANPAGE p " +
            "join ASAUTHPLAN ap on p.AUTHPLANGUID = ap.AUTHPLANGUID " +
            "join ASAUTHCOMPANY c on ap.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "where c.SECURITYGROUPGUID = ?",
            securityGroupGuid
        );
        class PlanPageProjection implements AsAuthPlanPageRepository.Projection {
            private final Row r;
            PlanPageProjection(Row r) { this.r = r; }
            public String getAUTHPLANPAGEGUID() { return r.get("AUTHPLANPAGEGUID"); }
            public String getAUTHPLANGUID() { return r.get("AUTHPLANGUID"); }
            public String getAUTHPAGEGUID() { return r.get("AUTHPAGEGUID"); }
        }
        List<AsAuthPlanPageRepository.Projection> planPages = planPagesRows.stream()
                .map(m -> new PlanPageProjection(new Row(m))).collect(Collectors.toList());

        List<Map<String, Object>> planPageButtonsRows = jdbc.queryForList(
            "select /*+ LEADING(c ap p b) USE_NL(ap p b) */ b.AUTHPLANPAGEGUID, b.AUTHBUTTONGUID " +
            "from ASAUTHPLANPAGEBUTTON b " +
            "join ASAUTHPLANPAGE p on b.AUTHPLANPAGEGUID = p.AUTHPLANPAGEGUID " +
            "join ASAUTHPLAN ap on p.AUTHPLANGUID = ap.AUTHPLANGUID " +
            "join ASAUTHCOMPANY c on ap.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "where c.SECURITYGROUPGUID = ?",
            securityGroupGuid
        );
        class PlanPageButtonProjection implements AsAuthPlanPageButtonRepository.Projection {
            private final Row r;
            PlanPageButtonProjection(Row r) { this.r = r; }
            public String getAUTHPLANPAGEGUID() { return r.get("AUTHPLANPAGEGUID"); }
            public String getAUTHBUTTONGUID() { return r.get("AUTHBUTTONGUID"); }
        }
        List<AsAuthPlanPageButtonRepository.Projection> planPageButtons = planPageButtonsRows.stream()
                .map(m -> new PlanPageButtonProjection(new Row(m))).collect(Collectors.toList());

        List<Map<String, Object>> transactionsRows = jdbc.queryForList(
            "select /*+ LEADING(c ap t) USE_NL(ap t) */ t.AUTHTRANSACTIONGUID, t.AUTHPLANGUID, t.TRANSACTIONGUID " +
            "from ASAUTHTRANSACTION t " +
            "join ASAUTHPLAN ap on t.AUTHPLANGUID = ap.AUTHPLANGUID " +
            "join ASAUTHCOMPANY c on ap.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "where c.SECURITYGROUPGUID = ?",
            securityGroupGuid
        );
        class TransactionProjection implements AsAuthTransactionRepository.Projection {
            private final Row r;
            TransactionProjection(Row r) { this.r = r; }
            public String getAUTHTRANSACTIONGUID() { return r.get("AUTHTRANSACTIONGUID"); }
            public String getAUTHPLANGUID() { return r.get("AUTHPLANGUID"); }
            public String getTRANSACTIONGUID() { return r.get("TRANSACTIONGUID"); }
        }
        List<AsAuthTransactionRepository.Projection> transactions = transactionsRows.stream()
                .map(m -> new TransactionProjection(new Row(m))).collect(Collectors.toList());

        List<Map<String, Object>> transactionButtonsRows = jdbc.queryForList(
            "select /*+ LEADING(c ap t b) USE_NL(ap t b) */ b.AUTHTRANSACTIONGUID, b.AUTHBUTTONGUID " +
            "from ASAUTHTRANSACTIONBUTTON b " +
            "join ASAUTHTRANSACTION t on b.AUTHTRANSACTIONGUID = t.AUTHTRANSACTIONGUID " +
            "join ASAUTHPLAN ap on t.AUTHPLANGUID = ap.AUTHPLANGUID " +
            "join ASAUTHCOMPANY c on ap.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "where c.SECURITYGROUPGUID = ?",
            securityGroupGuid
        );
        class TransactionButtonProjection implements AsAuthTransactionButtonRepository.Projection {
            private final Row r;
            TransactionButtonProjection(Row r) { this.r = r; }
            public String getAUTHTRANSACTIONGUID() { return r.get("AUTHTRANSACTIONGUID"); }
            public String getAUTHBUTTONGUID() { return r.get("AUTHBUTTONGUID"); }
        }
        List<AsAuthTransactionButtonRepository.Projection> transactionButtons = transactionButtonsRows.stream()
                .map(m -> new TransactionButtonProjection(new Row(m))).collect(Collectors.toList());

        List<Map<String, Object>> planInquiriesRows = jdbc.queryForList(
            "select /*+ LEADING(c ap i) USE_NL(ap i) */ i.AUTHPLANINQUIRYGUID, i.AUTHPLANGUID, i.INQUIRYSCREENNAMEGUID " +
            "from ASAUTHPLANINQUIRY i " +
            "join ASAUTHPLAN ap on i.AUTHPLANGUID = ap.AUTHPLANGUID " +
            "join ASAUTHCOMPANY c on ap.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "where c.SECURITYGROUPGUID = ?",
            securityGroupGuid
        );
        class PlanInquiryProjection implements AsAuthPlanInquiryRepository.Projection {
            private final Row r;
            PlanInquiryProjection(Row r) { this.r = r; }
            public String getAUTHPLANINQUIRYGUID() { return r.get("AUTHPLANINQUIRYGUID"); }
            public String getAUTHPLANGUID() { return r.get("AUTHPLANGUID"); }
            public String getINQUIRYSCREENGUID() { return r.get("INQUIRYSCREENNAMEGUID"); }
        }
        List<AsAuthPlanInquiryRepository.Projection> planInquiries = planInquiriesRows.stream()
                .map(m -> new PlanInquiryProjection(new Row(m))).collect(Collectors.toList());

        List<Map<String, Object>> productsRows = jdbc.queryForList(
            "select /*+ LEADING(c p) USE_NL(p) */ p.AUTHPRODUCTGUID, p.AUTHCOMPANYGUID, p.PRODUCTGUID " +
            "from ASAUTHPRODUCT p " +
            "join ASAUTHCOMPANY c on p.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "where c.SECURITYGROUPGUID = ?",
            securityGroupGuid
        );
        class ProductProjection implements AsAuthProductRepository.Projection {
            private final Row r;
            ProductProjection(Row r) { this.r = r; }
            public String getAUTHPRODUCTGUID() { return r.get("AUTHPRODUCTGUID"); }
            public String getAUTHCOMPANYGUID() { return r.get("AUTHCOMPANYGUID"); }
            public String getPRODUCTGUID() { return r.get("PRODUCTGUID"); }
        }
        List<AsAuthProductRepository.Projection> products = productsRows.stream()
                .map(m -> new ProductProjection(new Row(m))).collect(Collectors.toList());

        List<Map<String, Object>> productPagesRows = jdbc.queryForList(
            "select /*+ LEADING(c ap p) USE_NL(ap p) */ p.AUTHPRODUCTPAGEGUID, p.AUTHPRODUCTGUID, p.AUTHPAGEGUID " +
            "from ASAUTHPRODUCTPAGE p " +
            "join ASAUTHPRODUCT ap on p.AUTHPRODUCTGUID = ap.AUTHPRODUCTGUID " +
            "join ASAUTHCOMPANY c on ap.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "where c.SECURITYGROUPGUID = ?",
            securityGroupGuid
        );
        class ProductPageProjection implements AsAuthProductPageRepository.Projection {
            private final Row r;
            ProductPageProjection(Row r) { this.r = r; }
            public String getAUTHPRODUCTPAGEGUID() { return r.get("AUTHPRODUCTPAGEGUID"); }
            public String getAUTHPRODUCTGUID() { return r.get("AUTHPRODUCTGUID"); }
            public String getAUTHPAGEGUID() { return r.get("AUTHPAGEGUID"); }
        }
        List<AsAuthProductPageRepository.Projection> productPages = productPagesRows.stream()
                .map(m -> new ProductPageProjection(new Row(m))).collect(Collectors.toList());

        List<Map<String, Object>> productPageButtonsRows = jdbc.queryForList(
            "select /*+ LEADING(c ap p b) USE_NL(ap p b) */ b.AUTHPRODUCTPAGEGUID, b.AUTHBUTTONGUID " +
            "from ASAUTHPRODUCTPAGEBUTTON b " +
            "join ASAUTHPRODUCTPAGE p on b.AUTHPRODUCTPAGEGUID = p.AUTHPRODUCTPAGEGUID " +
            "join ASAUTHPRODUCT ap on p.AUTHPRODUCTGUID = ap.AUTHPRODUCTGUID " +
            "join ASAUTHCOMPANY c on ap.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "where c.SECURITYGROUPGUID = ?",
            securityGroupGuid
        );
        class ProductPageButtonProjection implements AsAuthProductPageButtonRepository.Projection {
            private final Row r;
            ProductPageButtonProjection(Row r) { this.r = r; }
            public String getAUTHPRODUCTPAGEGUID() { return r.get("AUTHPRODUCTPAGEGUID"); }
            public String getAUTHBUTTONGUID() { return r.get("AUTHBUTTONGUID"); }
        }
        List<AsAuthProductPageButtonRepository.Projection> productPageButtons = productPageButtonsRows.stream()
                .map(m -> new ProductPageButtonProjection(new Row(m))).collect(Collectors.toList());

        List<Map<String, Object>> productTransactionsRows = jdbc.queryForList(
            "select /*+ LEADING(c ap t) USE_NL(ap t) */ t.AUTHPRODUCTTRANSACTIONGUID, t.AUTHPRODUCTGUID, t.TRANSACTIONGUID " +
            "from ASAUTHPRODUCTTRANSACTION t " +
            "join ASAUTHPRODUCT ap on t.AUTHPRODUCTGUID = ap.AUTHPRODUCTGUID " +
            "join ASAUTHCOMPANY c on ap.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "where c.SECURITYGROUPGUID = ?",
            securityGroupGuid
        );
        class ProductTransactionProjection implements AsAuthProductTransactionRepository.Projection {
            private final Row r;
            ProductTransactionProjection(Row r) { this.r = r; }
            public String getAUTHPRODUCTTRANSACTIONGUID() { return r.get("AUTHPRODUCTTRANSACTIONGUID"); }
            public String getAUTHPRODUCTGUID() { return r.get("AUTHPRODUCTGUID"); }
            public String getTRANSACTIONGUID() { return r.get("TRANSACTIONGUID"); }
        }
        List<AsAuthProductTransactionRepository.Projection> productTransactions = productTransactionsRows.stream()
                .map(m -> new ProductTransactionProjection(new Row(m))).collect(Collectors.toList());

        List<Map<String, Object>> productTransactionButtonsRows = jdbc.queryForList(
            "select /*+ LEADING(c ap t b) USE_NL(ap t b) */ b.AUTHPRODUCTTRANSACTIONGUID, b.AUTHBUTTONGUID " +
            "from ASAUTHPRODUCTTRANSACTIONBUTTON b " +
            "join ASAUTHPRODUCTTRANSACTION t on b.AUTHPRODUCTTRANSACTIONGUID = t.AUTHPRODUCTTRANSACTIONGUID " +
            "join ASAUTHPRODUCT ap on t.AUTHPRODUCTGUID = ap.AUTHPRODUCTGUID " +
            "join ASAUTHCOMPANY c on ap.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "where c.SECURITYGROUPGUID = ?",
            securityGroupGuid
        );
        class ProductTransactionButtonProjection implements AsAuthProductTransactionButtonRepository.Projection {
            private final Row r;
            ProductTransactionButtonProjection(Row r) { this.r = r; }
            public String getAUTHPRODUCTTRANSACTIONGUID() { return r.get("AUTHPRODUCTTRANSACTIONGUID"); }
            public String getAUTHBUTTONGUID() { return r.get("AUTHBUTTONGUID"); }
        }
        List<AsAuthProductTransactionButtonRepository.Projection> productTransactionButtons = productTransactionButtonsRows.stream()
                .map(m -> new ProductTransactionButtonProjection(new Row(m))).collect(Collectors.toList());

        // ── Pre-build lookup maps for parent/child mapping ────────────────
        Map<String, String> authCompanyToCompanyGuidMap = companies.stream()
                .filter(ac -> ac.getAUTHCOMPANYGUID() != null && ac.getCOMPANYGUID() != null)
                .collect(Collectors.toMap(
                        ac -> ac.getAUTHCOMPANYGUID().trim(),
                        ac -> ac.getCOMPANYGUID().trim()
                ));

        Map<String, String> companyPageToCompanyGuidMap = companyPages.stream()
                .filter(p -> p.getAUTHCOMPANYPAGEGUID() != null && p.getAUTHCOMPANYGUID() != null)
                .collect(Collectors.toMap(
                        AsAuthCompanyPageRepository.Projection::getAUTHCOMPANYPAGEGUID,
                        p -> authCompanyToCompanyGuidMap.getOrDefault(p.getAUTHCOMPANYGUID(), "")
                ));

        Map<String, String> authPlanToCompanyGuidMap = plans.stream()
                .filter(p -> p.getAUTHPLANGUID() != null && p.getAUTHCOMPANYGUID() != null)
                .collect(Collectors.toMap(
                        AsAuthPlanRepository.Projection::getAUTHPLANGUID,
                        p -> authCompanyToCompanyGuidMap.getOrDefault(p.getAUTHCOMPANYGUID(), "")
                ));

        Map<String, String> authPlanToPlanGuidMap = plans.stream()
                .filter(p -> p.getAUTHPLANGUID() != null && p.getPLANGUID() != null)
                .collect(Collectors.toMap(
                        AsAuthPlanRepository.Projection::getAUTHPLANGUID,
                        AsAuthPlanRepository.Projection::getPLANGUID
                ));

        Map<String, String> planPageToCompanyGuidMap = planPages.stream()
                .filter(p -> p.getAUTHPLANPAGEGUID() != null && p.getAUTHPLANGUID() != null)
                .collect(Collectors.toMap(
                        AsAuthPlanPageRepository.Projection::getAUTHPLANPAGEGUID,
                        p -> authPlanToCompanyGuidMap.getOrDefault(p.getAUTHPLANGUID(), "")
                ));

        Map<String, String> planPageToPlanGuidMap = planPages.stream()
                .filter(p -> p.getAUTHPLANPAGEGUID() != null && p.getAUTHPLANGUID() != null)
                .collect(Collectors.toMap(
                        AsAuthPlanPageRepository.Projection::getAUTHPLANPAGEGUID,
                        p -> authPlanToPlanGuidMap.getOrDefault(p.getAUTHPLANGUID(), "")
                ));

        Map<String, String> authTransactionToCompanyGuidMap = transactions.stream()
                .filter(t -> t.getAUTHTRANSACTIONGUID() != null && t.getAUTHPLANGUID() != null)
                .collect(Collectors.toMap(
                        AsAuthTransactionRepository.Projection::getAUTHTRANSACTIONGUID,
                        t -> authPlanToCompanyGuidMap.getOrDefault(t.getAUTHPLANGUID(), "")
                ));

        Map<String, String> authTransactionToPlanGuidMap = transactions.stream()
                .filter(t -> t.getAUTHTRANSACTIONGUID() != null && t.getAUTHPLANGUID() != null)
                .collect(Collectors.toMap(
                        AsAuthTransactionRepository.Projection::getAUTHTRANSACTIONGUID,
                        t -> authPlanToPlanGuidMap.getOrDefault(t.getAUTHPLANGUID(), "")
                ));

        Map<String, String> authProductToCompanyGuidMap = products.stream()
                .filter(p -> p.getAUTHPRODUCTGUID() != null && p.getAUTHCOMPANYGUID() != null)
                .collect(Collectors.toMap(
                        AsAuthProductRepository.Projection::getAUTHPRODUCTGUID,
                        p -> authCompanyToCompanyGuidMap.getOrDefault(p.getAUTHCOMPANYGUID(), "")
                ));

        Map<String, String> authProductToProductGuidMap = products.stream()
                .filter(p -> p.getAUTHPRODUCTGUID() != null && p.getPRODUCTGUID() != null)
                .collect(Collectors.toMap(
                        AsAuthProductRepository.Projection::getAUTHPRODUCTGUID,
                        AsAuthProductRepository.Projection::getPRODUCTGUID
                ));

        Map<String, String> productPageToCompanyGuidMap = productPages.stream()
                .filter(p -> p.getAUTHPRODUCTPAGEGUID() != null && p.getAUTHPRODUCTGUID() != null)
                .collect(Collectors.toMap(
                        AsAuthProductPageRepository.Projection::getAUTHPRODUCTPAGEGUID,
                        p -> authProductToCompanyGuidMap.getOrDefault(p.getAUTHPRODUCTGUID(), "")
                ));

        Map<String, String> productPageToProductGuidMap = productPages.stream()
                .filter(p -> p.getAUTHPRODUCTPAGEGUID() != null && p.getAUTHPRODUCTGUID() != null)
                .collect(Collectors.toMap(
                        AsAuthProductPageRepository.Projection::getAUTHPRODUCTPAGEGUID,
                        p -> authProductToProductGuidMap.getOrDefault(p.getAUTHPRODUCTGUID(), "")
                ));

        Map<String, String> productTransactionToCompanyGuidMap = productTransactions.stream()
                .filter(t -> t.getAUTHPRODUCTTRANSACTIONGUID() != null && t.getAUTHPRODUCTGUID() != null)
                .collect(Collectors.toMap(
                        AsAuthProductTransactionRepository.Projection::getAUTHPRODUCTTRANSACTIONGUID,
                        t -> authProductToCompanyGuidMap.getOrDefault(t.getAUTHPRODUCTGUID(), "")
                ));

        Map<String, String> productTransactionToProductGuidMap = productTransactions.stream()
                .filter(t -> t.getAUTHPRODUCTTRANSACTIONGUID() != null && t.getAUTHPRODUCTGUID() != null)
                .collect(Collectors.toMap(
                        AsAuthProductTransactionRepository.Projection::getAUTHPRODUCTTRANSACTIONGUID,
                        t -> authProductToProductGuidMap.getOrDefault(t.getAUTHPRODUCTGUID(), "")
                ));

        // ── Pre-group flat lists into maps to achieve O(1) lookups inside loops ──
        Map<String, List<AsAuthCompanyPageRepository.Projection>> companyPagesMap = companyPages.stream()
                .filter(p -> authCompanyToCompanyGuidMap.get(p.getAUTHCOMPANYGUID()) != null)
                .collect(Collectors.groupingBy(p -> authCompanyToCompanyGuidMap.get(p.getAUTHCOMPANYGUID())));

        Map<String, List<AsAuthCompanyPageButtonRepository.Projection>> companyPageButtonsMap = companyPageButtons.stream()
                .filter(b -> companyPageToCompanyGuidMap.get(b.getAUTHCOMPANYPAGEGUID()) != null)
                .collect(Collectors.groupingBy(b -> companyPageToCompanyGuidMap.get(b.getAUTHCOMPANYPAGEGUID())));

        Map<String, List<AsAuthCompanyInquiryRepository.Projection>> companyInquiriesMap = companyInquiries.stream()
                .filter(i -> authCompanyToCompanyGuidMap.get(i.getAUTHCOMPANYGUID()) != null)
                .collect(Collectors.groupingBy(i -> authCompanyToCompanyGuidMap.get(i.getAUTHCOMPANYGUID())));

        Map<String, List<AsAuthCompanyWebServiceRepository.Projection>> companyWebServicesMap = companyWebServices.stream()
                .filter(w -> authCompanyToCompanyGuidMap.get(w.getAUTHCOMPANYGUID()) != null)
                .collect(Collectors.groupingBy(w -> authCompanyToCompanyGuidMap.get(w.getAUTHCOMPANYGUID())));

        Map<String, List<AsAuthPlanRepository.Projection>> plansMap = plans.stream()
                .filter(p -> authCompanyToCompanyGuidMap.get(p.getAUTHCOMPANYGUID()) != null)
                .collect(Collectors.groupingBy(p -> authCompanyToCompanyGuidMap.get(p.getAUTHCOMPANYGUID())));

        Map<String, List<AsAuthPlanPageRepository.Projection>> planPagesMap = planPages.stream()
                .filter(pp -> authPlanToCompanyGuidMap.get(pp.getAUTHPLANGUID()) != null && authPlanToPlanGuidMap.get(pp.getAUTHPLANGUID()) != null)
                .collect(Collectors.groupingBy(pp -> authPlanToCompanyGuidMap.get(pp.getAUTHPLANGUID()) + "|" + authPlanToPlanGuidMap.get(pp.getAUTHPLANGUID())));

        Map<String, List<AsAuthPlanPageButtonRepository.Projection>> planPageButtonsMap = planPageButtons.stream()
                .filter(pb -> planPageToCompanyGuidMap.get(pb.getAUTHPLANPAGEGUID()) != null && planPageToPlanGuidMap.get(pb.getAUTHPLANPAGEGUID()) != null)
                .collect(Collectors.groupingBy(pb -> planPageToCompanyGuidMap.get(pb.getAUTHPLANPAGEGUID()) + "|" + planPageToPlanGuidMap.get(pb.getAUTHPLANPAGEGUID())));

        Map<String, List<AsAuthTransactionRepository.Projection>> transactionsMap = transactions.stream()
                .filter(t -> authPlanToCompanyGuidMap.get(t.getAUTHPLANGUID()) != null && authPlanToPlanGuidMap.get(t.getAUTHPLANGUID()) != null)
                .collect(Collectors.groupingBy(t -> authPlanToCompanyGuidMap.get(t.getAUTHPLANGUID()) + "|" + authPlanToPlanGuidMap.get(t.getAUTHPLANGUID())));

        Map<String, List<AsAuthTransactionButtonRepository.Projection>> transactionButtonsMap = transactionButtons.stream()
                .filter(b -> b.getAUTHTRANSACTIONGUID() != null)
                .collect(Collectors.groupingBy(AsAuthTransactionButtonRepository.Projection::getAUTHTRANSACTIONGUID));

        Map<String, List<AsAuthPlanInquiryRepository.Projection>> planInquiriesMap = planInquiries.stream()
                .filter(pi -> authPlanToCompanyGuidMap.get(pi.getAUTHPLANGUID()) != null && authPlanToPlanGuidMap.get(pi.getAUTHPLANGUID()) != null)
                .collect(Collectors.groupingBy(pi -> authPlanToCompanyGuidMap.get(pi.getAUTHPLANGUID()) + "|" + authPlanToPlanGuidMap.get(pi.getAUTHPLANGUID())));

        Map<String, List<AsAuthProductRepository.Projection>> productsMap = products.stream()
                .filter(pr -> authCompanyToCompanyGuidMap.get(pr.getAUTHCOMPANYGUID()) != null)
                .collect(Collectors.groupingBy(pr -> authCompanyToCompanyGuidMap.get(pr.getAUTHCOMPANYGUID())));

        Map<String, List<AsAuthProductPageRepository.Projection>> productPagesMap = productPages.stream()
                .filter(pp -> authProductToCompanyGuidMap.get(pp.getAUTHPRODUCTGUID()) != null && authProductToProductGuidMap.get(pp.getAUTHPRODUCTGUID()) != null)
                .collect(Collectors.groupingBy(pp -> authProductToCompanyGuidMap.get(pp.getAUTHPRODUCTGUID()) + "|" + authProductToProductGuidMap.get(pp.getAUTHPRODUCTGUID())));

        Map<String, List<AsAuthProductPageButtonRepository.Projection>> productPageButtonsMap = productPageButtons.stream()
                .filter(pb -> productPageToCompanyGuidMap.get(pb.getAUTHPRODUCTPAGEGUID()) != null && productPageToProductGuidMap.get(pb.getAUTHPRODUCTPAGEGUID()) != null)
                .collect(Collectors.groupingBy(pb -> productPageToCompanyGuidMap.get(pb.getAUTHPRODUCTPAGEGUID()) + "|" + productPageToProductGuidMap.get(pb.getAUTHPRODUCTPAGEGUID())));

        Map<String, List<AsAuthProductTransactionRepository.Projection>> productTransactionsMap = productTransactions.stream()
                .filter(t -> authProductToCompanyGuidMap.get(t.getAUTHPRODUCTGUID()) != null && authProductToProductGuidMap.get(t.getAUTHPRODUCTGUID()) != null)
                .collect(Collectors.groupingBy(t -> authProductToCompanyGuidMap.get(t.getAUTHPRODUCTGUID()) + "|" + authProductToProductGuidMap.get(t.getAUTHPRODUCTGUID())));

        Map<String, List<AsAuthProductTransactionButtonRepository.Projection>> productTransactionButtonsMap = productTransactionButtons.stream()
                .filter(b -> b.getAUTHPRODUCTTRANSACTIONGUID() != null)
                .collect(Collectors.groupingBy(AsAuthProductTransactionButtonRepository.Projection::getAUTHPRODUCTTRANSACTIONGUID));

        // ── Assemble nested structure per company ───────────────────────
        List<CompanyAuthDto> companyDtos = new ArrayList<>();
        for (AsAuthCompany ac : companies) {
            String cGuid = ac.getCOMPANYGUID();
            CompanyAuthDto cDto = new CompanyAuthDto(cGuid);

            // Company Pages → Buttons
            Map<String, PageDto> pageMap = new LinkedHashMap<>();
            List<AsAuthCompanyPageRepository.Projection> cPages = companyPagesMap.getOrDefault(cGuid, Collections.emptyList());
            for (AsAuthCompanyPageRepository.Projection p : cPages) {
                pageMap.computeIfAbsent(p.getAUTHCOMPANYPAGEGUID(), k -> new PageDto(p.getAUTHPAGEGUID()));
            }
            List<AsAuthCompanyPageButtonRepository.Projection> cPageButtons = companyPageButtonsMap.getOrDefault(cGuid, Collections.emptyList());
            for (AsAuthCompanyPageButtonRepository.Projection b : cPageButtons) {
                PageDto page = pageMap.get(b.getAUTHCOMPANYPAGEGUID());
                if (page != null) {
                    page.getButtons().add(new ButtonDto(b.getAUTHBUTTONGUID()));
                }
            }
            cDto.setCompanyPages(new ArrayList<>(pageMap.values()));

            // Company Inquiries
            List<AsAuthCompanyInquiryRepository.Projection> cInquiries = companyInquiriesMap.getOrDefault(cGuid, Collections.emptyList());
            cDto.setCompanyInquiries(
                cInquiries.stream()
                    .map(i -> new InquiryDto(i.getINQUIRYSCREENGUID()))
                    .collect(Collectors.toList())
            );

            // Company Web Services
            List<AsAuthCompanyWebServiceRepository.Projection> cWebServices = companyWebServicesMap.getOrDefault(cGuid, Collections.emptyList());
            cDto.setCompanyWebServices(
                cWebServices.stream()
                    .map(w -> new WebServiceDto(w.getAUTHWEBSERVICEGUID()))
                    .collect(Collectors.toList())
            );

            // ── Plans ───────────────────────────────────────────────────
            List<PlanAuthDto> planDtos = new ArrayList<>();
            List<AsAuthPlanRepository.Projection> cPlans = plansMap.getOrDefault(cGuid, Collections.emptyList());
            for (AsAuthPlanRepository.Projection plan : cPlans) {
                String pGuid = plan.getPLANGUID();
                PlanAuthDto planDto = new PlanAuthDto(pGuid);
                String planKey = cGuid + "|" + pGuid;

                // Plan Pages → Buttons
                Map<String, PageDto> planPageMap = new LinkedHashMap<>();
                List<AsAuthPlanPageRepository.Projection> pPages = planPagesMap.getOrDefault(planKey, Collections.emptyList());
                for (AsAuthPlanPageRepository.Projection pp : pPages) {
                    planPageMap.computeIfAbsent(pp.getAUTHPLANPAGEGUID(), k -> new PageDto(pp.getAUTHPAGEGUID()));
                }
                List<AsAuthPlanPageButtonRepository.Projection> pPageButtons = planPageButtonsMap.getOrDefault(planKey, Collections.emptyList());
                for (AsAuthPlanPageButtonRepository.Projection pb : pPageButtons) {
                    PageDto page = planPageMap.get(pb.getAUTHPLANPAGEGUID());
                    if (page != null) {
                        page.getButtons().add(new ButtonDto(pb.getAUTHBUTTONGUID()));
                    }
                }
                planDto.setPlanPages(new ArrayList<>(planPageMap.values()));

                // Plan Transactions
                List<AsAuthTransactionRepository.Projection> pTransactions = transactionsMap.getOrDefault(planKey, Collections.emptyList());
                List<TransactionDto> pTxnDtos = new ArrayList<>();
                for (AsAuthTransactionRepository.Projection t : pTransactions) {
                    TransactionDto tDto = new TransactionDto(t.getTRANSACTIONGUID());
                    List<AsAuthTransactionButtonRepository.Projection> buttons = transactionButtonsMap.getOrDefault(t.getAUTHTRANSACTIONGUID(), Collections.emptyList());
                    tDto.setButtons(buttons.stream()
                        .map(b -> new ButtonDto(b.getAUTHBUTTONGUID()))
                        .collect(Collectors.toList())
                    );
                    pTxnDtos.add(tDto);
                }
                planDto.setPlanTransactions(pTxnDtos);

                // Plan Inquiries
                List<AsAuthPlanInquiryRepository.Projection> pInquiries = planInquiriesMap.getOrDefault(planKey, Collections.emptyList());
                planDto.setPlanInquiries(
                    pInquiries.stream()
                        .map(pi -> new InquiryDto(pi.getINQUIRYSCREENGUID()))
                        .collect(Collectors.toList())
                );

                planDtos.add(planDto);
            }
            cDto.setPlans(planDtos);

            // ── Products ────────────────────────────────────────────────
            List<ProductAuthDto> productDtos = new ArrayList<>();
            List<AsAuthProductRepository.Projection> cProducts = productsMap.getOrDefault(cGuid, Collections.emptyList());
            for (AsAuthProductRepository.Projection prod : cProducts) {
                String prGuid = prod.getPRODUCTGUID();
                ProductAuthDto prodDto = new ProductAuthDto(prGuid);
                String productKey = cGuid + "|" + prGuid;

                // Product Pages → Buttons
                Map<String, PageDto> prodPageMap = new LinkedHashMap<>();
                List<AsAuthProductPageRepository.Projection> prPages = productPagesMap.getOrDefault(productKey, Collections.emptyList());
                for (AsAuthProductPageRepository.Projection pp : prPages) {
                    prodPageMap.computeIfAbsent(pp.getAUTHPRODUCTPAGEGUID(), k -> new PageDto(pp.getAUTHPAGEGUID()));
                }
                List<AsAuthProductPageButtonRepository.Projection> prPageButtons = productPageButtonsMap.getOrDefault(productKey, Collections.emptyList());
                for (AsAuthProductPageButtonRepository.Projection pb : prPageButtons) {
                    PageDto page = prodPageMap.get(pb.getAUTHPRODUCTPAGEGUID());
                    if (page != null) {
                        page.getButtons().add(new ButtonDto(pb.getAUTHBUTTONGUID()));
                    }
                }
                prodDto.setProductPages(new ArrayList<>(prodPageMap.values()));

                // Product Transactions
                List<AsAuthProductTransactionRepository.Projection> prTransactions = productTransactionsMap.getOrDefault(productKey, Collections.emptyList());
                List<TransactionDto> prTxnDtos = new ArrayList<>();
                for (AsAuthProductTransactionRepository.Projection t : prTransactions) {
                    TransactionDto tDto = new TransactionDto(t.getTRANSACTIONGUID());
                    List<AsAuthProductTransactionButtonRepository.Projection> buttons = productTransactionButtonsMap.getOrDefault(t.getAUTHPRODUCTTRANSACTIONGUID(), Collections.emptyList());
                    tDto.setButtons(buttons.stream()
                        .map(b -> new ButtonDto(b.getAUTHBUTTONGUID()))
                        .collect(Collectors.toList())
                    );
                    prTxnDtos.add(tDto);
                }
                prodDto.setProductTransactions(prTxnDtos);

                productDtos.add(prodDto);
            }
            cDto.setProducts(productDtos);

            companyDtos.add(cDto);
        }

        dto.setCompanies(companyDtos);
        log.info("Successfully fetched security configuration for group: {} (GUID: {}). Total companies: {}",
                dto.getGroupName(), securityGroupGuid, companyDtos.size());
        return dto;
    }

    // ===================================================================
    // 3.  POST /api/security-group/generate-scripts  —  delta generator
    // ===================================================================
    @Transactional(value = "secondaryDevTransactionManager", readOnly = true)
    public GenerateScriptsResponseDto generateScripts(SecurityGroupDto incoming) {
        String guid = incoming.getSecurityGroupGuid();
        String groupName = incoming.getGroupName();
        log.info("Generating scripts for security group: {} (GUID: {})", groupName, guid);
        boolean isNewGroup = (guid == null || guid.trim().isEmpty());

        // Step A — Read existing state
        SecurityGroupDto existing;
        if (isNewGroup) {
            // For a new group we need to generate a GUID placeholder
            guid = java.util.UUID.randomUUID().toString().toUpperCase();
            incoming.setSecurityGroupGuid(guid);
            existing = new SecurityGroupDto(guid, groupName);
        } else {
            existing = getSecurityConfiguration(guid);
        }

        // Step B & C — Compare and generate SQL
        List<String> scripts = new ArrayList<>();
        Map<String, String> authGuidMap = new java.util.HashMap<>();
        if (!isNewGroup) {
            loadExistingAuthGuids(guid, authGuidMap);
        }

        // ── Security Group INSERT if new ────────────────────────────────
        if (isNewGroup) {
            scripts.add(String.format(
                "INSERT INTO ASSECURITYGROUP (SECURITYGROUPGUID, GROUPNAME) VALUES ('%s', '%s');",
                esc(guid), esc(groupName)));
        }

        // Flatten both trees into sets of composite keys, then diff
        Set<String> existingCompanyKeys = flattenCompanyKeys(existing);
        Set<String> incomingCompanyKeys = flattenCompanyKeys(incoming);

        Set<String> existingCompanyPageKeys = flattenCompanyPageKeys(existing);
        Set<String> incomingCompanyPageKeys = flattenCompanyPageKeys(incoming);

        Set<String> existingCompanyPageButtonKeys = flattenCompanyPageButtonKeys(existing);
        Set<String> incomingCompanyPageButtonKeys = flattenCompanyPageButtonKeys(incoming);

        Set<String> existingCompanyInquiryKeys = flattenCompanyInquiryKeys(existing);
        Set<String> incomingCompanyInquiryKeys = flattenCompanyInquiryKeys(incoming);

        Set<String> existingCompanyWebServiceKeys = flattenCompanyWebServiceKeys(existing);
        Set<String> incomingCompanyWebServiceKeys = flattenCompanyWebServiceKeys(incoming);

        Set<String> existingPlanKeys = flattenPlanKeys(existing);
        Set<String> incomingPlanKeys = flattenPlanKeys(incoming);

        Set<String> existingPlanPageKeys = flattenPlanPageKeys(existing);
        Set<String> incomingPlanPageKeys = flattenPlanPageKeys(incoming);

        Set<String> existingPlanPageButtonKeys = flattenPlanPageButtonKeys(existing);
        Set<String> incomingPlanPageButtonKeys = flattenPlanPageButtonKeys(incoming);

        Set<String> existingTransactionKeys = flattenTransactionKeys(existing);
        Set<String> incomingTransactionKeys = flattenTransactionKeys(incoming);

        Set<String> existingPlanInquiryKeys = flattenPlanInquiryKeys(existing);
        Set<String> incomingPlanInquiryKeys = flattenPlanInquiryKeys(incoming);

        Set<String> existingProductKeys = flattenProductKeys(existing);
        Set<String> incomingProductKeys = flattenProductKeys(incoming);

        Set<String> existingProductPageKeys = flattenProductPageKeys(existing);
        Set<String> incomingProductPageKeys = flattenProductPageKeys(incoming);

        Set<String> existingProductPageButtonKeys = flattenProductPageButtonKeys(existing);
        Set<String> incomingProductPageButtonKeys = flattenProductPageButtonKeys(incoming);

        Set<String> existingProductTransactionKeys = flattenProductTransactionKeys(existing);
        Set<String> incomingProductTransactionKeys = flattenProductTransactionKeys(incoming);

        Set<String> existingProductTransactionButtonKeys = flattenProductTransactionButtonKeys(existing);
        Set<String> incomingProductTransactionButtonKeys = flattenProductTransactionButtonKeys(incoming);

        Set<String> existingTransactionButtonKeys = flattenTransactionButtonKeys(existing);
        Set<String> incomingTransactionButtonKeys = flattenTransactionButtonKeys(incoming);

        // ── DELETES (bottom-up to respect FK constraints) ───────────────
        scripts.add("-- ====== DELETE SCRIPTS (removing revoked permissions) ======");

        // Product transaction buttons
        diff(existingProductTransactionButtonKeys, incomingProductTransactionButtonKeys).forEach(k -> {
            String[] p = k.split("\\|");
            scripts.add(String.format(
                "DELETE FROM ASAUTHPRODUCTTRANSACTIONBUTTON WHERE AUTHBUTTONGUID = '%s' AND AUTHPRODUCTTRANSACTIONGUID IN (SELECT AUTHPRODUCTTRANSACTIONGUID FROM ASAUTHPRODUCTTRANSACTION WHERE TRANSACTIONGUID = '%s' AND AUTHPRODUCTGUID IN (SELECT AUTHPRODUCTGUID FROM ASAUTHPRODUCT WHERE PRODUCTGUID = '%s' AND AUTHCOMPANYGUID IN (SELECT AUTHCOMPANYGUID FROM ASAUTHCOMPANY WHERE COMPANYGUID = '%s' AND SECURITYGROUPGUID = '%s')));",
                esc(p[4]), esc(p[3]), esc(p[2]), esc(p[1]), esc(p[0])));
        });
        // Plan transaction buttons
        diff(existingTransactionButtonKeys, incomingTransactionButtonKeys).forEach(k -> {
            String[] p = k.split("\\|");
            scripts.add(String.format(
                "DELETE FROM ASAUTHTRANSACTIONBUTTON WHERE AUTHBUTTONGUID = '%s' AND AUTHTRANSACTIONGUID IN (SELECT AUTHTRANSACTIONGUID FROM ASAUTHTRANSACTION WHERE TRANSACTIONGUID = '%s' AND AUTHPLANGUID IN (SELECT AUTHPLANGUID FROM ASAUTHPLAN WHERE PLANGUID = '%s' AND AUTHCOMPANYGUID IN (SELECT AUTHCOMPANYGUID FROM ASAUTHCOMPANY WHERE COMPANYGUID = '%s' AND SECURITYGROUPGUID = '%s')));",
                esc(p[4]), esc(p[3]), esc(p[2]), esc(p[1]), esc(p[0])));
        });
        
        // Product page buttons
        diff(existingProductPageButtonKeys, incomingProductPageButtonKeys).forEach(k -> {
            String[] p = k.split("\\|");
            scripts.add(String.format(
                "DELETE FROM ASAUTHPRODUCTPAGEBUTTON WHERE AUTHBUTTONGUID = '%s' AND AUTHPRODUCTPAGEGUID IN (SELECT AUTHPRODUCTPAGEGUID FROM ASAUTHPRODUCTPAGE WHERE AUTHPAGEGUID = '%s' AND AUTHPRODUCTGUID IN (SELECT AUTHPRODUCTGUID FROM ASAUTHPRODUCT WHERE PRODUCTGUID = '%s' AND AUTHCOMPANYGUID IN (SELECT AUTHCOMPANYGUID FROM ASAUTHCOMPANY WHERE COMPANYGUID = '%s' AND SECURITYGROUPGUID = '%s')));",
                esc(p[4]), esc(p[3]), esc(p[2]), esc(p[1]), esc(p[0])));
        });
        // Product pages
        diff(existingProductPageKeys, incomingProductPageKeys).forEach(k -> {
            String[] p = k.split("\\|");
            scripts.add(String.format(
                "DELETE FROM ASAUTHPRODUCTPAGE WHERE AUTHPAGEGUID = '%s' AND AUTHPRODUCTGUID IN (SELECT AUTHPRODUCTGUID FROM ASAUTHPRODUCT WHERE PRODUCTGUID = '%s' AND AUTHCOMPANYGUID IN (SELECT AUTHCOMPANYGUID FROM ASAUTHCOMPANY WHERE COMPANYGUID = '%s' AND SECURITYGROUPGUID = '%s'));",
                esc(p[3]), esc(p[2]), esc(p[1]), esc(p[0])));
        });
        // Product transactions
        diff(existingProductTransactionKeys, incomingProductTransactionKeys).forEach(k -> {
            String[] p = k.split("\\|");
            scripts.add(String.format(
                "DELETE FROM ASAUTHPRODUCTTRANSACTION WHERE TRANSACTIONGUID = '%s' AND AUTHPRODUCTGUID IN (SELECT AUTHPRODUCTGUID FROM ASAUTHPRODUCT WHERE PRODUCTGUID = '%s' AND AUTHCOMPANYGUID IN (SELECT AUTHCOMPANYGUID FROM ASAUTHCOMPANY WHERE COMPANYGUID = '%s' AND SECURITYGROUPGUID = '%s'));",
                esc(p[3]), esc(p[2]), esc(p[1]), esc(p[0])));
        });
        // Products
        diff(existingProductKeys, incomingProductKeys).forEach(k -> {
            String[] p = k.split("\\|");
            scripts.add(String.format(
                "DELETE FROM ASAUTHPRODUCT WHERE PRODUCTGUID = '%s' AND AUTHCOMPANYGUID IN (SELECT AUTHCOMPANYGUID FROM ASAUTHCOMPANY WHERE COMPANYGUID = '%s' AND SECURITYGROUPGUID = '%s');",
                esc(p[2]), esc(p[1]), esc(p[0])));
        });

        // Plan page buttons
        diff(existingPlanPageButtonKeys, incomingPlanPageButtonKeys).forEach(k -> {
            String[] p = k.split("\\|");
            scripts.add(String.format(
                "DELETE FROM ASAUTHPLANPAGEBUTTON WHERE AUTHBUTTONGUID = '%s' AND AUTHPLANPAGEGUID IN (SELECT AUTHPLANPAGEGUID FROM ASAUTHPLANPAGE WHERE AUTHPAGEGUID = '%s' AND AUTHPLANGUID IN (SELECT AUTHPLANGUID FROM ASAUTHPLAN WHERE PLANGUID = '%s' AND AUTHCOMPANYGUID IN (SELECT AUTHCOMPANYGUID FROM ASAUTHCOMPANY WHERE COMPANYGUID = '%s' AND SECURITYGROUPGUID = '%s')));",
                esc(p[4]), esc(p[3]), esc(p[2]), esc(p[1]), esc(p[0])));
        });
        // Plan pages
        diff(existingPlanPageKeys, incomingPlanPageKeys).forEach(k -> {
            String[] p = k.split("\\|");
            scripts.add(String.format(
                "DELETE FROM ASAUTHPLANPAGE WHERE AUTHPAGEGUID = '%s' AND AUTHPLANGUID IN (SELECT AUTHPLANGUID FROM ASAUTHPLAN WHERE PLANGUID = '%s' AND AUTHCOMPANYGUID IN (SELECT AUTHCOMPANYGUID FROM ASAUTHCOMPANY WHERE COMPANYGUID = '%s' AND SECURITYGROUPGUID = '%s'));",
                esc(p[3]), esc(p[2]), esc(p[1]), esc(p[0])));
        });
        // Plan transactions
        diff(existingTransactionKeys, incomingTransactionKeys).forEach(k -> {
            String[] p = k.split("\\|");
            scripts.add(String.format(
                "DELETE FROM ASAUTHTRANSACTION WHERE TRANSACTIONGUID = '%s' AND AUTHPLANGUID IN (SELECT AUTHPLANGUID FROM ASAUTHPLAN WHERE PLANGUID = '%s' AND AUTHCOMPANYGUID IN (SELECT AUTHCOMPANYGUID FROM ASAUTHCOMPANY WHERE COMPANYGUID = '%s' AND SECURITYGROUPGUID = '%s'));",
                esc(p[3]), esc(p[2]), esc(p[1]), esc(p[0])));
        });
        // Plan inquiries
        diff(existingPlanInquiryKeys, incomingPlanInquiryKeys).forEach(k -> {
            String[] p = k.split("\\|");
            scripts.add(String.format(
                "DELETE FROM ASAUTHPLANINQUIRY WHERE INQUIRYSCREENNAMEGUID = '%s' AND AUTHPLANGUID IN (SELECT AUTHPLANGUID FROM ASAUTHPLAN WHERE PLANGUID = '%s' AND AUTHCOMPANYGUID IN (SELECT AUTHCOMPANYGUID FROM ASAUTHCOMPANY WHERE COMPANYGUID = '%s' AND SECURITYGROUPGUID = '%s'));",
                esc(p[3]), esc(p[2]), esc(p[1]), esc(p[0])));
        });
        // Plans
        diff(existingPlanKeys, incomingPlanKeys).forEach(k -> {
            String[] p = k.split("\\|");
            scripts.add(String.format(
                "DELETE FROM ASAUTHPLAN WHERE PLANGUID = '%s' AND AUTHCOMPANYGUID IN (SELECT AUTHCOMPANYGUID FROM ASAUTHCOMPANY WHERE COMPANYGUID = '%s' AND SECURITYGROUPGUID = '%s');",
                esc(p[2]), esc(p[1]), esc(p[0])));
        });

        // Company page buttons
        diff(existingCompanyPageButtonKeys, incomingCompanyPageButtonKeys).forEach(k -> {
            String[] p = k.split("\\|");
            scripts.add(String.format(
                "DELETE FROM ASAUTHCOMPANYPAGEBUTTON WHERE AUTHBUTTONGUID = '%s' AND AUTHCOMPANYPAGEGUID IN (SELECT AUTHCOMPANYPAGEGUID FROM ASAUTHCOMPANYPAGE WHERE AUTHPAGEGUID = '%s' AND AUTHCOMPANYGUID IN (SELECT AUTHCOMPANYGUID FROM ASAUTHCOMPANY WHERE COMPANYGUID = '%s' AND SECURITYGROUPGUID = '%s'));",
                esc(p[3]), esc(p[2]), esc(p[1]), esc(p[0])));
        });
        // Company pages
        diff(existingCompanyPageKeys, incomingCompanyPageKeys).forEach(k -> {
            String[] p = k.split("\\|");
            scripts.add(String.format(
                "DELETE FROM ASAUTHCOMPANYPAGE WHERE AUTHPAGEGUID = '%s' AND AUTHCOMPANYGUID IN (SELECT AUTHCOMPANYGUID FROM ASAUTHCOMPANY WHERE COMPANYGUID = '%s' AND SECURITYGROUPGUID = '%s');",
                esc(p[2]), esc(p[1]), esc(p[0])));
        });
        // Company inquiries
        diff(existingCompanyInquiryKeys, incomingCompanyInquiryKeys).forEach(k -> {
            String[] p = k.split("\\|");
            scripts.add(String.format(
                "DELETE FROM ASAUTHCOMPANYINQUIRY WHERE INQUIRYSCREENNAMEGUID = '%s' AND AUTHCOMPANYGUID IN (SELECT AUTHCOMPANYGUID FROM ASAUTHCOMPANY WHERE COMPANYGUID = '%s' AND SECURITYGROUPGUID = '%s');",
                esc(p[2]), esc(p[1]), esc(p[0])));
        });
        // Company web services
        diff(existingCompanyWebServiceKeys, incomingCompanyWebServiceKeys).forEach(k -> {
            String[] p = k.split("\\|");
            scripts.add(String.format(
                "DELETE FROM ASAUTHCOMPANYWEBSERVICE WHERE AUTHWEBSERVICEGUID = '%s' AND AUTHCOMPANYGUID IN (SELECT AUTHCOMPANYGUID FROM ASAUTHCOMPANY WHERE COMPANYGUID = '%s' AND SECURITYGROUPGUID = '%s');",
                esc(p[2]), esc(p[1]), esc(p[0])));
        });
        // Companies
        diff(existingCompanyKeys, incomingCompanyKeys).forEach(k -> {
            String[] p = k.split("\\|");
            scripts.add(String.format(
                "DELETE FROM ASAUTHCOMPANY WHERE COMPANYGUID = '%s' AND SECURITYGROUPGUID = '%s';",
                esc(p[1]), esc(p[0])));
        });

        // Orphaned group cleanup
        if (!isNewGroup && incomingCompanyKeys.isEmpty() && !existingCompanyKeys.isEmpty()) {
            scripts.add(String.format(
                "DELETE FROM ASSECURITYGROUP WHERE SECURITYGROUPGUID = '%s';", esc(guid)));
        }

        // ── INSERTS (top-down to satisfy FK constraints) ───────────────
        scripts.add("-- ====== INSERT SCRIPTS (granting new permissions) ======");

        // Companies
        diff(incomingCompanyKeys, existingCompanyKeys).forEach(k -> {
            String[] p = k.split("\\|");
            String newGuid = java.util.UUID.randomUUID().toString().toUpperCase();
            authGuidMap.put(k, newGuid);
            scripts.add(String.format(
                "INSERT INTO ASAUTHCOMPANY (AUTHCOMPANYGUID, COMPANYGUID, SECURITYGROUPGUID) VALUES ('%s', '%s', '%s');",
                esc(newGuid), esc(p[1]), esc(p[0])));
        });
        // Company pages
        diff(incomingCompanyPageKeys, existingCompanyPageKeys).forEach(k -> {
            String[] p = k.split("\\|");
            String newGuid = java.util.UUID.randomUUID().toString().toUpperCase();
            authGuidMap.put(k, newGuid);
            String parentKey = p[0] + "|" + p[1];
            String parentAuthGuid = authGuidMap.get(parentKey);
            scripts.add(String.format(
                "INSERT INTO ASAUTHCOMPANYPAGE (AUTHCOMPANYPAGEGUID, AUTHCOMPANYGUID, AUTHPAGEGUID) VALUES ('%s', '%s', '%s');",
                esc(newGuid), esc(parentAuthGuid), esc(p[2])));
        });
        // Company page buttons
        diff(incomingCompanyPageButtonKeys, existingCompanyPageButtonKeys).forEach(k -> {
            String[] p = k.split("\\|");
            String newGuid = java.util.UUID.randomUUID().toString().toUpperCase();
            String parentKey = p[0] + "|" + p[1] + "|" + p[2];
            String parentAuthGuid = authGuidMap.get(parentKey);
            scripts.add(String.format(
                "INSERT INTO ASAUTHCOMPANYPAGEBUTTON (AUTHCOMPANYPAGEBUTTONGUID, AUTHCOMPANYPAGEGUID, AUTHBUTTONGUID) VALUES ('%s', '%s', '%s');",
                esc(newGuid), esc(parentAuthGuid), esc(p[3])));
        });
        // Company inquiries
        diff(incomingCompanyInquiryKeys, existingCompanyInquiryKeys).forEach(k -> {
            String[] p = k.split("\\|");
            String newGuid = java.util.UUID.randomUUID().toString().toUpperCase();
            String parentKey = p[0] + "|" + p[1];
            String parentAuthGuid = authGuidMap.get(parentKey);
            scripts.add(String.format(
                "INSERT INTO ASAUTHCOMPANYINQUIRY (AUTHCOMPANYINQUIRYGUID, AUTHCOMPANYGUID, INQUIRYSCREENNAMEGUID) VALUES ('%s', '%s', '%s');",
                esc(newGuid), esc(parentAuthGuid), esc(p[2])));
        });
        // Company web services
        diff(incomingCompanyWebServiceKeys, existingCompanyWebServiceKeys).forEach(k -> {
            String[] p = k.split("\\|");
            String parentKey = p[0] + "|" + p[1];
            String parentAuthGuid = authGuidMap.get(parentKey);
            scripts.add(String.format(
                "INSERT INTO ASAUTHCOMPANYWEBSERVICE (AUTHWEBSERVICEGUID, AUTHCOMPANYGUID) VALUES ('%s', '%s');",
                esc(p[2]), esc(parentAuthGuid)));
        });
        // Plans
        diff(incomingPlanKeys, existingPlanKeys).forEach(k -> {
            String[] p = k.split("\\|");
            String newGuid = java.util.UUID.randomUUID().toString().toUpperCase();
            authGuidMap.put(k, newGuid);
            String parentKey = p[0] + "|" + p[1];
            String parentAuthGuid = authGuidMap.get(parentKey);
            scripts.add(String.format(
                "INSERT INTO ASAUTHPLAN (AUTHPLANGUID, AUTHCOMPANYGUID, PLANGUID) VALUES ('%s', '%s', '%s');",
                esc(newGuid), esc(parentAuthGuid), esc(p[2])));
        });
        // Plan pages
        diff(incomingPlanPageKeys, existingPlanPageKeys).forEach(k -> {
            String[] p = k.split("\\|");
            String newGuid = java.util.UUID.randomUUID().toString().toUpperCase();
            authGuidMap.put(k, newGuid);
            String parentKey = p[0] + "|" + p[1] + "|" + p[2];
            String parentAuthGuid = authGuidMap.get(parentKey);
            scripts.add(String.format(
                "INSERT INTO ASAUTHPLANPAGE (AUTHPLANPAGEGUID, AUTHPLANGUID, AUTHPAGEGUID) VALUES ('%s', '%s', '%s');",
                esc(newGuid), esc(parentAuthGuid), esc(p[3])));
        });
        // Plan page buttons
        diff(incomingPlanPageButtonKeys, existingPlanPageButtonKeys).forEach(k -> {
            String[] p = k.split("\\|");
            String parentKey = p[0] + "|" + p[1] + "|" + p[2] + "|" + p[3];
            String parentAuthGuid = authGuidMap.get(parentKey);
            scripts.add(String.format(
                "INSERT INTO ASAUTHPLANPAGEBUTTON (AUTHPLANPAGEGUID, AUTHBUTTONGUID) VALUES ('%s', '%s');",
                esc(parentAuthGuid), esc(p[4])));
        });
        // Plan transactions
        diff(incomingTransactionKeys, existingTransactionKeys).forEach(k -> {
            String[] p = k.split("\\|");
            String newGuid = java.util.UUID.randomUUID().toString().toUpperCase();
            authGuidMap.put(k, newGuid);
            String parentKey = p[0] + "|" + p[1] + "|" + p[2];
            String parentAuthGuid = authGuidMap.get(parentKey);
            scripts.add(String.format(
                "INSERT INTO ASAUTHTRANSACTION (AUTHTRANSACTIONGUID, AUTHPLANGUID, TRANSACTIONGUID) VALUES ('%s', '%s', '%s');",
                esc(newGuid), esc(parentAuthGuid), esc(p[3])));
        });
        // Plan inquiries
        diff(incomingPlanInquiryKeys, existingPlanInquiryKeys).forEach(k -> {
            String[] p = k.split("\\|");
            String newGuid = java.util.UUID.randomUUID().toString().toUpperCase();
            String parentKey = p[0] + "|" + p[1] + "|" + p[2];
            String parentAuthGuid = authGuidMap.get(parentKey);
            scripts.add(String.format(
                "INSERT INTO ASAUTHPLANINQUIRY (AUTHPLANINQUIRYGUID, AUTHPLANGUID, INQUIRYSCREENNAMEGUID) VALUES ('%s', '%s', '%s');",
                esc(newGuid), esc(parentAuthGuid), esc(p[3])));
        });
        // Products
        diff(incomingProductKeys, existingProductKeys).forEach(k -> {
            String[] p = k.split("\\|");
            String newGuid = java.util.UUID.randomUUID().toString().toUpperCase();
            authGuidMap.put(k, newGuid);
            String parentKey = p[0] + "|" + p[1];
            String parentAuthGuid = authGuidMap.get(parentKey);
            scripts.add(String.format(
                "INSERT INTO ASAUTHPRODUCT (AUTHPRODUCTGUID, AUTHCOMPANYGUID, PRODUCTGUID) VALUES ('%s', '%s', '%s');",
                esc(newGuid), esc(parentAuthGuid), esc(p[2])));
        });
        // Product pages
        diff(incomingProductPageKeys, existingProductPageKeys).forEach(k -> {
            String[] p = k.split("\\|");
            String newGuid = java.util.UUID.randomUUID().toString().toUpperCase();
            authGuidMap.put(k, newGuid);
            String parentKey = p[0] + "|" + p[1] + "|" + p[2];
            String parentAuthGuid = authGuidMap.get(parentKey);
            scripts.add(String.format(
                "INSERT INTO ASAUTHPRODUCTPAGE (AUTHPRODUCTPAGEGUID, AUTHPRODUCTGUID, AUTHPAGEGUID) VALUES ('%s', '%s', '%s');",
                esc(newGuid), esc(parentAuthGuid), esc(p[3])));
        });
        // Product page buttons
        diff(incomingProductPageButtonKeys, existingProductPageButtonKeys).forEach(k -> {
            String[] p = k.split("\\|");
            String parentKey = p[0] + "|" + p[1] + "|" + p[2] + "|" + p[3];
            String parentAuthGuid = authGuidMap.get(parentKey);
            scripts.add(String.format(
                "INSERT INTO ASAUTHPRODUCTPAGEBUTTON (AUTHPRODUCTPAGEGUID, AUTHBUTTONGUID) VALUES ('%s', '%s');",
                esc(parentAuthGuid), esc(p[4])));
        });
        // Product transactions
        diff(incomingProductTransactionKeys, existingProductTransactionKeys).forEach(k -> {
            String[] p = k.split("\\|");
            String newGuid = java.util.UUID.randomUUID().toString().toUpperCase();
            authGuidMap.put(k, newGuid);
            String parentKey = p[0] + "|" + p[1] + "|" + p[2];
            String parentAuthGuid = authGuidMap.get(parentKey);
            scripts.add(String.format(
                "INSERT INTO ASAUTHPRODUCTTRANSACTION (AUTHPRODUCTTRANSACTIONGUID, AUTHPRODUCTGUID, TRANSACTIONGUID) VALUES ('%s', '%s', '%s');",
                esc(newGuid), esc(parentAuthGuid), esc(p[3])));
        });
        // Product transaction buttons
        diff(incomingProductTransactionButtonKeys, existingProductTransactionButtonKeys).forEach(k -> {
            String[] p = k.split("\\|");
            String parentKey = p[0] + "|" + p[1] + "|" + p[2] + "|" + p[3];
            String parentAuthGuid = authGuidMap.get(parentKey);
            scripts.add(String.format(
                "INSERT INTO ASAUTHPRODUCTTRANSACTIONBUTTON (AUTHPRODUCTTRANSACTIONGUID, AUTHBUTTONGUID) VALUES ('%s', '%s');",
                esc(parentAuthGuid), esc(p[4])));
        });
        // Plan transaction buttons
        diff(incomingTransactionButtonKeys, existingTransactionButtonKeys).forEach(k -> {
            String[] p = k.split("\\|");
            String parentKey = p[0] + "|" + p[1] + "|" + p[2] + "|" + p[3];
            String parentAuthGuid = authGuidMap.get(parentKey);
            scripts.add(String.format(
                "INSERT INTO ASAUTHTRANSACTIONBUTTON (AUTHTRANSACTIONGUID, AUTHBUTTONGUID) VALUES ('%s', '%s');",
                esc(parentAuthGuid), esc(p[4])));
        });

        log.info("Generated {} delta SQL scripts for security group: {} (GUID: {})", scripts.size(), groupName, guid);
        return new GenerateScriptsResponseDto(guid, groupName, scripts);
    }

    @Transactional(value = "secondaryDevTransactionManager", readOnly = true)
    public GenerateScriptsResponseDto generateMigrationScripts(SecurityGroupDto incoming) {
        String guid = incoming.getSecurityGroupGuid();
        String groupName = incoming.getGroupName();
        log.info("Generating migration scripts for security group: {} (GUID: {})", groupName, guid);

        List<String> scripts = new ArrayList<>();
        List<MigrationScriptDto> migrationScripts = new ArrayList<>();
        Map<String, String> authGuidMap = new java.util.HashMap<>();
        if (guid != null && !guid.trim().isEmpty()) {
            loadExistingAuthGuids(guid, authGuidMap);
        }

        // Helper to safely get or generate a GUID for a composite key
        java.util.function.Function<String, String> getOrGen = (key) -> {
            String existingGuid = authGuidMap.get(key);
            if (existingGuid == null || existingGuid.trim().isEmpty()) {
                existingGuid = java.util.UUID.randomUUID().toString().toUpperCase();
                authGuidMap.put(key, existingGuid);
            }
            return existingGuid.trim();
        };

        // 1. Security Group
        String sgScript = String.format(
            "INSERT INTO ASSECURITYGROUP (SECURITYGROUPGUID, GROUPNAME) VALUES ('%s', '%s');",
            esc(guid), esc(groupName));
        scripts.add("-- ====== SECURITY GROUP ======");
        scripts.add(sgScript);
        migrationScripts.add(new MigrationScriptDto(null, null, null, "SECURITY_GROUP", guid, sgScript));

        if (incoming.getCompanies() != null) {
            for (CompanyAuthDto company : incoming.getCompanies()) {
                String companyGuid = company.getCompanyGuid();
                String compKey = guid + "|" + companyGuid;
                String authCompanyGuid = getOrGen.apply(compKey);

                String compScript = String.format(
                    "INSERT INTO ASAUTHCOMPANY (AUTHCOMPANYGUID, COMPANYGUID, SECURITYGROUPGUID) VALUES ('%s', '%s', '%s');",
                    esc(authCompanyGuid), esc(companyGuid), esc(guid));
                scripts.add(compScript);
                migrationScripts.add(new MigrationScriptDto(companyGuid, null, null, "COMPANY", companyGuid, compScript));

                // Company Pages
                if (company.getCompanyPages() != null) {
                    for (PageDto page : company.getCompanyPages()) {
                        String pageGuid = page.getPageGuid();
                        String pageKey = compKey + "|" + pageGuid;
                        String authPageGuid = getOrGen.apply(pageKey);

                        String pageScript = String.format(
                            "INSERT INTO ASAUTHCOMPANYPAGE (AUTHCOMPANYPAGEGUID, AUTHCOMPANYGUID, AUTHPAGEGUID) VALUES ('%s', '%s', '%s');",
                            esc(authPageGuid), esc(authCompanyGuid), esc(pageGuid));
                        scripts.add(pageScript);
                        migrationScripts.add(new MigrationScriptDto(companyGuid, null, null, "COMPANY_PAGE", pageGuid, pageScript));

                        // Company Page Buttons
                        if (page.getButtons() != null) {
                            for (ButtonDto button : page.getButtons()) {
                                String buttonGuid = button.getButtonGuid();
                                String btnKey = pageKey + "|" + buttonGuid;
                                String authBtnGuid = getOrGen.apply(btnKey);

                                String btnScript = String.format(
                                    "INSERT INTO ASAUTHCOMPANYPAGEBUTTON (AUTHCOMPANYPAGEBUTTONGUID, AUTHCOMPANYPAGEGUID, AUTHBUTTONGUID) VALUES ('%s', '%s', '%s');",
                                    esc(authBtnGuid), esc(authPageGuid), esc(buttonGuid));
                                scripts.add(btnScript);
                                migrationScripts.add(new MigrationScriptDto(companyGuid, null, null, "COMPANY_BUTTON", buttonGuid, btnScript));
                            }
                        }
                    }
                }

                // Company Inquiries
                if (company.getCompanyInquiries() != null) {
                    for (InquiryDto inquiry : company.getCompanyInquiries()) {
                        String inqGuid = inquiry.getInquiryScreenNameGuid();
                        String inqKey = compKey + "|" + inqGuid;
                        String authInqGuid = getOrGen.apply(inqKey);

                        String inqScript = String.format(
                            "INSERT INTO ASAUTHCOMPANYINQUIRY (AUTHCOMPANYINQUIRYGUID, AUTHCOMPANYGUID, INQUIRYSCREENNAMEGUID) VALUES ('%s', '%s', '%s');",
                            esc(authInqGuid), esc(authCompanyGuid), esc(inqGuid));
                        scripts.add(inqScript);
                        migrationScripts.add(new MigrationScriptDto(companyGuid, null, null, "COMPANY_INQUIRY", inqGuid, inqScript));
                    }
                }

                // Company Web Services
                if (company.getCompanyWebServices() != null) {
                    for (WebServiceDto ws : company.getCompanyWebServices()) {
                        String wsGuid = ws.getWebServiceGuid();
                        String wsScript = String.format(
                            "INSERT INTO ASAUTHCOMPANYWEBSERVICE (AUTHWEBSERVICEGUID, AUTHCOMPANYGUID) VALUES ('%s', '%s');",
                            esc(wsGuid), esc(authCompanyGuid));
                        scripts.add(wsScript);
                        migrationScripts.add(new MigrationScriptDto(companyGuid, null, null, "COMPANY_WEBSERVICE", wsGuid, wsScript));
                    }
                }

                // Plans
                if (company.getPlans() != null) {
                    for (PlanAuthDto plan : company.getPlans()) {
                        String planGuid = plan.getPlanGuid();
                        String planKey = compKey + "|" + planGuid;
                        String authPlanGuid = getOrGen.apply(planKey);

                        String planScript = String.format(
                            "INSERT INTO ASAUTHPLAN (AUTHPLANGUID, AUTHCOMPANYGUID, PLANGUID) VALUES ('%s', '%s', '%s');",
                            esc(authPlanGuid), esc(authCompanyGuid), esc(planGuid));
                        scripts.add(planScript);
                        migrationScripts.add(new MigrationScriptDto(companyGuid, null, planGuid, "PLAN", planGuid, planScript));

                        // Plan Pages
                        if (plan.getPlanPages() != null) {
                            for (PageDto page : plan.getPlanPages()) {
                                String pageGuid = page.getPageGuid();
                                String planPageKey = planKey + "|" + pageGuid;
                                String authPlanPageGuid = getOrGen.apply(planPageKey);

                                String pageScript = String.format(
                                    "INSERT INTO ASAUTHPLANPAGE (AUTHPLANPAGEGUID, AUTHPLANGUID, AUTHPAGEGUID) VALUES ('%s', '%s', '%s');",
                                    esc(authPlanPageGuid), esc(authPlanGuid), esc(pageGuid));
                                scripts.add(pageScript);
                                migrationScripts.add(new MigrationScriptDto(companyGuid, null, planGuid, "PLAN_PAGE", pageGuid, pageScript));

                                // Plan Page Buttons
                                if (page.getButtons() != null) {
                                    for (ButtonDto button : page.getButtons()) {
                                        String buttonGuid = button.getButtonGuid();
                                        String btnScript = String.format(
                                            "INSERT INTO ASAUTHPLANPAGEBUTTON (AUTHPLANPAGEGUID, AUTHBUTTONGUID) VALUES ('%s', '%s');",
                                            esc(authPlanPageGuid), esc(buttonGuid));
                                        scripts.add(btnScript);
                                        migrationScripts.add(new MigrationScriptDto(companyGuid, null, planGuid, "PLAN_PAGE_BUTTON", buttonGuid, btnScript));
                                    }
                                }
                            }
                        }

                        // Plan Transactions
                        if (plan.getPlanTransactions() != null) {
                            for (TransactionDto txn : plan.getPlanTransactions()) {
                                String txnGuid = txn.getTransactionGuid();
                                String txnKey = planKey + "|" + txnGuid;
                                String authTxnGuid = getOrGen.apply(txnKey);

                                String txnScript = String.format(
                                    "INSERT INTO ASAUTHTRANSACTION (AUTHTRANSACTIONGUID, AUTHPLANGUID, TRANSACTIONGUID) VALUES ('%s', '%s', '%s');",
                                    esc(authTxnGuid), esc(authPlanGuid), esc(txnGuid));
                                scripts.add(txnScript);
                                migrationScripts.add(new MigrationScriptDto(companyGuid, null, planGuid, "PLAN_TRANSACTION", txnGuid, txnScript));

                                // Plan Transaction Buttons
                                if (txn.getButtons() != null) {
                                    for (ButtonDto button : txn.getButtons()) {
                                        String buttonGuid = button.getButtonGuid();
                                        String btnScript = String.format(
                                            "INSERT INTO ASAUTHTRANSACTIONBUTTON (AUTHTRANSACTIONGUID, AUTHBUTTONGUID) VALUES ('%s', '%s');",
                                            esc(authTxnGuid), esc(buttonGuid));
                                        scripts.add(btnScript);
                                        migrationScripts.add(new MigrationScriptDto(companyGuid, null, planGuid, "PLAN_TRANSACTION_BUTTON", buttonGuid, btnScript));
                                    }
                                }
                            }
                        }

                        // Plan Inquiries
                        if (plan.getPlanInquiries() != null) {
                            for (InquiryDto inquiry : plan.getPlanInquiries()) {
                                String inqGuid = inquiry.getInquiryScreenNameGuid();
                                String piKey = planKey + "|" + inqGuid;
                                String authPiGuid = getOrGen.apply(piKey);

                                String inqScript = String.format(
                                    "INSERT INTO ASAUTHPLANINQUIRY (AUTHPLANINQUIRYGUID, AUTHPLANGUID, INQUIRYSCREENNAMEGUID) VALUES ('%s', '%s', '%s');",
                                    esc(authPiGuid), esc(authPlanGuid), esc(inqGuid));
                                scripts.add(inqScript);
                                migrationScripts.add(new MigrationScriptDto(companyGuid, null, planGuid, "PLAN_INQUIRY", inqGuid, inqScript));
                            }
                        }
                    }
                }

                // Products
                if (company.getProducts() != null) {
                    for (ProductAuthDto product : company.getProducts()) {
                        String productGuid = product.getProductGuid();
                        String prodKey = compKey + "|" + productGuid;
                        String authProdGuid = getOrGen.apply(prodKey);

                        String prodScript = String.format(
                            "INSERT INTO ASAUTHPRODUCT (AUTHPRODUCTGUID, AUTHCOMPANYGUID, PRODUCTGUID) VALUES ('%s', '%s', '%s');",
                            esc(authProdGuid), esc(authCompanyGuid), esc(productGuid));
                        scripts.add(prodScript);
                        migrationScripts.add(new MigrationScriptDto(companyGuid, productGuid, null, "PRODUCT", productGuid, prodScript));

                        // Product Pages
                        if (product.getProductPages() != null) {
                            for (PageDto page : product.getProductPages()) {
                                String pageGuid = page.getPageGuid();
                                String prodPageKey = prodKey + "|" + pageGuid;
                                String authProdPageGuid = getOrGen.apply(prodPageKey);

                                String pageScript = String.format(
                                    "INSERT INTO ASAUTHPRODUCTPAGE (AUTHPRODUCTPAGEGUID, AUTHPRODUCTGUID, AUTHPAGEGUID) VALUES ('%s', '%s', '%s');",
                                    esc(authProdPageGuid), esc(authProdGuid), esc(pageGuid));
                                scripts.add(pageScript);
                                migrationScripts.add(new MigrationScriptDto(companyGuid, productGuid, null, "PRODUCT_PAGE", pageGuid, pageScript));

                                // Product Page Buttons
                                if (page.getButtons() != null) {
                                    for (ButtonDto button : page.getButtons()) {
                                        String buttonGuid = button.getButtonGuid();
                                        String btnScript = String.format(
                                            "INSERT INTO ASAUTHPRODUCTPAGEBUTTON (AUTHPRODUCTPAGEGUID, AUTHBUTTONGUID) VALUES ('%s', '%s');",
                                            esc(authProdPageGuid), esc(buttonGuid));
                                        scripts.add(btnScript);
                                        migrationScripts.add(new MigrationScriptDto(companyGuid, productGuid, null, "PRODUCT_BUTTON", buttonGuid, btnScript));
                                    }
                                }
                            }
                        }

                        // Product Transactions
                        if (product.getProductTransactions() != null) {
                            for (TransactionDto txn : product.getProductTransactions()) {
                                String txnGuid = txn.getTransactionGuid();
                                String txnKey = prodKey + "|" + txnGuid;
                                String authProdTxnGuid = getOrGen.apply(txnKey);

                                String txnScript = String.format(
                                    "INSERT INTO ASAUTHPRODUCTTRANSACTION (AUTHPRODUCTTRANSACTIONGUID, AUTHPRODUCTGUID, TRANSACTIONGUID) VALUES ('%s', '%s', '%s');",
                                    esc(authProdTxnGuid), esc(authProdGuid), esc(txnGuid));
                                scripts.add(txnScript);
                                migrationScripts.add(new MigrationScriptDto(companyGuid, productGuid, null, "PRODUCT_TRANSACTION", txnGuid, txnScript));

                                // Product Transaction Buttons
                                if (txn.getButtons() != null) {
                                    for (ButtonDto button : txn.getButtons()) {
                                        String buttonGuid = button.getButtonGuid();
                                        String btnScript = String.format(
                                            "INSERT INTO ASAUTHPRODUCTTRANSACTIONBUTTON (AUTHPRODUCTTRANSACTIONGUID, AUTHBUTTONGUID) VALUES ('%s', '%s');",
                                            esc(authProdTxnGuid), esc(buttonGuid));
                                        scripts.add(btnScript);
                                        migrationScripts.add(new MigrationScriptDto(companyGuid, productGuid, null, "PRODUCT_TRANSACTION_BUTTON", buttonGuid, btnScript));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        log.info("Generated {} migration SQL scripts for security group: {} (GUID: {})", scripts.size(), groupName, guid);
        return new GenerateScriptsResponseDto(guid, groupName, scripts, migrationScripts);
    }

    private void loadExistingAuthGuids(String securityGroupGuid, Map<String, String> authGuidMap) {
        JdbcTemplate jdbc = new JdbcTemplate(secondaryDevDataSource);
        
        // 1. Companies
        jdbc.query(
            "SELECT AUTHCOMPANYGUID, COMPANYGUID FROM ASAUTHCOMPANY WHERE SECURITYGROUPGUID = ?",
            rs -> {
                String authGuid = rs.getString("AUTHCOMPANYGUID");
                String companyGuid = rs.getString("COMPANYGUID");
                authGuidMap.put(securityGroupGuid + "|" + (companyGuid != null ? companyGuid.trim() : ""), authGuid != null ? authGuid.trim() : "");
            },
            securityGroupGuid
        );
        
        // 2. Company Pages
        jdbc.query(
            "SELECT cp.AUTHCOMPANYPAGEGUID, c.COMPANYGUID, cp.AUTHPAGEGUID " +
            "FROM ASAUTHCOMPANYPAGE cp " +
            "JOIN ASAUTHCOMPANY c ON cp.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "WHERE c.SECURITYGROUPGUID = ?",
            rs -> {
                String comp = rs.getString("COMPANYGUID");
                String pg = rs.getString("AUTHPAGEGUID");
                authGuidMap.put(securityGroupGuid + "|" + (comp != null ? comp.trim() : "") + "|" + (pg != null ? pg.trim() : ""), rs.getString("AUTHCOMPANYPAGEGUID"));
            },
            securityGroupGuid
        );

        // 3. Plans
        jdbc.query(
            "SELECT p.AUTHPLANGUID, c.COMPANYGUID, p.PLANGUID " +
            "FROM ASAUTHPLAN p " +
            "JOIN ASAUTHCOMPANY c ON p.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "WHERE c.SECURITYGROUPGUID = ?",
            rs -> {
                String comp = rs.getString("COMPANYGUID");
                String plan = rs.getString("PLANGUID");
                authGuidMap.put(securityGroupGuid + "|" + (comp != null ? comp.trim() : "") + "|" + (plan != null ? plan.trim() : ""), rs.getString("AUTHPLANGUID"));
            },
            securityGroupGuid
        );

        // 4. Plan Pages
        jdbc.query(
            "SELECT pp.AUTHPLANPAGEGUID, c.COMPANYGUID, p.PLANGUID, pp.AUTHPAGEGUID " +
            "FROM ASAUTHPLANPAGE pp " +
            "JOIN ASAUTHPLAN p ON pp.AUTHPLANGUID = p.AUTHPLANGUID " +
            "JOIN ASAUTHCOMPANY c ON p.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "WHERE c.SECURITYGROUPGUID = ?",
            rs -> {
                String comp = rs.getString("COMPANYGUID");
                String plan = rs.getString("PLANGUID");
                String pg = rs.getString("AUTHPAGEGUID");
                authGuidMap.put(securityGroupGuid + "|" + (comp != null ? comp.trim() : "") + "|" + (plan != null ? plan.trim() : "") + "|" + (pg != null ? pg.trim() : ""), rs.getString("AUTHPLANPAGEGUID"));
            },
            securityGroupGuid
        );

        // 5. Transactions
        jdbc.query(
            "SELECT t.AUTHTRANSACTIONGUID, c.COMPANYGUID, p.PLANGUID, t.TRANSACTIONGUID " +
            "FROM ASAUTHTRANSACTION t " +
            "JOIN ASAUTHPLAN p ON t.AUTHPLANGUID = p.AUTHPLANGUID " +
            "JOIN ASAUTHCOMPANY c ON p.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "WHERE c.SECURITYGROUPGUID = ?",
            rs -> {
                String comp = rs.getString("COMPANYGUID");
                String plan = rs.getString("PLANGUID");
                String txn = rs.getString("TRANSACTIONGUID");
                authGuidMap.put(securityGroupGuid + "|" + (comp != null ? comp.trim() : "") + "|" + (plan != null ? plan.trim() : "") + "|" + (txn != null ? txn.trim() : ""), rs.getString("AUTHTRANSACTIONGUID"));
            },
            securityGroupGuid
        );

        // 6. Products
        jdbc.query(
            "SELECT pr.AUTHPRODUCTGUID, c.COMPANYGUID, pr.PRODUCTGUID " +
            "FROM ASAUTHPRODUCT pr " +
            "JOIN ASAUTHCOMPANY c ON pr.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "WHERE c.SECURITYGROUPGUID = ?",
            rs -> {
                String comp = rs.getString("COMPANYGUID");
                String prod = rs.getString("PRODUCTGUID");
                authGuidMap.put(securityGroupGuid + "|" + (comp != null ? comp.trim() : "") + "|" + (prod != null ? prod.trim() : ""), rs.getString("AUTHPRODUCTGUID"));
            },
            securityGroupGuid
        );

        // 7. Product Pages
        jdbc.query(
            "SELECT pp.AUTHPRODUCTPAGEGUID, c.COMPANYGUID, pr.PRODUCTGUID, pp.AUTHPAGEGUID " +
            "FROM ASAUTHPRODUCTPAGE pp " +
            "JOIN ASAUTHPRODUCT pr ON pp.AUTHPRODUCTGUID = pr.AUTHPRODUCTGUID " +
            "JOIN ASAUTHCOMPANY c ON pr.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "WHERE c.SECURITYGROUPGUID = ?",
            rs -> {
                String comp = rs.getString("COMPANYGUID");
                String prod = rs.getString("PRODUCTGUID");
                String pg = rs.getString("AUTHPAGEGUID");
                authGuidMap.put(securityGroupGuid + "|" + (comp != null ? comp.trim() : "") + "|" + (prod != null ? prod.trim() : "") + "|" + (pg != null ? pg.trim() : ""), rs.getString("AUTHPRODUCTPAGEGUID"));
            },
            securityGroupGuid
        );

        // 8. Product Transactions
        jdbc.query(
            "SELECT pt.AUTHPRODUCTTRANSACTIONGUID, c.COMPANYGUID, pr.PRODUCTGUID, pt.TRANSACTIONGUID " +
            "FROM ASAUTHPRODUCTTRANSACTION pt " +
            "JOIN ASAUTHPRODUCT pr ON pt.AUTHPRODUCTGUID = pr.AUTHPRODUCTGUID " +
            "JOIN ASAUTHCOMPANY c ON pr.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "WHERE c.SECURITYGROUPGUID = ?",
            rs -> {
                String comp = rs.getString("COMPANYGUID");
                String prod = rs.getString("PRODUCTGUID");
                String txn = rs.getString("TRANSACTIONGUID");
                authGuidMap.put(securityGroupGuid + "|" + (comp != null ? comp.trim() : "") + "|" + (prod != null ? prod.trim() : "") + "|" + (txn != null ? txn.trim() : ""), rs.getString("AUTHPRODUCTTRANSACTIONGUID"));
            },
            securityGroupGuid
        );

        // 9. Company Page Buttons
        jdbc.query(
            "SELECT cpb.AUTHCOMPANYPAGEBUTTONGUID, c.COMPANYGUID, cp.AUTHPAGEGUID, cpb.AUTHBUTTONGUID " +
            "FROM ASAUTHCOMPANYPAGEBUTTON cpb " +
            "JOIN ASAUTHCOMPANYPAGE cp ON cpb.AUTHCOMPANYPAGEGUID = cp.AUTHCOMPANYPAGEGUID " +
            "JOIN ASAUTHCOMPANY c ON cp.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "WHERE c.SECURITYGROUPGUID = ?",
            rs -> {
                String comp = rs.getString("COMPANYGUID");
                String pg = rs.getString("AUTHPAGEGUID");
                String btn = rs.getString("AUTHBUTTONGUID");
                authGuidMap.put(securityGroupGuid + "|" + (comp != null ? comp.trim() : "") + "|" + (pg != null ? pg.trim() : "") + "|" + (btn != null ? btn.trim() : ""), rs.getString("AUTHCOMPANYPAGEBUTTONGUID"));
            },
            securityGroupGuid
        );

        // 10. Company Inquiries
        jdbc.query(
            "SELECT ci.AUTHCOMPANYINQUIRYGUID, c.COMPANYGUID, ci.INQUIRYSCREENNAMEGUID " +
            "FROM ASAUTHCOMPANYINQUIRY ci " +
            "JOIN ASAUTHCOMPANY c ON ci.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "WHERE c.SECURITYGROUPGUID = ?",
            rs -> {
                String comp = rs.getString("COMPANYGUID");
                String inq = rs.getString("INQUIRYSCREENNAMEGUID");
                authGuidMap.put(securityGroupGuid + "|" + (comp != null ? comp.trim() : "") + "|" + (inq != null ? inq.trim() : ""), rs.getString("AUTHCOMPANYINQUIRYGUID"));
            },
            securityGroupGuid
        );

        // 11. Plan Inquiries
        jdbc.query(
            "SELECT pi.AUTHPLANINQUIRYGUID, c.COMPANYGUID, p.PLANGUID, pi.INQUIRYSCREENNAMEGUID " +
            "FROM ASAUTHPLANINQUIRY pi " +
            "JOIN ASAUTHPLAN p ON pi.AUTHPLANGUID = p.AUTHPLANGUID " +
            "JOIN ASAUTHCOMPANY c ON p.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID " +
            "WHERE c.SECURITYGROUPGUID = ?",
            rs -> {
                String comp = rs.getString("COMPANYGUID");
                String plan = rs.getString("PLANGUID");
                String inq = rs.getString("INQUIRYSCREENNAMEGUID");
                authGuidMap.put(securityGroupGuid + "|" + (comp != null ? comp.trim() : "") + "|" + (plan != null ? plan.trim() : "") + "|" + (inq != null ? inq.trim() : ""), rs.getString("AUTHPLANINQUIRYGUID"));
            },
            securityGroupGuid
        );
    }

    // ===================================================================
    //  Flattening helpers — convert nested DTO trees to Sets of pipe-
    //  delimited composite keys for O(n) set-difference comparisons
    // ===================================================================

    private Set<String> flattenCompanyKeys(SecurityGroupDto dto) {
        String g = dto.getSecurityGroupGuid();
        return dto.getCompanies().stream()
                .map(c -> g + "|" + c.getCompanyGuid())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> flattenCompanyPageKeys(SecurityGroupDto dto) {
        String g = dto.getSecurityGroupGuid();
        Set<String> keys = new LinkedHashSet<>();
        for (CompanyAuthDto c : dto.getCompanies()) {
            for (PageDto p : c.getCompanyPages()) {
                keys.add(g + "|" + c.getCompanyGuid() + "|" + p.getPageGuid());
            }
        }
        return keys;
    }

    private Set<String> flattenCompanyPageButtonKeys(SecurityGroupDto dto) {
        String g = dto.getSecurityGroupGuid();
        Set<String> keys = new LinkedHashSet<>();
        for (CompanyAuthDto c : dto.getCompanies()) {
            for (PageDto p : c.getCompanyPages()) {
                for (ButtonDto b : p.getButtons()) {
                    keys.add(g + "|" + c.getCompanyGuid() + "|" + p.getPageGuid() + "|" + b.getButtonGuid());
                }
            }
        }
        return keys;
    }

    private Set<String> flattenCompanyInquiryKeys(SecurityGroupDto dto) {
        String g = dto.getSecurityGroupGuid();
        Set<String> keys = new LinkedHashSet<>();
        for (CompanyAuthDto c : dto.getCompanies()) {
            for (InquiryDto i : c.getCompanyInquiries()) {
                keys.add(g + "|" + c.getCompanyGuid() + "|" + i.getInquiryScreenNameGuid());
            }
        }
        return keys;
    }

    private Set<String> flattenCompanyWebServiceKeys(SecurityGroupDto dto) {
        String g = dto.getSecurityGroupGuid();
        Set<String> keys = new LinkedHashSet<>();
        for (CompanyAuthDto c : dto.getCompanies()) {
            for (WebServiceDto w : c.getCompanyWebServices()) {
                keys.add(g + "|" + c.getCompanyGuid() + "|" + w.getWebServiceGuid());
            }
        }
        return keys;
    }

    private Set<String> flattenPlanKeys(SecurityGroupDto dto) {
        String g = dto.getSecurityGroupGuid();
        Set<String> keys = new LinkedHashSet<>();
        for (CompanyAuthDto c : dto.getCompanies()) {
            for (PlanAuthDto p : c.getPlans()) {
                keys.add(g + "|" + c.getCompanyGuid() + "|" + p.getPlanGuid());
            }
        }
        return keys;
    }

    private Set<String> flattenPlanPageKeys(SecurityGroupDto dto) {
        String g = dto.getSecurityGroupGuid();
        Set<String> keys = new LinkedHashSet<>();
        for (CompanyAuthDto c : dto.getCompanies()) {
            for (PlanAuthDto plan : c.getPlans()) {
                for (PageDto pg : plan.getPlanPages()) {
                    keys.add(g + "|" + c.getCompanyGuid() + "|" + plan.getPlanGuid() + "|" + pg.getPageGuid());
                }
            }
        }
        return keys;
    }

    private Set<String> flattenPlanPageButtonKeys(SecurityGroupDto dto) {
        String g = dto.getSecurityGroupGuid();
        Set<String> keys = new LinkedHashSet<>();
        for (CompanyAuthDto c : dto.getCompanies()) {
            for (PlanAuthDto plan : c.getPlans()) {
                for (PageDto pg : plan.getPlanPages()) {
                    for (ButtonDto b : pg.getButtons()) {
                        keys.add(g + "|" + c.getCompanyGuid() + "|" + plan.getPlanGuid() + "|" + pg.getPageGuid() + "|" + b.getButtonGuid());
                    }
                }
            }
        }
        return keys;
    }

    private Set<String> flattenTransactionKeys(SecurityGroupDto dto) {
        String g = dto.getSecurityGroupGuid();
        Set<String> keys = new LinkedHashSet<>();
        for (CompanyAuthDto c : dto.getCompanies()) {
            for (PlanAuthDto plan : c.getPlans()) {
                for (TransactionDto t : plan.getPlanTransactions()) {
                    keys.add(g + "|" + c.getCompanyGuid() + "|" + plan.getPlanGuid() + "|" + t.getTransactionGuid());
                }
            }
        }
        return keys;
    }

    private Set<String> flattenPlanInquiryKeys(SecurityGroupDto dto) {
        String g = dto.getSecurityGroupGuid();
        Set<String> keys = new LinkedHashSet<>();
        for (CompanyAuthDto c : dto.getCompanies()) {
            for (PlanAuthDto plan : c.getPlans()) {
                for (InquiryDto i : plan.getPlanInquiries()) {
                    keys.add(g + "|" + c.getCompanyGuid() + "|" + plan.getPlanGuid() + "|" + i.getInquiryScreenNameGuid());
                }
            }
        }
        return keys;
    }

    private Set<String> flattenProductKeys(SecurityGroupDto dto) {
        String g = dto.getSecurityGroupGuid();
        Set<String> keys = new LinkedHashSet<>();
        for (CompanyAuthDto c : dto.getCompanies()) {
            for (ProductAuthDto pr : c.getProducts()) {
                keys.add(g + "|" + c.getCompanyGuid() + "|" + pr.getProductGuid());
            }
        }
        return keys;
    }

    private Set<String> flattenProductPageKeys(SecurityGroupDto dto) {
        String g = dto.getSecurityGroupGuid();
        Set<String> keys = new LinkedHashSet<>();
        for (CompanyAuthDto c : dto.getCompanies()) {
            for (ProductAuthDto pr : c.getProducts()) {
                for (PageDto pg : pr.getProductPages()) {
                    keys.add(g + "|" + c.getCompanyGuid() + "|" + pr.getProductGuid() + "|" + pg.getPageGuid());
                }
            }
        }
        return keys;
    }

    private Set<String> flattenProductPageButtonKeys(SecurityGroupDto dto) {
        String g = dto.getSecurityGroupGuid();
        Set<String> keys = new LinkedHashSet<>();
        for (CompanyAuthDto c : dto.getCompanies()) {
            for (ProductAuthDto pr : c.getProducts()) {
                for (PageDto pg : pr.getProductPages()) {
                    for (ButtonDto b : pg.getButtons()) {
                        keys.add(g + "|" + c.getCompanyGuid() + "|" + pr.getProductGuid() + "|" + pg.getPageGuid() + "|" + b.getButtonGuid());
                    }
                }
            }
        }
        return keys;
    }

    private Set<String> flattenProductTransactionKeys(SecurityGroupDto dto) {
        String g = dto.getSecurityGroupGuid();
        Set<String> keys = new LinkedHashSet<>();
        for (CompanyAuthDto c : dto.getCompanies()) {
            for (ProductAuthDto pr : c.getProducts()) {
                for (TransactionDto t : pr.getProductTransactions()) {
                    keys.add(g + "|" + c.getCompanyGuid() + "|" + pr.getProductGuid() + "|" + t.getTransactionGuid());
                }
            }
        }
        return keys;
    }

    private Set<String> flattenProductTransactionButtonKeys(SecurityGroupDto dto) {
        String g = dto.getSecurityGroupGuid();
        Set<String> keys = new LinkedHashSet<>();
        for (CompanyAuthDto c : dto.getCompanies()) {
            for (ProductAuthDto pr : c.getProducts()) {
                for (TransactionDto t : pr.getProductTransactions()) {
                    for (ButtonDto b : t.getButtons()) {
                        keys.add(g + "|" + c.getCompanyGuid() + "|" + pr.getProductGuid() + "|" + t.getTransactionGuid() + "|" + b.getButtonGuid());
                    }
                }
            }
        }
        return keys;
    }

    private Set<String> flattenTransactionButtonKeys(SecurityGroupDto dto) {
        String g = dto.getSecurityGroupGuid();
        Set<String> keys = new LinkedHashSet<>();
        for (CompanyAuthDto c : dto.getCompanies()) {
            for (PlanAuthDto plan : c.getPlans()) {
                for (TransactionDto t : plan.getPlanTransactions()) {
                    for (ButtonDto b : t.getButtons()) {
                        keys.add(g + "|" + c.getCompanyGuid() + "|" + plan.getPlanGuid() + "|" + t.getTransactionGuid() + "|" + b.getButtonGuid());
                    }
                }
            }
        }
        return keys;
    }

    // ── Utility ─────────────────────────────────────────────────────────

    /**
     * Helper to batch collection queries in maximum chunks of 1000 to prevent Oracle ORA-01795 error.
     */
    private <T> List<T> fetchInChunks(Collection<String> keys, java.util.function.Function<Collection<String>, List<T>> fetcher) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>(keys);
        List<T> results = new ArrayList<>();
        int limit = 1000;
        for (int i = 0; i < list.size(); i += limit) {
            List<String> chunk = list.subList(i, Math.min(i + limit, list.size()));
            results.addAll(fetcher.apply(chunk));
        }
        return results;
    }

    /** Set difference: elements in {@code a} that are NOT in {@code b}. */
    private List<String> diff(Set<String> a, Set<String> b) {
        return a.stream().filter(k -> !b.contains(k)).collect(Collectors.toList());
    }

    public static class ParameterizedSql {
        public final String sql;
        public final Object[] args;
        public ParameterizedSql(String sql, Object[] args) {
            this.sql = sql;
            this.args = args;
        }
    }

    private ParameterizedSql parameterize(String sql) {
        StringBuilder template = new StringBuilder();
        List<Object> args = new ArrayList<>();
        int len = sql.length();
        boolean inQuotes = false;
        StringBuilder currentVal = null;
        for (int i = 0; i < len; i++) {
            char c = sql.charAt(i);
            if (c == '\'') {
                if (inQuotes) {
                    // Check if it's an escaped single quote (two single quotes in a row)
                    if (i + 1 < len && sql.charAt(i + 1) == '\'') {
                        currentVal.append('\'');
                        i++; // Skip the second quote
                    } else {
                        inQuotes = false;
                        args.add(currentVal.toString());
                        template.append('?');
                    }
                } else {
                    inQuotes = true;
                    currentVal = new StringBuilder();
                }
            } else {
                if (inQuotes) {
                    currentVal.append(c);
                } else {
                    template.append(c);
                }
            }
        }
        String tSql = template.toString().trim();
        if (tSql.endsWith(";")) {
            tSql = tSql.substring(0, tSql.length() - 1).trim();
        }
        return new ParameterizedSql(tSql, args.toArray());
    }

    private Set<String> getItAdminGuids() {
        Set<String> guids = new java.util.HashSet<>();
        JdbcTemplate jdbc = new JdbcTemplate(secondaryDevDataSource);
        
        // Get IT ADMIN Security Group GUID
        List<String> groupGuids = jdbc.query(
            "SELECT SECURITYGROUPGUID FROM ASSECURITYGROUP WHERE UPPER(TRIM(GROUPNAME)) = 'IT ADMIN'",
            (rs, rowNum) -> rs.getString("SECURITYGROUPGUID")
        );
        if (groupGuids.isEmpty()) {
            return guids;
        }
        String itAdminGuid = groupGuids.get(0);
        if (itAdminGuid != null) {
            guids.add(itAdminGuid.trim().toUpperCase());
        }
        
        // Load all child authorization GUIDs for IT ADMIN
        // 1. Companies
        jdbc.query("SELECT AUTHCOMPANYGUID FROM ASAUTHCOMPANY WHERE SECURITYGROUPGUID = ?",
            rs -> { String g = rs.getString("AUTHCOMPANYGUID"); if (g != null) guids.add(g.trim().toUpperCase()); },
            itAdminGuid);
            
        // 2. Company Pages
        jdbc.query("SELECT cp.AUTHCOMPANYPAGEGUID FROM ASAUTHCOMPANYPAGE cp JOIN ASAUTHCOMPANY c ON cp.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID WHERE c.SECURITYGROUPGUID = ?",
            rs -> { String g = rs.getString("AUTHCOMPANYPAGEGUID"); if (g != null) guids.add(g.trim().toUpperCase()); },
            itAdminGuid);
            
        // 3. Company Page Buttons
        jdbc.query("SELECT cpb.AUTHCOMPANYPAGEBUTTONGUID FROM ASAUTHCOMPANYPAGEBUTTON cpb JOIN ASAUTHCOMPANYPAGE cp ON cpb.AUTHCOMPANYPAGEGUID = cp.AUTHCOMPANYPAGEGUID JOIN ASAUTHCOMPANY c ON cp.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID WHERE c.SECURITYGROUPGUID = ?",
            rs -> { String g = rs.getString("AUTHCOMPANYPAGEBUTTONGUID"); if (g != null) guids.add(g.trim().toUpperCase()); },
            itAdminGuid);

        // 4. Company Inquiries
        jdbc.query("SELECT ci.AUTHCOMPANYINQUIRYGUID FROM ASAUTHCOMPANYINQUIRY ci JOIN ASAUTHCOMPANY c ON ci.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID WHERE c.SECURITYGROUPGUID = ?",
            rs -> { String g = rs.getString("AUTHCOMPANYINQUIRYGUID"); if (g != null) guids.add(g.trim().toUpperCase()); },
            itAdminGuid);

        // 5. Plans
        jdbc.query("SELECT p.AUTHPLANGUID FROM ASAUTHPLAN p JOIN ASAUTHCOMPANY c ON p.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID WHERE c.SECURITYGROUPGUID = ?",
            rs -> { String g = rs.getString("AUTHPLANGUID"); if (g != null) guids.add(g.trim().toUpperCase()); },
            itAdminGuid);

        // 6. Plan Pages
        jdbc.query("SELECT pp.AUTHPLANPAGEGUID FROM ASAUTHPLANPAGE pp JOIN ASAUTHPLAN p ON pp.AUTHPLANGUID = p.AUTHPLANGUID JOIN ASAUTHCOMPANY c ON p.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID WHERE c.SECURITYGROUPGUID = ?",
            rs -> { String g = rs.getString("AUTHPLANPAGEGUID"); if (g != null) guids.add(g.trim().toUpperCase()); },
            itAdminGuid);

        // 7. Plan Inquiries
        jdbc.query("SELECT pi.AUTHPLANINQUIRYGUID FROM ASAUTHPLANINQUIRY pi JOIN ASAUTHPLAN p ON pi.AUTHPLANGUID = p.AUTHPLANGUID JOIN ASAUTHCOMPANY c ON p.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID WHERE c.SECURITYGROUPGUID = ?",
            rs -> { String g = rs.getString("AUTHPLANINQUIRYGUID"); if (g != null) guids.add(g.trim().toUpperCase()); },
            itAdminGuid);

        // 8. Transactions
        jdbc.query("SELECT t.AUTHTRANSACTIONGUID FROM ASAUTHTRANSACTION t JOIN ASAUTHPLAN p ON t.AUTHPLANGUID = p.AUTHPLANGUID JOIN ASAUTHCOMPANY c ON p.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID WHERE c.SECURITYGROUPGUID = ?",
            rs -> { String g = rs.getString("AUTHTRANSACTIONGUID"); if (g != null) guids.add(g.trim().toUpperCase()); },
            itAdminGuid);

        // 9. Products
        jdbc.query("SELECT pr.AUTHPRODUCTGUID FROM ASAUTHPRODUCT pr JOIN ASAUTHCOMPANY c ON pr.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID WHERE c.SECURITYGROUPGUID = ?",
            rs -> { String g = rs.getString("AUTHPRODUCTGUID"); if (g != null) guids.add(g.trim().toUpperCase()); },
            itAdminGuid);

        // 10. Product Pages
        jdbc.query("SELECT pp.AUTHPRODUCTPAGEGUID FROM ASAUTHPRODUCTPAGE pp JOIN ASAUTHPRODUCT pr ON pp.AUTHPRODUCTGUID = pr.AUTHPRODUCTGUID JOIN ASAUTHCOMPANY c ON pr.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID WHERE c.SECURITYGROUPGUID = ?",
            rs -> { String g = rs.getString("AUTHPRODUCTPAGEGUID"); if (g != null) guids.add(g.trim().toUpperCase()); },
            itAdminGuid);

        // 11. Product Transactions
        jdbc.query("SELECT pt.AUTHPRODUCTTRANSACTIONGUID FROM ASAUTHPRODUCTTRANSACTION pt JOIN ASAUTHPRODUCT pr ON pt.AUTHPRODUCTGUID = pr.AUTHPRODUCTGUID JOIN ASAUTHCOMPANY c ON pr.AUTHCOMPANYGUID = c.AUTHCOMPANYGUID WHERE c.SECURITYGROUPGUID = ?",
            rs -> { String g = rs.getString("AUTHPRODUCTTRANSACTIONGUID"); if (g != null) guids.add(g.trim().toUpperCase()); },
            itAdminGuid);

        return guids;
    }

    /** Execute generated delta SQL scripts on secondaryDev database. */
    @Transactional(value = "secondaryDevTransactionManager")
    public void executeScripts(List<String> scripts) {
        if (scripts == null || scripts.isEmpty()) {
            log.info("No scripts to execute.");
            return;
        }

        // Relocated block: restrict modifying IT ADMIN group on execution
        Set<String> itAdminGuids = getItAdminGuids();
        for (String script : scripts) {
            if (script == null) continue;
            String upper = script.toUpperCase();
            if (upper.contains("'IT ADMIN'") || upper.contains("\"IT ADMIN\"")) {
                throw new IllegalArgumentException("Modifying the 'IT ADMIN' security group is restricted as it is the base group for all.");
            }
            if (!itAdminGuids.isEmpty()) {
                for (String guid : itAdminGuids) {
                    if (upper.contains(guid)) {
                        throw new IllegalArgumentException("Modifying the 'IT ADMIN' security group is restricted as it is the base group for all.");
                    }
                }
            }
        }

        log.info("Starting execution of {} SQL scripts on secondaryDev database", scripts.size());
        
        List<String> deleteScripts = new ArrayList<>();
        List<String> insertScripts = new ArrayList<>();
        for (String script : scripts) {
            if (script == null) continue;
            String trimmed = script.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                continue;
            }
            if (trimmed.toUpperCase().startsWith("DELETE")) {
                deleteScripts.add(trimmed);
            } else {
                insertScripts.add(trimmed);
            }
        }
        log.info("Parsed scripts: {} DELETE statements, {} INSERT statements", deleteScripts.size(), insertScripts.size());
        
        Map<String, List<Object[]>> batchedInserts = new java.util.LinkedHashMap<>();
        for (String script : insertScripts) {
            ParameterizedSql paramSql = parameterize(script);
            List<Object[]> argsList = batchedInserts.computeIfAbsent(paramSql.sql, k -> new ArrayList<>());
            argsList.add(paramSql.args);
        }
        
        Map<Integer, Map<String, List<Object[]>>> levelsMap = new java.util.TreeMap<>();
        for (Map.Entry<String, List<Object[]>> entry : batchedInserts.entrySet()) {
            String sql = entry.getKey();
            List<Object[]> argsList = entry.getValue();
            int level = getInsertLevel(sql);
            levelsMap.computeIfAbsent(level, k -> new java.util.LinkedHashMap<>()).put(sql, argsList);
        }
        
        int numConnections = 24;
        ExecutorService executor = Executors.newFixedThreadPool(numConnections);
        List<Connection> conns = new ArrayList<>();
        Set<Connection> usedConnections = java.util.Collections.synchronizedSet(new java.util.HashSet<>());
        
        try {
            List<Future<Connection>> connFutures = new ArrayList<>();
            for (int i = 0; i < numConnections; i++) {
                connFutures.add(executor.submit(() -> {
                    Connection conn = secondaryDevDataSource.getConnection();
                    conn.setAutoCommit(false);
                    return conn;
                }));
            }
            for (Future<Connection> f : connFutures) {
                conns.add(f.get());
            }
        } catch (Exception e) {
            executor.shutdown();
            for (Connection conn : conns) {
                try { conn.close(); } catch (Exception ignored) {}
            }
            throw new RuntimeException("Failed to acquire connections from datasource pool", e);
        }
        
        BlockingQueue<Connection> connectionQueue = new LinkedBlockingQueue<>(conns);
        long startExec = System.currentTimeMillis();
        
        try {
            if (!deleteScripts.isEmpty()) {
                long startDelete = System.currentTimeMillis();
                Connection deleteConn = conns.get(0);
                usedConnections.add(deleteConn);
                try (java.sql.Statement stmt = deleteConn.createStatement()) {
                    for (String deleteSql : deleteScripts) {
                        if (deleteSql.endsWith(";")) deleteSql = deleteSql.substring(0, deleteSql.length() - 1);
                        stmt.addBatch(deleteSql);
                    }
                    stmt.executeBatch();
                }
            }
            
            for (Map.Entry<Integer, Map<String, List<Object[]>>> levelEntry : levelsMap.entrySet()) {
                Map<String, List<Object[]>> templates = levelEntry.getValue();
                List<Future<Void>> futures = new ArrayList<>();
                for (Map.Entry<String, List<Object[]>> templateEntry : templates.entrySet()) {
                    String sql = templateEntry.getKey();
                    List<Object[]> argsList = templateEntry.getValue();
                    int chunkSize = Math.max(1000, (int) Math.ceil((double) argsList.size() / numConnections));
                    for (int i = 0; i < argsList.size(); i += chunkSize) {
                        List<Object[]> chunk = argsList.subList(i, Math.min(argsList.size(), i + chunkSize));
                        futures.add(executor.submit(() -> {
                            Connection conn = connectionQueue.take();
                            usedConnections.add(conn);
                            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                                for (Object[] args : chunk) {
                                    for (int j = 0; j < args.length; j++) {
                                        if (args[j] == null) ps.setNull(j + 1, java.sql.Types.VARCHAR);
                                        else ps.setObject(j + 1, args[j]);
                                    }
                                    ps.addBatch();
                                }
                                ps.executeBatch();
                            } finally {
                                connectionQueue.put(conn);
                            }
                            return null;
                        }));
                    }
                }
                for (Future<Void> future : futures) {
                    future.get();
                }
            }
            
            // Parallel commit — only on connections that did actual work
            long startCommit = System.currentTimeMillis();
            List<Future<Void>> commitFutures = new ArrayList<>();
            for (Connection conn : usedConnections) {
                commitFutures.add(executor.submit(() -> { conn.commit(); return null; }));
            }
            for (Future<Void> f : commitFutures) {
                f.get();
            }
            long endExec = System.currentTimeMillis();
            log.info("Committed {}/{} connections in {} ms. Total execution time: {} ms.",
                    usedConnections.size(), numConnections, (endExec - startCommit), (endExec - startExec));
        } catch (Exception e) {
            log.error("Error occurred during script execution. Rolling back all connections.", e);
            for (Connection conn : conns) {
                try { conn.rollback(); } catch (Exception ignored) {}
            }
            throw new RuntimeException("Script execution failed and all changes were rolled back.", e);
        } finally {
            executor.shutdown();
            for (Connection conn : conns) {
                try { conn.close(); } catch (Exception ignored) {}
            }
        }
    }

    private int getInsertLevel(String sql) {
        String upper = sql.toUpperCase();
        if (upper.contains("ASSECURITYGROUP")) {
            return 0;
        }
        if (upper.contains("ASAUTHCOMPANYPAGEBUTTON")) {
            return 3;
        }
        if (upper.contains("ASAUTHCOMPANYPAGE")) {
            return 2;
        }
        if (upper.contains("ASAUTHCOMPANYINQUIRY")) {
            return 2;
        }
        if (upper.contains("ASAUTHCOMPANYWEBSERVICE")) {
            return 2;
        }
        if (upper.contains("ASAUTHCOMPANY")) {
            return 1;
        }
        if (upper.contains("ASAUTHPLANPAGEBUTTON")) {
            return 4;
        }
        if (upper.contains("ASAUTHPLANPAGE")) {
            return 3;
        }
        if (upper.contains("ASAUTHTRANSACTIONBUTTON")) {
            return 4;
        }
        if (upper.contains("ASAUTHTRANSACTION")) {
            return 3;
        }
        if (upper.contains("ASAUTHPLANINQUIRY")) {
            return 3;
        }
        if (upper.contains("ASAUTHPLAN")) {
            return 2;
        }
        if (upper.contains("ASAUTHPRODUCTPAGEBUTTON")) {
            return 4;
        }
        if (upper.contains("ASAUTHPRODUCTPAGE")) {
            return 3;
        }
        if (upper.contains("ASAUTHPRODUCTTRANSACTIONBUTTON")) {
            return 4;
        }
        if (upper.contains("ASAUTHPRODUCTTRANSACTION")) {
            return 3;
        }
        if (upper.contains("ASAUTHPRODUCT")) {
            return 2;
        }
        return 5;
    }

    private <T> List<List<T>> partition(List<T> list, int numParts) {
        List<List<T>> partitions = new ArrayList<>();
        if (list == null || list.isEmpty()) {
            return partitions;
        }
        int size = list.size();
        int chunkSize = (int) Math.ceil((double) size / numParts);
        for (int i = 0; i < size; i += chunkSize) {
            partitions.add(list.subList(i, Math.min(size, i + chunkSize)));
        }
        return partitions;
    }

    /** Escape single quotes for safe SQL string literals. */
    private String esc(String value) {
        if (value == null) return "";
        return value.replace("'", "''");
    }

    /**
     * Delete a security group and all its nested configuration/relations
     * from the secondaryDev database.
     */
    @Transactional(value = "secondaryDevTransactionManager")
    public void deleteSecurityGroup(String securityGroupGuid) {
        log.info("Request to delete security group GUID: {}", securityGroupGuid);
        
        AsSecurityGroup group = securityGroupRepo.findById(securityGroupGuid)
                .orElseThrow(() -> {
                    log.error("Security group not found with GUID: {}", securityGroupGuid);
                    return new IllegalArgumentException("Security group not found with GUID: " + securityGroupGuid);
                });

        if (group.getGROUPNAME() != null && group.getGROUPNAME().trim().equalsIgnoreCase("IT ADMIN")) {
            log.error("Restrict deletion: Cannot delete 'IT ADMIN' security group.");
            throw new IllegalArgumentException("Deleting the 'IT ADMIN' security group is restricted as it is the base group for all.");
        }

        JdbcTemplate jdbc = new JdbcTemplate(secondaryDevDataSource);
        long startTime = System.currentTimeMillis();

        log.info("Deleting dependent authorization records for security group: {} (GUID: {})", 
                group.getGROUPNAME(), securityGroupGuid);

        // 1. ASAUTHPRODUCTTRANSACTIONBUTTON
        int count1 = jdbc.update(
            "DELETE FROM ASAUTHPRODUCTTRANSACTIONBUTTON WHERE AUTHPRODUCTTRANSACTIONGUID IN (" +
            "  SELECT apt.AUTHPRODUCTTRANSACTIONGUID FROM ASAUTHPRODUCTTRANSACTION apt" +
            "  JOIN ASAUTHPRODUCT ap ON apt.AUTHPRODUCTGUID = ap.AUTHPRODUCTGUID" +
            "  JOIN ASAUTHCOMPANY ac ON ap.AUTHCOMPANYGUID = ac.AUTHCOMPANYGUID" +
            "  WHERE ac.SECURITYGROUPGUID = ?" +
            ")", securityGroupGuid);
        log.info("Deleted {} rows from ASAUTHPRODUCTTRANSACTIONBUTTON", count1);

        // 2. ASAUTHTRANSACTIONBUTTON
        int count2 = jdbc.update(
            "DELETE FROM ASAUTHTRANSACTIONBUTTON WHERE AUTHTRANSACTIONGUID IN (" +
            "  SELECT at.AUTHTRANSACTIONGUID FROM ASAUTHTRANSACTION at" +
            "  JOIN ASAUTHPLAN ap ON at.AUTHPLANGUID = ap.AUTHPLANGUID" +
            "  JOIN ASAUTHCOMPANY ac ON ap.AUTHCOMPANYGUID = ac.AUTHCOMPANYGUID" +
            "  WHERE ac.SECURITYGROUPGUID = ?" +
            ")", securityGroupGuid);
        log.info("Deleted {} rows from ASAUTHTRANSACTIONBUTTON", count2);

        // 3. ASAUTHPRODUCTPAGEBUTTON
        int count3 = jdbc.update(
            "DELETE FROM ASAUTHPRODUCTPAGEBUTTON WHERE AUTHPRODUCTPAGEGUID IN (" +
            "  SELECT app.AUTHPRODUCTPAGEGUID FROM ASAUTHPRODUCTPAGE app" +
            "  JOIN ASAUTHPRODUCT ap ON apt.AUTHPRODUCTGUID = ap.AUTHPRODUCTGUID" +
            "  JOIN ASAUTHCOMPANY ac ON ap.AUTHCOMPANYGUID = ac.AUTHCOMPANYGUID" +
            "  WHERE ac.SECURITYGROUPGUID = ?" +
            ")", securityGroupGuid);
        log.info("Deleted {} rows from ASAUTHPRODUCTPAGEBUTTON", count3);

        // 4. ASAUTHPRODUCTPAGE
        int count4 = jdbc.update(
            "DELETE FROM ASAUTHPRODUCTPAGE WHERE AUTHPRODUCTGUID IN (" +
            "  SELECT ap.AUTHPRODUCTGUID FROM ASAUTHPRODUCT ap" +
            "  JOIN ASAUTHCOMPANY ac ON ap.AUTHCOMPANYGUID = ac.AUTHCOMPANYGUID" +
            "  WHERE ac.SECURITYGROUPGUID = ?" +
            ")", securityGroupGuid);
        log.info("Deleted {} rows from ASAUTHPRODUCTPAGE", count4);

        // 5. ASAUTHPRODUCTTRANSACTION
        int count5 = jdbc.update(
            "DELETE FROM ASAUTHPRODUCTTRANSACTION WHERE AUTHPRODUCTGUID IN (" +
            "  SELECT ap.AUTHPRODUCTGUID FROM ASAUTHPRODUCT ap" +
            "  JOIN ASAUTHCOMPANY ac ON ap.AUTHCOMPANYGUID = ac.AUTHCOMPANYGUID" +
            "  WHERE ac.SECURITYGROUPGUID = ?" +
            ")", securityGroupGuid);
        log.info("Deleted {} rows from ASAUTHPRODUCTTRANSACTION", count5);

        // 6. ASAUTHPRODUCT
        int count6 = jdbc.update(
            "DELETE FROM ASAUTHPRODUCT WHERE AUTHCOMPANYGUID IN (" +
            "  SELECT ac.AUTHCOMPANYGUID FROM ASAUTHCOMPANY ac" +
            "  WHERE ac.SECURITYGROUPGUID = ?" +
            ")", securityGroupGuid);
        log.info("Deleted {} rows from ASAUTHPRODUCT", count6);

        // 7. ASAUTHPLANPAGEBUTTON
        int count7 = jdbc.update(
            "DELETE FROM ASAUTHPLANPAGEBUTTON WHERE AUTHPLANPAGEGUID IN (" +
            "  SELECT app.AUTHPLANPAGEGUID FROM ASAUTHPLANPAGE app" +
            "  JOIN ASAUTHPLAN ap ON app.AUTHPLANGUID = ap.AUTHPLANGUID" +
            "  JOIN ASAUTHCOMPANY ac ON ap.AUTHCOMPANYGUID = ac.AUTHCOMPANYGUID" +
            "  WHERE ac.SECURITYGROUPGUID = ?" +
            ")", securityGroupGuid);
        log.info("Deleted {} rows from ASAUTHPLANPAGEBUTTON", count7);

        // 8. ASAUTHPLANPAGE
        int count8 = jdbc.update(
            "DELETE FROM ASAUTHPLANPAGE WHERE AUTHPLANGUID IN (" +
            "  SELECT ap.AUTHPLANGUID FROM ASAUTHPLAN ap" +
            "  JOIN ASAUTHCOMPANY ac ON ap.AUTHCOMPANYGUID = ac.AUTHCOMPANYGUID" +
            "  WHERE ac.SECURITYGROUPGUID = ?" +
            ")", securityGroupGuid);
        log.info("Deleted {} rows from ASAUTHPLANPAGE", count8);

        // 9. ASAUTHTRANSACTION
        int count9 = jdbc.update(
            "DELETE FROM ASAUTHTRANSACTION WHERE AUTHPLANGUID IN (" +
            "  SELECT ap.AUTHPLANGUID FROM ASAUTHPLAN ap" +
            "  JOIN ASAUTHCOMPANY ac ON ap.AUTHCOMPANYGUID = ac.AUTHCOMPANYGUID" +
            "  WHERE ac.SECURITYGROUPGUID = ?" +
            ")", securityGroupGuid);
        log.info("Deleted {} rows from ASAUTHTRANSACTION", count9);

        // 10. ASAUTHPLANINQUIRY
        int count10 = jdbc.update(
            "DELETE FROM ASAUTHPLANINQUIRY WHERE AUTHPLANGUID IN (" +
            "  SELECT ap.AUTHPLANGUID FROM ASAUTHPLAN ap" +
            "  JOIN ASAUTHCOMPANY ac ON ap.AUTHCOMPANYGUID = ac.AUTHCOMPANYGUID" +
            "  WHERE ac.SECURITYGROUPGUID = ?" +
            ")", securityGroupGuid);
        log.info("Deleted {} rows from ASAUTHPLANINQUIRY", count10);

        // 11. ASAUTHPLAN
        int count11 = jdbc.update(
            "DELETE FROM ASAUTHPLAN WHERE AUTHCOMPANYGUID IN (" +
            "  SELECT ac.AUTHCOMPANYGUID FROM ASAUTHCOMPANY ac" +
            "  WHERE ac.SECURITYGROUPGUID = ?" +
            ")", securityGroupGuid);
        log.info("Deleted {} rows from ASAUTHPLAN", count11);

        // 12. ASAUTHCOMPANYPAGEBUTTON
        int count12 = jdbc.update(
            "DELETE FROM ASAUTHCOMPANYPAGEBUTTON WHERE AUTHCOMPANYPAGEGUID IN (" +
            "  SELECT acp.AUTHCOMPANYPAGEGUID FROM ASAUTHCOMPANYPAGE acp" +
            "  JOIN ASAUTHCOMPANY ac ON acp.AUTHCOMPANYGUID = ac.AUTHCOMPANYGUID" +
            "  WHERE ac.SECURITYGROUPGUID = ?" +
            ")", securityGroupGuid);
        log.info("Deleted {} rows from ASAUTHCOMPANYPAGEBUTTON", count12);

        // 13. ASAUTHCOMPANYPAGE
        int count13 = jdbc.update(
            "DELETE FROM ASAUTHCOMPANYPAGE WHERE AUTHCOMPANYGUID IN (" +
            "  SELECT ac.AUTHCOMPANYGUID FROM ASAUTHCOMPANY ac" +
            "  WHERE ac.SECURITYGROUPGUID = ?" +
            ")", securityGroupGuid);
        log.info("Deleted {} rows from ASAUTHCOMPANYPAGE", count13);

        // 14. ASAUTHCOMPANYINQUIRY
        int count14 = jdbc.update(
            "DELETE FROM ASAUTHCOMPANYINQUIRY WHERE AUTHCOMPANYGUID IN (" +
            "  SELECT ac.AUTHCOMPANYGUID FROM ASAUTHCOMPANY ac" +
            "  WHERE ac.SECURITYGROUPGUID = ?" +
            ")", securityGroupGuid);
        log.info("Deleted {} rows from ASAUTHCOMPANYINQUIRY", count14);

        // 15. ASAUTHCOMPANYWEBSERVICE
        int count15 = jdbc.update(
            "DELETE FROM ASAUTHCOMPANYWEBSERVICE WHERE AUTHCOMPANYGUID IN (" +
            "  SELECT ac.AUTHCOMPANYGUID FROM ASAUTHCOMPANY ac" +
            "  WHERE ac.SECURITYGROUPGUID = ?" +
            ")", securityGroupGuid);
        log.info("Deleted {} rows from ASAUTHCOMPANYWEBSERVICE", count15);

        // 16. ASAUTHCOMPANY
        int count16 = jdbc.update(
            "DELETE FROM ASAUTHCOMPANY WHERE SECURITYGROUPGUID = ?", securityGroupGuid);
        log.info("Deleted {} rows from ASAUTHCOMPANY", count16);

        // 17. ASSECURITYGROUP
        int count17 = jdbc.update(
            "DELETE FROM ASSECURITYGROUP WHERE SECURITYGROUPGUID = ?", securityGroupGuid);
        log.info("Deleted {} rows from ASSECURITYGROUP", count17);

        long duration = System.currentTimeMillis() - startTime;
        log.info("Successfully deleted security group {} (GUID: {}). Total affected child rows: {}. Operation took {} ms.", 
            group.getGROUPNAME(), securityGroupGuid, 
            (count1 + count2 + count3 + count4 + count5 + count6 + count7 + count8 + count9 + count10 + count11 + count12 + count13 + count14 + count15 + count16 + count17),
            duration);
    }
}

