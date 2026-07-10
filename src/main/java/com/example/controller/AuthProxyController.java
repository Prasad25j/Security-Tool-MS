package com.example.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class AuthProxyController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @RequestMapping(value = "/api/ivs/**", method = {
            RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
            RequestMethod.DELETE, RequestMethod.OPTIONS
    })
    public ResponseEntity<byte[]> proxyAuth(
            @RequestBody(required = false) byte[] body,
            HttpMethod method,
            HttpServletRequest request) throws URISyntaxException {

        // Construct target URI
        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        String targetUrl = authServiceUrl + path + (queryString != null ? "?" + queryString : "");
        URI uri = new URI(targetUrl);

        // Copy incoming headers
        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames()).forEach(headerName -> {
            Collections.list(request.getHeaders(headerName)).forEach(headerVal -> {
                headers.add(headerName, headerVal);
            });
        });

        // Create entity with body and headers
        HttpEntity<byte[]> requestEntity = new HttpEntity<>(body, headers);

        try {
            // Forward HTTP request
            return restTemplate.exchange(uri, method, requestEntity, byte[].class);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            // Return identical backend error status and body to UI
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Auth Proxy error: " + e.getMessage()).getBytes());
        }
    }
}
