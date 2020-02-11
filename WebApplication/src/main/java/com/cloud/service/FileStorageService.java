package com.cloud.service;

import com.cloud.entity.Bill;
import com.cloud.entity.FileUpload;
import com.cloud.repository.FileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


@Service
public class FileStorageService {

    @Autowired
    private FileRepository fileRepository;

    public FileUpload storeFile(MultipartFile file, String actualPath, Bill userBill) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        //getting metadata information
        Path path = Paths.get(actualPath);
        BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String creationTime =  df.format(attr.creationTime().toMillis());
        String lastAccessTime = df.format(attr.lastAccessTime().toMillis());
        String lastModifiedTime =  df.format(attr.lastModifiedTime().toMillis());
        Long size = attr.size();
        String md5Hex = DigestUtils.md5Hex(fileName);
        String contentType = Files.probeContentType(path);
        FileUpload newFileUpload = new FileUpload(fileName, actualPath, new Date(),creationTime,lastAccessTime,lastModifiedTime,size,contentType,md5Hex,userBill);
        return fileRepository.save(newFileUpload);

    }

}
