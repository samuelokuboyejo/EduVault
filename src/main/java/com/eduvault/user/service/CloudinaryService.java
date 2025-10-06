package com.eduvault.user.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.eduvault.config.CloudinaryConfiguration;
import com.eduvault.user.utils.UploadResponse;
import org.cloudinary.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

@Service
public class CloudinaryService {
    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);

    private final Cloudinary cloudinaryClient;

    @Autowired
    public CloudinaryService(final CloudinaryConfiguration configuration) {
        this.cloudinaryClient =
                new Cloudinary(
                        String.format(
                                "cloudinary://%s:%s@%s",
                                configuration.getApiKey(),
                                configuration.getApiSecret(),
                                configuration.getCloudName()));
    }

    public UploadResponse upload(final MultipartFile file) throws IOException {
        Map uploadResult = this.cloudinaryClient.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "raw",
                        "folder", "receipts",
                        "use_filename", true,
                        "unique_filename", true,
                        "type", "upload",
                        "access_mode", "public"
                )
        );

        log.debug("cloudinary pdf upload response: [{}]", uploadResult);
        JSONObject json = new JSONObject(uploadResult);

        String publicId = json.getString("public_id");
        String cloudName = this.cloudinaryClient.config.cloudName;
        String pdfUrl = "https://res.cloudinary.com/" + cloudName + "/raw/upload/" + publicId + ".pdf";

        return new UploadResponse(
                pdfUrl,
                json.getString("secure_url"),
                json.optString("format", "pdf"),
                json.optInt("width", 0),
                json.optInt("height", 0),
                json.getInt("bytes"),
                json.getString("original_filename"),
                json.getString("created_at")
        );
    }

    private File convertMultiPartToFile(final MultipartFile file) throws IOException {
        final File newFile = Files.createTempFile("temp", file.getOriginalFilename()).toFile();
        file.transferTo(newFile);
        return newFile;
    }
}
