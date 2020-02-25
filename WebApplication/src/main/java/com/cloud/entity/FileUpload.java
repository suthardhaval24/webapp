package com.cloud.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
public class FileUpload {
    @Id
    @GeneratedValue
    @Type(type = "uuid-char")
    @Column(name = "file_id")
    private UUID id;
    @Column
    private String fileName;
    @Column
    private String url;
    @Column
    private Date uploadeDate;
    @Column
    @JsonIgnore
    private Long size;
    @Column
    @JsonIgnore
    private String contentType;
    @Column
    @JsonIgnore
    private String md5Hex;

    public FileUpload() {
    }

    public FileUpload(String fileName, String url, Date uploadDate, Long size, String contentType, String md5Hex) {
        //setting id manually to avoid on fly creation of file
        //this.id = id;
        this.fileName = fileName;
        this.url = url;
        this.uploadeDate = uploadDate;
        this.size = size;
        this.contentType = contentType;
        this.md5Hex = md5Hex;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getUploadeDate() {
        return uploadeDate;
    }

    public void setUploadeDate(Date uploadeDate) {
        this.uploadeDate = uploadeDate;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getMd5Hex() {
        return md5Hex;
    }

    public void setMd5Hex(String md5Hex) {
        this.md5Hex = md5Hex;
    }
}
