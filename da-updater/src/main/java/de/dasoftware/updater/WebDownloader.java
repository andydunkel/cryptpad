package de.dasoftware.updater;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP downloader for update files and XML data
 */
public class WebDownloader {
    
    private static final int TIMEOUT_SECONDS = 3;
    
    private final String url;
    private final HttpClient httpClient;
    
    /**
     * Constructor
     * 
     * @param url URL to download from
     */
    public WebDownloader(String url) {
        this.url = url;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }
    
    /**
     * Downloads a text file (XML) from the URL
     * 
     * @return Content as string, or empty string on error
     * @throws IOException If download fails
     */
    public String downloadAsText() throws IOException {
        try {
            // Create temp file
            Path tempFile = Files.createTempFile("update_", ".xml");
            
            // Download to temp file
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .GET()
                    .build();
            
            HttpResponse<Path> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofFile(tempFile));
            
            if (response.statusCode() != 200) {
                throw new IOException("HTTP error code: " + response.statusCode());
            }
            
            // Read content
            String content = Files.readString(tempFile, StandardCharsets.UTF_8);
            
            // Delete temp file
            try {
                Thread.sleep(100); // Small delay like in C# version
                Files.deleteIfExists(tempFile);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            return content;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Download interrupted", e);
        }
    }
    
    /**
     * Downloads a file asynchronously to temp directory
     * 
     * @return Path to downloaded file in temp directory
     * @throws IOException If download fails
     */
    public String downloadFile() throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        Path targetFile = Paths.get(tempDir, UpdateLogic.FILENAME);
        
        // Delete existing file
        Files.deleteIfExists(targetFile);
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .GET()
                    .build();
            
            HttpResponse<Path> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofFile(targetFile));
            
            if (response.statusCode() != 200) {
                throw new IOException("HTTP error code: " + response.statusCode());
            }
            
            return targetFile.toString();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Download interrupted", e);
        }
    }
    
    /**
     * Downloads a file asynchronously (non-blocking)
     * 
     * @return CompletableFuture with path to downloaded file
     */
    public CompletableFuture<String> downloadFileAsync() {
        String tempDir = System.getProperty("java.io.tmpdir");
        Path targetFile = Paths.get(tempDir, UpdateLogic.FILENAME);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Delete existing file
                Files.deleteIfExists(targetFile);
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                        .GET()
                        .build();
                
                HttpResponse<Path> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofFile(targetFile));
                
                if (response.statusCode() != 200) {
                    throw new IOException("HTTP error code: " + response.statusCode());
                }
                
                return targetFile.toString();
                
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Download failed", e);
            }
        });
    }
    
    /**
     * Gets the HTTP client instance
     * 
     * @return HttpClient instance
     */
    public HttpClient getClient() {
        return httpClient;
    }
}