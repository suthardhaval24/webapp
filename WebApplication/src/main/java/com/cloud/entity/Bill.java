package com.cloud.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Entity
public class Bill {

    public enum Payment_Status {
        paid, due, past_due, no_payment_required;
    }

    @Id
    @GeneratedValue
    @Type(type = "uuid-char")
    @Column(name = "bill_id")
    private UUID id;
    @Column(name = "owner_id")
    private UUID owner_id;
    @Column
    private String vendor;
    @Column
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bill_date;
    @Column
    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    private Date due_date;
    @Column
    private Double amount_due;
    @Column
    private Date creationTime;
    @Column
    private Date updatedTime;
    @ElementCollection
    @CollectionTable(name = "categories")
    private Set<String> categories;
    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private Payment_Status payment_status;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "file_id")
    private FileUpload fileUpload;

    public Bill() {
    }

    public Bill(UUID bill_id, String vendor, Date bill_date, Date due_date, Double amount_due, Payment_Status payment_status) {
        this.id = bill_id;
        this.vendor = vendor;
        this.bill_date = bill_date;
        this.amount_due = amount_due;
        this.payment_status = payment_status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID uuid) {
        this.id = uuid;
    }


    public UUID getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(UUID owner_id) {
        this.owner_id = owner_id;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public Date getBill_date() {
        return bill_date;
    }

    public void setBill_date(Date bill_date) {
        this.bill_date = bill_date;
    }

    public Date getDue_date() {
        return due_date;
    }

    public void setDue_date(Date due_date) {
        this.due_date = due_date;
    }

    public Double getAmount_due() {
        return amount_due;
    }

    public void setAmount_due(Double amount_due) {
        this.amount_due = amount_due;
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

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }

    public Payment_Status getPayment_status() {
        return payment_status;
    }

    public void setPayment_status(Payment_Status payment_status) {
        this.payment_status = payment_status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public FileUpload getFileUpload() {
        return fileUpload;
    }

    public void setFileUpload(FileUpload fileUpload) {
        this.fileUpload = fileUpload;
    }
}
