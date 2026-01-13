package com.vansh.manger.Manger.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService {

    // ✅ MUST MATCH FileController's BASE_DIR
    private static final String BASE_DIR = System.getProperty("user.home") + "/manger/uploads/";

    // Define base upload directories
    private final Path schoolLogoRoot = Paths.get(BASE_DIR, "logos");
    private final Path studentProfileRoot = Paths.get(BASE_DIR, "students");
    private final Path teacherProfileRoot = Paths.get(BASE_DIR, "teachers");

    public FileStorageService() {
        try {
            Files.createDirectories(schoolLogoRoot);
            Files.createDirectories(studentProfileRoot);
            Files.createDirectories(teacherProfileRoot);

        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directories!", e);
        }
    }

    /**
     * Generic file saving method
     * Returns ONLY the filename (not the full path) to be stored in the database
     */
    private String saveFile(MultipartFile file, Path subDirectory, String identifier) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Sanitize identifier
        String sanitizedIdentifier = identifier.replaceAll("[^a-zA-Z0-9-]", "");

        // Get file extension
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Create a unique filename with timestamp to avoid conflicts
        String fileName = sanitizedIdentifier + "-" + System.currentTimeMillis() + extension;
        Path destinationFile = subDirectory.resolve(fileName);

        // Save the file (overwrite if exists)
        Files.copy(file.getInputStream(), destinationFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);



        return fileName;
    }

    public String saveSchoolLogo(MultipartFile file, Long schoolId) throws IOException {
        return saveFile(file, schoolLogoRoot, "school-" + schoolId);
    }

    public String saveStudentProfile(MultipartFile file, String identifier) throws IOException {
        return saveFile(file, studentProfileRoot, identifier);
    }

    public String saveTeacherProfile(MultipartFile file, String identifier) throws IOException {
        return saveFile(file, teacherProfileRoot, identifier);
    }

    /**
     * Delete a file from storage
     */
    public void deleteFile(Path subDirectory, String filename) {
        if (filename == null || filename.isEmpty()) {
            return;
        }

        try {
            Path filePath = subDirectory.resolve(filename);
            Files.deleteIfExists(filePath);

        } catch (IOException e) {
            System.err.println("⚠️ Failed to delete file: " + e.getMessage());
        }
    }

    public void deleteStudentProfile(String filename) {
        deleteFile(studentProfileRoot, filename);
    }

    public void deleteTeacherProfile(String filename) {
        deleteFile(teacherProfileRoot, filename);
    }

    public void deleteSchoolLogo(String filename) {
        deleteFile(schoolLogoRoot, filename);
    }
}