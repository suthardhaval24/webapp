package com.cloud.validator;

import com.cloud.entity.User;
import com.cloud.service.UserService;
import org.passay.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.Arrays;

public class UserValidator implements Validator {
    @Autowired
    private UserService userService;

    @Override
    public boolean supports(Class<?> aClass){
        return User.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "first_name", "first_name required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "last_name", "last_name required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "emailId", "emailId required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "password required");

        if(errors.hasErrors()) return;

        User user = (User) target;

        //email criteria
        if(!user.getEmailId().matches("^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")) errors.rejectValue("emailId", "emailId format is wrong");

        //password criteria
        PasswordValidator validator = new PasswordValidator(Arrays.asList(
                new LengthRule(9, 30),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1),
                new WhitespaceRule()));
        RuleResult result = validator.validate(new PasswordData(user.getPassword()));
        if(!result.isValid()) errors.rejectValue("password", "Password must 9-30 characters long and must have Uppercase, Lowercase, Special characters and Digits");

        if(errors.hasErrors()) return;

        //unique email verification
        if(userService.isEmailPresent(user.getEmailId())) errors.rejectValue("emailId", "account already exists");

    }
}