package com.chunglun.sre.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
public class NewmanRestfulService {
    @Value("${upload.folder}")
    private String uploadFolder;

    @Value("${report.folder}")
    private String reportFolder;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        if (file.isEmpty()) {
            model.addAttribute("error", "Please select a file to upload");
            return "index";
        }

        String uid = UUID.randomUUID().toString();
        String filename = uid + ".json";
        File uploadPath = new File(uploadFolder);
        if (!uploadPath.exists()) uploadPath.mkdirs();
        File jsonFile = new File(uploadPath, filename);
        file.transferTo(jsonFile);

        String reportFilename = uid + ".html";
        File reportPath = new File(reportFolder);
        if (!reportPath.exists()) reportPath.mkdirs();
        File reportFile = new File(reportPath, reportFilename);

        ProcessBuilder pb = new ProcessBuilder("newman", "run", jsonFile.getAbsolutePath(),
                "--reporters", "html", "--reporter-html-export", reportFile.getAbsolutePath());
        try {
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                model.addAttribute("error", "Newman execution failed");
                return "index";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error running Newman: " + e.getMessage());
            return "index";
        }

        model.addAttribute("report_url", "/reports/" + reportFilename);
        return "index";
    }

    @GetMapping("/reports/{filename:.+}")
    public ResponseEntity<Resource> getReport(@PathVariable String filename) throws MalformedURLException {
        Path file = Paths.get(reportFolder).resolve(filename);
        Resource resource = new UrlResource(file.toUri());
        if (resource.exists() && resource.isReadable()) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "text/html")
                    .body(resource);
        }
        return ResponseEntity.notFound().build();
    }
}
