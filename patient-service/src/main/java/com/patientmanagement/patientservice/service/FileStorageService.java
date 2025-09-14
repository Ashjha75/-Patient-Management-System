package com.patientmanagement.patientservice.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Interface for a file storage service, abstracting the underlying storage mechanism (e.g., AWS S3).
 */
public interface FileStorageService {

    /**
     * Uploads a file to a specified folder within the storage.
     *
     * @param file   The file to upload, represented as a MultipartFile.
     * @param folder The destination folder or path within the storage.
     * @return The public URL or unique identifier of the uploaded file.
     */
    String upload(MultipartFile file, String folder);
}
