package com.example.mysqlspringboot.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    // Directory to store uploaded images
    private static final String IMAGE_DIR = "C:/Users/DELL/Documents/GitHub/spring-boot/UploadedImages/";

    // POST: Upload an image
    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // Ensure the directory exists
            Path uploadDir = Paths.get(IMAGE_DIR);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Save the file to the directory
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            Path filePath = uploadDir.resolve(fileName);
            file.transferTo(filePath.toFile());

            // Return the file download URL
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/images/")
                    .path(fileName)
                    .toUriString();

            return ResponseEntity.ok(fileUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Image upload failed: " + e.getMessage());
        }
    }

    // GET: Serve an image
    @GetMapping("/{fileName}")
    public ResponseEntity<byte[]> getImage(@PathVariable String fileName) {
        try {
            // Load the image from the directory
            Path filePath = Paths.get(IMAGE_DIR).resolve(fileName);
            File imgFile = filePath.toFile();

            if (!imgFile.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null);
            }

            // Read the image file as a byte array
            byte[] imageBytes = Files.readAllBytes(filePath);

            // Return the image as a response
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, Files.probeContentType(filePath));

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // DELETE: Remove an image
    @DeleteMapping("/{fileName}")
    public ResponseEntity<String> deleteImage(@PathVariable String fileName) {
        try {
            // Resolve the file path
            Path filePath = Paths.get(IMAGE_DIR).resolve(fileName);
            File imgFile = filePath.toFile();

            if (!imgFile.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Image not found.");
            }

            // Delete the file
            if (imgFile.delete()) {
                return ResponseEntity.ok("Image deleted successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to delete the image.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred while deleting image: " + e.getMessage());
        }
    }
}
