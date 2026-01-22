package com.edutech.logisticsmanagementandtrackingsystem.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {


 private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }


    /**
     * Upload Base64 image to Cloudinary
     * @param base64Data - Base64 encoded string
     * @param folder - Folder name in Cloudinary (e.g., "driver-licenses")
     * @return Cloudinary URL of uploaded image
     */
    public String uploadBase64Image(String base64Data, String folder) {
        try {
            // Remove Base64 header if present
            String base64Image = base64Data;
            if (base64Data.contains(",")) {
                base64Image = base64Data.split(",")[1];
            }

            // Decode Base64 to bytes
            byte[] decodedBytes = Base64.getDecoder().decode(base64Image);

            // Generate unique public ID
            String publicId = folder + "/" + UUID.randomUUID().toString();

            // Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(decodedBytes, ObjectUtils.asMap(
                "public_id", publicId,
                "folder", folder,
                "resource_type", "auto", // Automatically detect image/pdf
                "quality", "auto", // Auto optimize quality
                "fetch_format", "auto" // Auto format conversion
            ));

            // Return secure URL
            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Delete image from Cloudinary
     * @param imageUrl - Full Cloudinary URL
     */
    public void deleteImage(String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.isEmpty()) {
                return;
            }

            // Extract public_id from URL
            // URL format: https://res.cloudinary.com/{cloud_name}/image/upload/{public_id}.jpg
            String[] urlParts = imageUrl.split("/upload/");
            if (urlParts.length < 2) {
                return;
            }

            String publicIdWithExtension = urlParts[1];
            // Remove file extension
            String publicId = publicIdWithExtension.substring(0, publicIdWithExtension.lastIndexOf('.'));

            // Delete from Cloudinary
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

        } catch (Exception e) {
            // Log but don't throw - deletion failure shouldn't break the flow
            System.err.println("Failed to delete image from Cloudinary: " + e.getMessage());
        }
    }

    /**
     * Upload with custom options
     */
    public String uploadWithOptions(String base64Data, String folder, Map<String, Object> options) {
        try {
            String base64Image = base64Data;
            if (base64Data.contains(",")) {
                base64Image = base64Data.split(",")[1];
            }

            byte[] decodedBytes = Base64.getDecoder().decode(base64Image);

            String publicId = folder + "/" + UUID.randomUUID().toString();

            // Merge custom options with defaults
            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", folder,
                "resource_type", "auto"
            );
            uploadOptions.putAll(options);

            Map uploadResult = cloudinary.uploader().upload(decodedBytes, uploadOptions);

            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Cloudinary: " + e.getMessage(), e);
        }
    }
}