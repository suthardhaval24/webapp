package com.cloud.dao;

import com.cloud.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserDao extends CrudRepository<User, UUID> {
    User findByEmailId(String email_address);

    @Query("SELECT count(emailId) FROM User WHERE emailId=:emailId")
    int isEmailPresent(@Param("emailId") String emailId);
}