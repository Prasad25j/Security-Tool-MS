package com.example.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class PasProxyController {

    private final HttpClient httpClient = HttpClient.newBuilder().build();

    @Value("${pas.service.url}")
    private String pasServiceUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    @Qualifier("secondaryDevDataSource")
    private DataSource dataSource;

    @Autowired
    @Qualifier("secondaryDevTransactionManager")
    private PlatformTransactionManager transactionManager;

    @RequestMapping(value = "/api/PASService/**", method = {
            RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
            RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS
    })
    public ResponseEntity<byte[]> proxyPas(
            @RequestBody(required = false) byte[] body,
            HttpServletRequest request) {

        try {
            // Construct target URL
            String path = request.getRequestURI(); // e.g. /api/PASService/rest/services/v1/users/...
            String targetPath = path.substring("/api".length()); // e.g. /PASService/rest/services/v1/users/...
            String queryString = request.getQueryString();
            String targetUrl = pasServiceUrl + targetPath + (queryString != null ? "?" + queryString : "");
            URI uri = new URI(targetUrl);

            // Build HttpRequest
            String method = request.getMethod();

            // Special Handling for PATCH on /PASService/rest/services/v1/users/{loginName}
            if ("PATCH".equalsIgnoreCase(method) && path.matches("/api/PASService/rest/services/v1/users/[^/]+")) {
                if (body != null && body.length > 0) {
                    try {
                        JsonNode rootNode = objectMapper.readTree(body);
                        JsonNode userNode = rootNode.path("user");
                        if (!userNode.isMissingNode() && userNode.hasNonNull("password")
                                && !userNode.path("password").asText().trim().isEmpty()) {
                            // If a new password is provided, convert method to PUT and proxy to OIPA JAX-RS
                            // (which will hash the password)
                            method = "PUT";
                        } else {
                            // No new password -> Update database directly to avoid OIPA's 405 error /
                            // password requirement
                            return handleDbUserUpdate(path, body);
                        }
                    } catch (Exception parseEx) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(("Invalid JSON body: " + parseEx.getMessage()).getBytes());
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Request body is required".getBytes());
                }
            }

            HttpRequest.BodyPublisher bodyPublisher;
            if (body != null && body.length > 0) {
                bodyPublisher = HttpRequest.BodyPublishers.ofByteArray(body);
            } else {
                bodyPublisher = HttpRequest.BodyPublishers.noBody();
            }

            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                    .uri(uri)
                    .method(method, bodyPublisher);

            // Copy headers (excluding restricted ones like Origin, Host, Connection,
            // Content-Length, etc.)
            java.util.Set<String> restrictedHeaders = java.util.Set.of(
                    "origin", "host", "connection", "content-length",
                    "date", "expect", "from", "upgrade", "via", "warning",
                    "keep-alive", "proxy-connection", "transfer-encoding");
            Collections.list(request.getHeaderNames()).forEach(headerName -> {
                String lowerName = headerName.toLowerCase();
                if (!restrictedHeaders.contains(lowerName)) {
                    Collections.list(request.getHeaders(headerName)).forEach(headerVal -> {
                        reqBuilder.header(headerName, headerVal);
                    });
                }
            });

            HttpRequest targetRequest = reqBuilder.build();

            // Send request
            HttpResponse<byte[]> response = httpClient.send(targetRequest, HttpResponse.BodyHandlers.ofByteArray());

            // Build ResponseEntity
            HttpHeaders responseHeaders = new HttpHeaders();
            response.headers().map().forEach((k, v) -> {
                if (!k.equalsIgnoreCase("transfer-encoding") && !k.equalsIgnoreCase("content-length")) {
                    v.forEach(val -> responseHeaders.add(k, val));
                }
            });

            return new ResponseEntity<>(response.body(), responseHeaders, HttpStatus.valueOf(response.statusCode()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("PAS Proxy error: " + e.getMessage()).getBytes());
        }
    }

    private ResponseEntity<byte[]> handleDbUserUpdate(String path, byte[] body) {
        String originalLoginName = path.substring(path.lastIndexOf('/') + 1);

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("UserUpdateTx");
        def.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);

            // 1. Parse request body
            JsonNode rootNode = objectMapper.readTree(body);
            JsonNode userNode = rootNode.path("user");
            if (userNode.isMissingNode()) {
                transactionManager.rollback(status);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Missing 'user' field in request".getBytes());
            }

            // Extract fields
            String newLoginName = userNode.path("loginName").asText(originalLoginName).trim();
            String userStatus = userNode.path("userStatus").asText(null);
            String localeCode = userNode.path("localeCode").asText(null);

            // Fetch CLIENTGUID for the originalLoginName
            java.util.List<String> guids = jdbc.queryForList(
                    "SELECT CLIENTGUID FROM ASUSER WHERE CLIENTNUMBER = ?",
                    new Object[] { originalLoginName },
                    String.class);

            if (guids.isEmpty()) {
                transactionManager.rollback(status);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(("User not found: " + originalLoginName).getBytes());
            }
            String clientGuid = guids.get(0);

            // Check if loginName has changed and if the new loginName already exists
            if (!originalLoginName.equalsIgnoreCase(newLoginName)) {
                int count = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM ASUSER WHERE CLIENTNUMBER = ? AND CLIENTGUID != ?",
                        new Object[] { newLoginName, clientGuid },
                        Integer.class);
                if (count > 0) {
                    transactionManager.rollback(status);
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(("Login name already exists: " + newLoginName).getBytes());
                }
            }

            // 2. Update ASUSER
            jdbc.update(
                    "UPDATE ASUSER SET CLIENTNUMBER = ?, LOCALECODE = ?, USERSTATUS = ? WHERE CLIENTGUID = ?",
                    newLoginName, localeCode, userStatus, clientGuid);

            // 3. Update ASCLIENT details
            JsonNode clientNode = userNode.path("client");
            if (!clientNode.isMissingNode()) {
                String firstName = clientNode.path("firstName").asText(null);
                String lastName = clientNode.path("lastName").asText(null);
                String sex = clientNode.path("gender").asText(null);
                if (sex == null || sex.trim().isEmpty() || "null".equalsIgnoreCase(sex)) {
                    sex = clientNode.path("sex").asText(null);
                }
                if (sex != null) {
                    sex = sex.trim();
                    if ("null".equalsIgnoreCase(sex) || sex.isEmpty()) {
                        sex = null;
                    }
                }
                String email = clientNode.path("email").asText(null);
                String companyName = clientNode.path("primaryCompany").asText(null);
                if (companyName == null || companyName.trim().isEmpty()) {
                    companyName = clientNode.path("companyName").asText(null);
                }

                jdbc.update(
                        "UPDATE ASCLIENT SET FIRSTNAME = ?, LASTNAME = ?, SEX = ?, EMAIL = ?, COMPANYNAME = ?, UPDATEDGMT = SYSDATE WHERE CLIENTGUID = ?",
                        firstName, lastName, sex, email, companyName, clientGuid);
            }

            // 4. Synchronize security groups mapping
            JsonNode secGroupNode = userNode.path("securityGroup");
            if (secGroupNode.isArray()) {
                // Delete existing mapping
                jdbc.update("DELETE FROM ASUSERSECURITYGROUP WHERE CLIENTGUID = ?", clientGuid);

                // Insert new mappings
                for (JsonNode group : secGroupNode) {
                    String secGroupGuid = group.path("securityGroupGuid").asText(null);
                    String groupName = group.path("securityGroupName").asText(null);
                    String roleEffectiveFromStr = group.path("roleEffectiveFrom").asText(null);
                    String roleEffectiveToStr = group.path("roleEffectiveTo").asText(null);

                    // Look up securityGroupGuid by name if not provided (newly added groups)
                    if (secGroupGuid == null || secGroupGuid.trim().isEmpty()) {
                        if (groupName != null && !groupName.trim().isEmpty()) {
                            java.util.List<String> foundGuids = jdbc.queryForList(
                                    "SELECT SECURITYGROUPGUID FROM ASSECURITYGROUP WHERE GROUPNAME = ?",
                                    new Object[] { groupName.trim() },
                                    String.class);
                            if (!foundGuids.isEmpty()) {
                                secGroupGuid = foundGuids.get(0);
                            }
                        }
                    }

                    if (secGroupGuid != null && !secGroupGuid.trim().isEmpty()) {
                        java.sql.Timestamp fromTime = null;
                        if (roleEffectiveFromStr != null && !roleEffectiveFromStr.trim().isEmpty()) {
                            try {
                                java.time.Instant instant = java.time.Instant.parse(roleEffectiveFromStr);
                                fromTime = java.sql.Timestamp.from(instant);
                            } catch (Exception ex) {
                                try {
                                    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(roleEffectiveFromStr);
                                    fromTime = java.sql.Timestamp.valueOf(ldt);
                                } catch (Exception ex2) {
                                    try {
                                        java.time.LocalDate ld = java.time.LocalDate.parse(roleEffectiveFromStr);
                                        fromTime = java.sql.Timestamp.valueOf(ld.atStartOfDay());
                                    } catch (Exception ex3) {
                                        fromTime = new java.sql.Timestamp(System.currentTimeMillis());
                                    }
                                }
                            }
                        } else {
                            fromTime = new java.sql.Timestamp(System.currentTimeMillis());
                        }

                        java.sql.Timestamp toTime = null;
                        if (roleEffectiveToStr != null && !roleEffectiveToStr.trim().isEmpty()) {
                            try {
                                java.time.Instant instant = java.time.Instant.parse(roleEffectiveToStr);
                                toTime = java.sql.Timestamp.from(instant);
                            } catch (Exception ex) {
                                try {
                                    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(roleEffectiveToStr);
                                    toTime = java.sql.Timestamp.valueOf(ldt);
                                } catch (Exception ex2) {
                                    try {
                                        java.time.LocalDate ld = java.time.LocalDate.parse(roleEffectiveToStr);
                                        toTime = java.sql.Timestamp.valueOf(ld.atStartOfDay());
                                    } catch (Exception ex3) {
                                        // Ignore parsing failure, keep toTime null
                                    }
                                }
                            }
                        }

                        jdbc.update(
                                "INSERT INTO ASUSERSECURITYGROUP (SECURITYGROUPGUID, CLIENTGUID, ROLEEFFECTIVEFROM, ROLEEFFECTIVETO) VALUES (?, ?, ?, ?)",
                                secGroupGuid, clientGuid, fromTime, toTime);
                    }
                }
            }

            transactionManager.commit(status);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

        } catch (Exception e) {
            transactionManager.rollback(status);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("DB User Update Error: " + e.getMessage()).getBytes());
        }
    }
}
