package com.chunglun.sre.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/")
public class NewmanRestfulService {
    private static final Logger logger = LoggerFactory.getLogger(NewmanRestfulService.class);

    @Value("${upload.folder}")
    private String uploadFolder;

    @Value("${report.folder}")
    private String reportFolder;
    @PostMapping("/executeTest")
    public ResponseEntity<?> executeTest(@RequestParam("collection") MultipartFile collectionFile,
                                         @RequestParam(value = "environment", required = false) MultipartFile environmentFile) {
        if (collectionFile.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Please select a file to upload"));
        }

        String uid = UUID.randomUUID().toString();
        String collectionFilename = uid + ".json";
        String environmentFilename = uid + "-env.json";

        try {
            File uploadPath = new File(uploadFolder);
            if (!uploadPath.exists()) {
                uploadPath.mkdirs();
            }
            // Save collection file
            File jsonFile = new File(uploadPath, collectionFilename);
            collectionFile.transferTo(jsonFile);
            // Optional: Save environment file
            File envFile = null;
            if (environmentFile != null && !environmentFile.isEmpty()) {
                envFile = new File(uploadPath, environmentFilename);
                environmentFile.transferTo(envFile);
            }
            // Prepare report file
            String reportFilename = uid + ".html";
            File reportPath = new File(reportFolder);
            if (!reportPath.exists()) {
                reportPath.mkdirs();
            }
            File reportFile = new File(reportPath, reportFilename);
            // Build Newman command
            ProcessBuilder pb = new ProcessBuilder(
                    "newman", "run", jsonFile.getAbsolutePath(),
                    "--reporters", "html", "--reporter-html-export", reportFile.getAbsolutePath());
            if (envFile != null) {
                pb.command().add("--environment");
                pb.command().add(envFile.getAbsolutePath());
            }
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Newman execution failed"));
            }
            Map<String, Object> response = new HashMap<>();
            response.put("report_url", "/v1/api/report/" + reportFilename);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("NewmanRestfulService.executeTest failed: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error running Newman: " + e.getMessage()));
        }
    }

    @GetMapping("/report/{filename:.+}")
    public ResponseEntity<Resource> getReport(@PathVariable String filename) throws MalformedURLException {
        try {
            if (!filename.endsWith(".html")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            Path file = Paths.get(reportFolder).resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .contentType(MediaType.TEXT_HTML) // or auto-detect if needed
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            logger.error("NewmanRestfulService.getReport failed: ", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
