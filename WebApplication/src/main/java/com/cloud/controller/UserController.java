package com.cloud.controller;

import com.cloud.entity.User;
import com.cloud.errors.UserRegistrationStatus;
import com.cloud.service.UserService;
import com.cloud.validator.UserValidator;
import com.timgroup.statsd.StatsDClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    private final static Logger logger = LogManager.getLogger(UserController.class);
    private final String userHTTPGET = "endpoint.user.register.HTTP.GET";
    private final String userHTTPPOST = "endpoint.user.register.HTTP.POST";
    private final String userHTTPPUT = "endpoint.user.register.HTTP.PUT";

    @Autowired
    private UserService userService;

    @Autowired
    private UserValidator userValidator;

    @Autowired
    private StatsDClient statsd;

    @InitBinder
    private void initBinder(WebDataBinder binder) {
        binder.setValidator(userValidator);
    }

    //Post API : post user info
    @RequestMapping(value = "v1/user", method = RequestMethod.POST)
    public ResponseEntity<?> createUser(@Valid @RequestBody User user, BindingResult errors,
                                        HttpServletResponse response) throws Exception {
        statsd.incrementCounter(userHTTPPOST);
        statsd.recordExecutionTime(userHTTPPOST,3000);
        logger.info("User : Post Method");
        UserRegistrationStatus registrationStatus;

        if (errors.hasErrors()) {
            registrationStatus = userService.getRegistrationStatus(errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    registrationStatus);
        } else {
            user.setCreationTime(new Date());
            user.setUpdatedTime(new Date());
            registrationStatus = new UserRegistrationStatus();
            User u = userService.saveUser(user);
            logger.info("User Created Successfully");
            return new ResponseEntity<User>(u, HttpStatus.CREATED);
        }
    }

    //GET API : get user info
    @RequestMapping(value = "v1/user/self", method = RequestMethod.GET)
    public ResponseEntity<?> getUser(@RequestHeader(value = "Authorization", required = false) String token, HttpServletRequest request) throws UnsupportedEncodingException {

        statsd.incrementCounter(userHTTPGET);
        statsd.recordExecutionTime(userHTTPGET,3000);
        logger.info("User: Get Method");
        if (token == null) {
            logger.debug("User: Put Method:UNAUTHORIZED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String userDetails[] = decryptAuthenticationToken(token);

        if (!(userService.isEmailPresent(userDetails[0])))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        else
            return ResponseEntity.status(HttpStatus.OK).body(userService.getUser(userDetails[0]));
    }

    //PUT API : Upate user
    @RequestMapping(value = "v1/user/self", method = RequestMethod.PUT)
    public ResponseEntity<?> updateUser(@RequestHeader(value = "Authorization", required = false) String Header, @Valid @RequestBody User user, BindingResult errors,
                                        HttpServletResponse response) throws UnsupportedEncodingException {
        statsd.incrementCounter(userHTTPPUT);
        statsd.recordExecutionTime(userHTTPPUT,3000);
        logger.info("User: Put Method");
        if (Header == null) {
            logger.debug("User: Put Method:UNAUTHORIZED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String[] userDetails = decryptAuthenticationToken(Header);
        if (userService.updateUserInfo(user, userDetails[0], userDetails[1])) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
        }
    }

    //Request body missing exception
    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ResponseEntity<?> handleEException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request Body Cannot be Empty");
    }

    //decryptToken
    public String[] decryptAuthenticationToken(String token) throws UnsupportedEncodingException {
        String[] basicAuthToken = token.split(" ");
        byte[] authKeys = Base64.getDecoder().decode(basicAuthToken[1]);
        return new String(authKeys, "utf-8").split(":");
    }
}

