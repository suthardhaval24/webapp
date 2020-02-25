package com.cloud.service;

import com.cloud.entity.FileUpload;
import com.cloud.repository.FileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;


@Service
public class FileStorageService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    AWSFileStorageService awsFileStorageService;

    public FileUpload storeFile(MultipartFile file, String actualPath) throws IOException {
        String fileName = awsFileStorageService.getUpdatedFileName();
        //getting metadata information
        Long size = file.getSize();
        String md5Hex = DigestUtils.md5Hex(fileName);
        String contentType = file.getContentType();
        FileUpload newFileUpload = new FileUpload(fileName, actualPath, new Date(), size, contentType, md5Hex);
        System.out.println("hello --end");
        return fileRepository.save(newFileUpload);
    }

}
