package com.cloud.controller;

import com.cloud.entity.User;
import com.cloud.errors.UserRegistrationStatus;
import com.cloud.service.UserService;
import com.cloud.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Date;

@RestController
@RequestMapping("/")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserValidator userValidator;

    @InitBinder
    private void initBinder(WebDataBinder binder) {
        binder.setValidator(userValidator);
    }

    //Post API : post user info
    @RequestMapping(value = "v1/user", method = RequestMethod.POST)
    public ResponseEntity<?> createUser(@Valid @RequestBody User user, BindingResult errors,
                                        HttpServletResponse response) throws Exception {
        UserRegistrationStatus registrationStatus;

        if(errors.hasErrors()) {
            registrationStatus = userService.getRegistrationStatus(errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    registrationStatus);
        }else {
            user.setCreationTime(new Date());
            user.setUpdatedTime(new Date());
            registrationStatus = new UserRegistrationStatus();
            User u = userService.saveUser(user);

            return  new ResponseEntity<User>(u, HttpStatus.CREATED);
        }
    }

    //GET API : get user info
    @RequestMapping(value="v1/user/self" ,method = RequestMethod.GET)
    public ResponseEntity<User> getUser(@RequestHeader("Authorization") String token, HttpServletRequest request) throws UnsupportedEncodingException {

        String userDetails[] = decryptAuthenticationToken(token);

        if(!(userService.isEmailPresent(userDetails[0])))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        else
            return ResponseEntity.status(HttpStatus.OK).body(userService.getUser(userDetails[0]));
    }

    //PUT API : Upate user
    @RequestMapping(value = "v1/user/self", method = RequestMethod.PUT)
    public ResponseEntity<String> updateUser(@RequestHeader("Authorization") String Header, @Valid @RequestBody User user, BindingResult errors,
                                             HttpServletResponse response) throws UnsupportedEncodingException {

        String[] userDetails =  decryptAuthenticationToken(Header);

        if(userService.updateUserInfo(user, userDetails[0], userDetails[1])){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
        }
    }

    //decryptToken
    public String[] decryptAuthenticationToken(String token) throws UnsupportedEncodingException {
        String[] basicAuthToken = token.split(" ");
        byte[] authKeys = Base64.getDecoder().decode(basicAuthToken[1]);
        return new String(authKeys,"utf-8").split(":");
    }
}

