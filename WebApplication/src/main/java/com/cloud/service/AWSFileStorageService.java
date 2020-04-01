package com.cloud.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class AWSFileStorageService {
    private AmazonS3 s3client;

    private Path fileStorageLocation;

    @Value("${endpointUrl}")
    private String endpointUrl;
    @Value("${bucketName}")
    private String bucketName;
    private String updateFileName;

    @PostConstruct
    private void initializeAmazon() {
        this.s3client = AmazonS3ClientBuilder.standard().withCredentials(new InstanceProfileCredentialsProvider(false))
                .build();
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    private String generateFileName(MultipartFile multiPart) {
        return FilenameUtils.getBaseName(multiPart.getOriginalFilename());
    }

    private void uploadFileTos3bucket(String fileName, File file) {
        try {
            s3client.putObject(new PutObjectRequest(bucketName, fileName, file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean deleteFile(String fileUrl) {
        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            s3client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            return false;
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            return false;
        }
        return true;
    }

    public String storeFile(MultipartFile multipartFile) {
        String fileUrl = "";
        try {
            File file = convertMultiPartToFile(multipartFile);
            String prefix = "http:\\/\\/";
            String uniqueID = UUID.randomUUID().toString();
            String fileName = generateFileName(multipartFile) + "_" + uniqueID + "." + FilenameUtils.getExtension(multipartFile.getOriginalFilename());
            this.updateFileName = fileName;
            fileStorageLocation = Paths.get(prefix + bucketName + ".s3.amazonaws.com");
            fileUrl = fileStorageLocation + "/" + fileName;
            uploadFileTos3bucket(fileName, file);
            //file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileUrl;
    }

    public Path getFileStorageLocation() {
        return fileStorageLocation;
    }

    public String getUpdatedFileName() {
        return this.updateFileName;
    }
}
