package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();

        // Step 1: Register
        String registerUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        Map<String, String> body = new HashMap<>();
        body.put("name", "P. Naman Kumar");       
        body.put("regNo", "22BDS0349");      
        body.put("email", "namankumarcollege@gmail.com"); 

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(registerUrl, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonResponse = mapper.readTree(response.getBody());
            String webhookUrl = jsonResponse.get("webhook").asText();
            String accessToken = jsonResponse.get("accessToken").asText();

            // Step 2: Your SQL query
            String finalQuery = "SELECT " +
                    "P.AMOUNT AS SALARY, " +
                    "CONCAT(E.FIRST_NAME, ' ', E.LAST_NAME) AS NAME, " +
                    "FLOOR(DATEDIFF(CURDATE(), E.DOB) / 365) AS AGE, " +
                    "D.DEPARTMENT_NAME " +
                    "FROM PAYMENTS P " +
                    "JOIN EMPLOYEE E ON P.EMP_ID = E.EMP_ID " +
                    "JOIN DEPARTMENT D ON E.DEPARTMENT = D.DEPARTMENT_ID " +
                    "WHERE DAY(P.PAYMENT_TIME) <> 1 " +
                    "ORDER BY P.AMOUNT DESC " +
                    "LIMIT 1;";

            Map<String, String> solution = new HashMap<>();
            solution.put("finalQuery", finalQuery);

            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setContentType(MediaType.APPLICATION_JSON);
            authHeaders.set("Authorization", accessToken);

            HttpEntity<Map<String, String>> submissionRequest = new HttpEntity<>(solution, authHeaders);
            ResponseEntity<String> submitResponse =
                    restTemplate.postForEntity(webhookUrl, submissionRequest, String.class);

            System.out.println("✅ Submission Response: " + submitResponse.getBody());
        } else {
            System.out.println("❌ Failed to generate webhook. Status: " + response.getStatusCode());
        }
    }
}
