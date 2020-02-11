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
    private String creationTime;
    @Column
    @JsonIgnore
    private String lastAccessTime;
    @Column
    @JsonIgnore
    private String lastModifiedTime;
    @Column
    @JsonIgnore
    private Long size;
    @Column
    @JsonIgnore
    private String contentType;
    @Column
    @JsonIgnore
    private String md5Hex;
    @OneToOne(cascade = CascadeType.ALL)//one-to-one
    @JoinColumn(name = "bill_id")
    @JsonIgnore
    private Bill bill;

    public FileUpload() {
    }

    public FileUpload(String fileName, String url, Date uploadDate, String creationTime, String lastAccessTime, String lastModifiedTime, Long size, String contentType, String md5Hex, Bill bill) {
        this.fileName = fileName;
        this.url = url;
        this.uploadeDate = uploadDate;
        this.creationTime = creationTime;
        this.lastAccessTime = lastAccessTime;
        this.lastModifiedTime = lastModifiedTime;
        this.size = size;
        this.contentType = contentType;
        this.md5Hex = md5Hex;
        this.bill = bill;
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

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(String lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public String getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(String lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
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

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }
}
