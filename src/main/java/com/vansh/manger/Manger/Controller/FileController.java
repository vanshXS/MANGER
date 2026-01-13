package com.vansh.manger.Manger.Controller;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")

public class FileController {

    private static final String BASE_DIR = System.getProperty("user.home") + "/manger/uploads/";

    @GetMapping("/students/{filename:.+}")
    public ResponseEntity<Resource> getStudentImage(@PathVariable String filename) {
        return serveFile("students", filename);
    }

    @GetMapping("/teachers/{filename:.+}")
    public ResponseEntity<Resource> getTeacherImage(@PathVariable String filename) {
        return serveFile("teachers", filename);
    }

    @GetMapping("/logos/{filename:.+}")
    public ResponseEntity<Resource> getSchoolLogo(@PathVariable String filename) {
        return serveFile("logos", filename);
    }

    /**
     * Generic file serving method
     */
    private ResponseEntity<Resource> serveFile(String directory, String filename) {
        try {
            System.out.println("=== FILE REQUEST ===");
            System.out.println("📁 Directory: " + directory);
            System.out.println("📄 Filename: " + filename);

            Path filePath = Paths.get(BASE_DIR, directory, filename);
            System.out.println("🔍 Full path: " + filePath.toAbsolutePath());

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                System.err.println("❌ File not found or not readable: " + filePath);

                // List files in directory for debugging
                File dir = Paths.get(BASE_DIR, directory).toFile();
                if (dir.exists() && dir.isDirectory()) {
                    System.out.println("📋 Available files in " + directory + ":");
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            System.out.println("   - " + f.getName());
                        }
                    }
                }

                return ResponseEntity.notFound().build();
            }

            String contentType = determineContentType(filename);
            System.out.println("✅ Serving file with content type: " + contentType);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                    .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                    .body(resource);

        } catch (Exception e) {
            System.err.println("❌ Error serving file: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String determineContentType(String filename) {
        String lowerFilename = filename.toLowerCase();

        if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowerFilename.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFilename.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerFilename.endsWith(".svg")) {
            return "image/svg+xml";
        }

        return "application/octet-stream";
    }

    @PostConstruct
    public void init() {
        try {
            Path studentsPath = Paths.get(BASE_DIR, "students");
            Path teachersPath = Paths.get(BASE_DIR, "teachers");
            Path logosPath = Paths.get(BASE_DIR, "logos");

            Files.createDirectories(studentsPath);
            Files.createDirectories(teachersPath);
            Files.createDirectories(logosPath);

            System.out.println("✅ Upload directories verified:");
            System.out.println("   Students: " + studentsPath.toAbsolutePath());
            System.out.println("   Teachers: " + teachersPath.toAbsolutePath());
            System.out.println("   Logos: " + logosPath.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("❌ Failed to create upload directories: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    public String test() {
        return "File controller is working!";
    }

    // Debug endpoint
    @GetMapping("/debug/{directory}")
    public ResponseEntity<String> debugDirectory(@PathVariable String directory) {
        File dir = Paths.get(BASE_DIR, directory).toFile();
        StringBuilder info = new StringBuilder();

        info.append("Directory: ").append(Paths.get(BASE_DIR, directory).toAbsolutePath()).append("\n");
        info.append("Exists: ").append(dir.exists()).append("\n");
        info.append("Is Directory: ").append(dir.isDirectory()).append("\n");

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            info.append("\nFiles found: ").append(files != null ? files.length : 0).append("\n");
            if (files != null) {
                for (File f : files) {
                    info.append("  - ").append(f.getName())
                            .append(" (").append(f.length()).append(" bytes)\n");
                }
            }
        }

        return ResponseEntity.ok(info.toString());
    }
}