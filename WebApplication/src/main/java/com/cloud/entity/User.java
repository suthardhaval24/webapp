package com.cloud.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;
import java.util.UUID;

@Entity
@JsonIgnoreProperties(value={"password"}, allowSetters= true)
public class User {
    @Id
    @GeneratedValue
    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID uuid;
    @Column
    private String first_name;
    @Column
    private String last_name;
    @Column(unique = true, length = 32)
    private String emailId;
    @Column(nullable=false)
    private String password;
    @Column
    private Date creationTime;
    @Column
    private Date updatedTime;

    public User(UUID uuid, String first_name, String last_name, String emailId, String password, Date creationTime, Date updatedTime) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.emailId = emailId;
        this.password = password;
    }

    public User()
    {

    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }
}
