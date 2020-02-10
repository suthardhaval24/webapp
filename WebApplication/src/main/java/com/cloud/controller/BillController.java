package com.cloud.controller;

import com.cloud.repository.BillRepository;
import com.cloud.repository.UserRepository;
import com.cloud.entity.Bill;
import com.cloud.entity.User;
import com.cloud.errors.BillStatus;
import com.cloud.service.BillService;
import com.cloud.service.UserService;
import com.cloud.validator.BillValidator;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;

@RestController
public class BillController {

    @Autowired
    BillRepository billRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BillService billService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BillValidator billValidator;

    @InitBinder
    private void initBinder(WebDataBinder binder) {
        binder.setValidator(billValidator);
    }


    @PostMapping(value = "/v1/bill/")
    public ResponseEntity<?> createBill(@RequestHeader(value = "Authorization", required = false) String token, @Valid @RequestBody(required = false) Bill bill, BindingResult errors,
                                        HttpServletResponse response) throws Exception {

        //check that authorization header is not missing
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        //check that body is not empty
        if (bill == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request body cannot be empty");
        }

        BillStatus billStatus;
        User user;
        String userDetails[] = decryptAuthenticationToken(token);
        if (errors.hasErrors()) {
            billStatus = billService.getBillStatus(errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(billStatus);
        } else {
            if (!(userService.isEmailPresent(userDetails[0])))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            else
                user = userRepository.findByEmailId(userDetails[0]);
            bill.setOwner_id(user.getUuid());
            bill.setUser(userRepository.findByEmailId(userDetails[0]));
            bill.setCreationTime(new Date());
            bill.setUpdatedTime(new Date());
            Bill new_bill = billRepository.save(bill);
            return new ResponseEntity<Bill>(new_bill, HttpStatus.CREATED);
        }
    }


    @GetMapping("/v1/bills")
    public ResponseEntity<?> getBills(@RequestHeader(value = "Authorization", required = false) String token) throws Exception {

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String userDetails[] = decryptAuthenticationToken(token);

        if (!(userService.isEmailPresent(userDetails[0])))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        else {
            User user = userService.getUser(userDetails[0]);
            //retrieve user bills
            List<Bill> all_bills = billRepository.findByOwnerId(user.getUuid());
            //if user has no bills
            if (all_bills.isEmpty())
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No bills found");
            else
                return new ResponseEntity<List<Bill>>(all_bills, HttpStatus.OK);
        }

    }

    @GetMapping("/v1/bill/{id}")
    public ResponseEntity<?> getBill(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable("id") String id) throws Exception {

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String userDetails[] = decryptAuthenticationToken(token);
        User user;
        Bill user_bill;
        UUID billId;
        //invalid url uuid
        try {
            billId = UUID.fromString(id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid URL");
        }

        if (!(userService.isEmailPresent(userDetails[0])))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        else {
            //if no bills are found
            try {
                user_bill = billRepository.findById(billId).get();
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            user = userService.getUser(userDetails[0]);
            if (!user_bill.getOwner_id().equals(user.getUuid())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found");
            } else {
                return new ResponseEntity<Bill>(user_bill, HttpStatus.OK);
            }
        }
    }

    @DeleteMapping("/v1/bill/{id}")
    public ResponseEntity<?> deleteBill(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable String id) throws Exception {

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String userDetails[] = decryptAuthenticationToken(token);
        User user;
        Bill bill;
        UUID billId;

        try {
            billId = UUID.fromString(id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid URL");
        }

        if (!(userService.isEmailPresent(userDetails[0])))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        else {
            try {
                bill = billRepository.findById(billId).get();
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No content");
            }
            user = userService.getUser(userDetails[0]);
            if (!bill.getOwner_id().equals(user.getUuid())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("NOT FOUND");
            } else {
                //delete file from dir
                if (bill.getFileUpload() != null) {
                    String path = bill.getFileUpload().getUrl();
                    File file = new File(path);
                    file.delete();
                }
                billRepository.delete(bill);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Deleted");

            }
        }
    }

    @PutMapping("/v1/bill/{id}")
    public ResponseEntity<?> updateBill(@RequestHeader(value = "Authorization", required = false) String token, @Valid @RequestBody(required = false) Bill bill, BindingResult errors,
                                        @PathVariable("id") String id) throws Exception {
        if (token == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unauthorized");
        }

        //check that body is not empty
        if (bill == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request body cannot be empty");
        }

        BillStatus billStatus;
        Bill currentBill;
        User user;
        String userDetails[] = decryptAuthenticationToken(token);
        UUID billId;
        try {
            billId = UUID.fromString(id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid URL");
        }
        if (errors.hasErrors()) {
            billStatus = billService.getBillStatus(errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(billStatus);
        } else {
            if (!(userService.isEmailPresent(userDetails[0])))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            else {
                try {
                    currentBill = billRepository.findById(billId).get();
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                }
                user = userService.getUser(userDetails[0]);
                if (!currentBill.getOwner_id().equals(user.getUuid())) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("NOT FOUND");
                } else {
                    currentBill.setPayment_status(bill.getPayment_status());
                    currentBill.setAmount_due(bill.getAmount_due());
                    currentBill.setBill_date(bill.getBill_date());
                    currentBill.setDue_date(bill.getDue_date());
                    currentBill.setVendor(bill.getVendor());
                    currentBill.setUpdatedTime(new Date());
                    currentBill.setCategories(bill.getCategories());
                    billRepository.save(currentBill);
                    return new ResponseEntity<Bill>(currentBill, HttpStatus.OK);
                }
            }
        }
    }

    //json mapping exception
    @ExceptionHandler({JsonMappingException.class})
    public ResponseEntity<?> handleException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Date format should be in 'YYYY-MM_DD' or amount_due should be numeric or payment_status value one of this : [paid, due, past_due, no_payment_required]");
    }

    //decryptToken
    public String[] decryptAuthenticationToken(String token) throws UnsupportedEncodingException {
        String[] basicAuthToken = token.split(" ");
        byte[] authKeys = Base64.getDecoder().decode(basicAuthToken[1]);
        return new String(authKeys, "utf-8").split(":");

    }
}

