package com.cloud.service;

import com.cloud.dao.UserDao;
import com.cloud.entity.User;
import com.cloud.entity.UserDetailsCustom;
import com.cloud.errors.UserRegistrationStatus;
import org.passay.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Arrays;
import java.util.Date;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    //save user to DB
    public User saveUser(User user){
        try {
            passwordEncoder = new BCryptPasswordEncoder();
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user = userDao.save(user);
        } catch (Exception e){
            return null;
        }
        return  user;
    }


    //emailcheck
    public Boolean isEmailPresent(String emailId) {
        return userDao.isEmailPresent(emailId) > 0 ? true : false;
    }

    //getuserdetails
    public User getUser(String emailId) {
        return userDao.findByEmailId(emailId);
    }


    @Override
    public UserDetails loadUserByUsername(String emailId) throws UsernameNotFoundException {
        User user = userDao.findByEmailId(emailId);
        if(user==null) throw new UsernameNotFoundException("User with given emailId does not exist");
        else return new UserDetailsCustom(user);
    }


    public UserRegistrationStatus getRegistrationStatus(BindingResult errors) {
        FieldError first_nameError = errors.getFieldError("first_name");
        FieldError last_nameError = errors.getFieldError("last_name");
        FieldError emailIdError = errors.getFieldError("emailId");
        FieldError passwordError = errors.getFieldError("password");
        String first_nameErrorMessage = first_nameError == null ? "-" : first_nameError.getCode();
        String last_nameErrorMessage = last_nameError == null ? "-" : last_nameError.getCode();
        String emailIdErrorMessage = emailIdError == null ? "-" : emailIdError.getCode();
        String passwordErrorMessage = passwordError == null ? "-" : passwordError.getCode();
        UserRegistrationStatus userRegistrationStatus = new UserRegistrationStatus(first_nameErrorMessage,last_nameErrorMessage,emailIdErrorMessage, passwordErrorMessage);
        return userRegistrationStatus;
    }

    //update user
    public Boolean updateUserInfo(User newUser, String emailId, String Password){
        User currUser = userDao.findByEmailId(emailId);
        if(currUser.getEmailId().equals(emailId)) {
            PasswordValidator validator = new PasswordValidator(Arrays.asList(
                    new LengthRule(9, 30),
                    new CharacterRule(EnglishCharacterData.UpperCase, 1),
                    new CharacterRule(EnglishCharacterData.LowerCase, 1),
                    new CharacterRule(EnglishCharacterData.Digit, 1),
                    new CharacterRule(EnglishCharacterData.Special, 1),
                    new WhitespaceRule()));

            RuleResult result = validator.validate(new PasswordData(newUser.getPassword()));

            if(result.isValid()) {
                currUser.setFirst_name(newUser.getFirst_name());
                currUser.setLast_name(newUser.getLast_name());
                currUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
                currUser.setUpdatedTime(new Date());
                userDao.save(currUser);
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }
}