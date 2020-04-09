package com.cloud.controller;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.cloud.entity.Bill;
import com.cloud.entity.User;
import com.cloud.errors.BillStatus;
import com.cloud.repository.BillRepository;
import com.cloud.repository.UserRepository;
import com.cloud.service.AWSFileStorageService;
import com.cloud.service.BillService;
import com.cloud.service.UserService;
import com.cloud.validator.BillValidator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.timgroup.statsd.StatsDClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.*;

@RestController
public class BillController {

    private final static Logger logger = LogManager.getLogger(BillController.class);
    private final String billHTTPGET = "endpoint.bill.HTTP.GET";
    private final String billsHTTPGET = "endpoint.bills.HTTP.GET";
    private final String DueBillsHTTPGET = "endpoint.Duebills.HTTP.GET";
    private final String billHTTPPOST = "endpoint.bill.HTTP.POST";
    private final String billHTTPPUT = "endpoint.bill.HTTP.PUT";
    private final String billHTTPDELETE = "endpoint.bill.HTTP.DELETE";
    private final String TABLE_NAME = "csye6225_Webapp_BillDues";

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

    @Autowired
    private AWSFileStorageService awsFileStorageService;

    @Autowired
    private StatsDClient statsd;

    @Value("${ARN}")
    private String topicArn;

    @Value("${domainName}")
    private String domain;


    @InitBinder
    private void initBinder(WebDataBinder binder) {
        binder.setValidator(billValidator);
    }


    @PostMapping(value = "/v1/bill/")
    public ResponseEntity<?> createBill(@RequestHeader(value = "Authorization", required = false) String token, @Valid @RequestBody(required = false) Bill bill, BindingResult errors,
                                        HttpServletResponse response) throws Exception {
        long startTime = System.currentTimeMillis();
        statsd.incrementCounter(billHTTPPOST);
        logger.info("Bill: Post Method");
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
            //from this attachment always be null
            bill.setFileUpload(null);
            Bill new_bill = billRepository.save(bill);
            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime);
            statsd.recordExecutionTime(billHTTPPOST, duration);
            return new ResponseEntity<Bill>(new_bill, HttpStatus.CREATED);
        }
    }

    @GetMapping("/v1/bills")
    public ResponseEntity<?> getBills(@RequestHeader(value = "Authorization", required = false) String token) throws Exception {
        long startTime = System.currentTimeMillis();
        statsd.incrementCounter(billsHTTPGET);
        logger.info("Bills: GET Method");
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
            else {
                long endTime = System.currentTimeMillis();
                long duration = (endTime - startTime);
                statsd.recordExecutionTime(billsHTTPGET, duration);
                return new ResponseEntity<List<Bill>>(all_bills, HttpStatus.OK);
            }
        }

    }

    @GetMapping("/v1/bill/due/{x}")
    public ResponseEntity<?> getDueBills(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable("x") String days) throws Exception {
        long startTime = System.currentTimeMillis();
        statsd.incrementCounter(DueBillsHTTPGET);
        logger.info("DueBill: GET Method");
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String userDetails[] = decryptAuthenticationToken(token);

        if (!days.matches("^([012]?[0-9]?[0-9]|3[0-5][0-9]|36[0-6])$")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid URL");
        }

        if (!(userService.isEmailPresent(userDetails[0])))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        else {
            User user = userService.getUser(userDetails[0]);
            //sending message to queue
            String QUEUE_NAME = "BillDueQueue";
            AmazonSQS sqs = AmazonSQSClient.builder().withRegion("us-east-1")
                    .withCredentials(new InstanceProfileCredentialsProvider(false)).build();
            String queue_url = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
            logger.info("QueueName", queue_url);
            JsonObject dueBillMessage = new JsonObject();
            dueBillMessage.addProperty("Days", days);
            dueBillMessage.addProperty("Email", user.getEmailId());
            sqs.sendMessage(new SendMessageRequest(queue_url, dueBillMessage.toString()));

            //creating a thread to run method in background
            new Thread(() -> {
                snsQuery(user);
            }).start();

            //check if request is already made in 60 minutes

            Boolean flag = checkDynamoDB(user.getEmailId());

            if (flag) {
                return new ResponseEntity<String>("Already requested in 1 hour time frame.", HttpStatus.OK);
            } else {
                long endTime = System.currentTimeMillis();
                long duration = (endTime - startTime);
                statsd.recordExecutionTime(DueBillsHTTPGET, duration);
                return new ResponseEntity<String>("Bills Link will be sent on your email address inside 1 hour.", HttpStatus.OK);
            }
        }
    }

    @GetMapping("/v1/bill/{id}")
    public ResponseEntity<?> getBill(@RequestHeader(value = "Authorization", required = false) String
                                             token, @PathVariable("id") String id) throws Exception {
        long startTime = System.currentTimeMillis();
        statsd.incrementCounter(billHTTPGET);
        logger.info("Bill: GET Method");
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
                long endTime = System.currentTimeMillis();
                long duration = (endTime - startTime);
                statsd.recordExecutionTime(billHTTPGET, duration);
                return new ResponseEntity<Bill>(user_bill, HttpStatus.OK);
            }
        }
    }

    @DeleteMapping("/v1/bill/{id}")
    public ResponseEntity<?> deleteBill(@RequestHeader(value = "Authorization", required = false) String
                                                token, @PathVariable String id) throws Exception {
        long startTime = System.currentTimeMillis();
        statsd.incrementCounter(billHTTPDELETE);
        logger.info("Bill: DELETE Method");
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
                    if (!awsFileStorageService.deleteFile(path)) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                    }
                }
                billRepository.delete(bill);
                long endTime = System.currentTimeMillis();
                long duration = (endTime - startTime);
                statsd.recordExecutionTime(billHTTPDELETE, duration);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Deleted");

            }
        }
    }

    @PutMapping("/v1/bill/{id}")
    public ResponseEntity<?> updateBill(@RequestHeader(value = "Authorization", required = false) String
                                                token, @Valid @RequestBody(required = false) Bill bill, BindingResult errors,
                                        @PathVariable("id") String id) throws Exception {
        long startTime = System.currentTimeMillis();
        statsd.incrementCounter(billHTTPPUT);
        logger.info("Bill: PUT Method");
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
                    long endTime = System.currentTimeMillis();
                    long duration = (endTime - startTime);
                    statsd.recordExecutionTime(billHTTPPUT, duration);
                    return new ResponseEntity<Bill>(currentBill, HttpStatus.OK);
                }
            }
        }
    }

    public void snsQuery(User user) {
        //Querying and SNS
        //retrieve user bills
        String message = null;
        //remove . from domain
        domain = domain.substring(0, domain.length() - 1);
        String prefix = domain + "/v1/bill/";
        JSONObject sqsJson = null;
        String QUEUE_NAME = "BillDueQueue";
        AmazonSQS sqs = AmazonSQSClient.builder().withRegion("us-east-1")
                .withCredentials(new InstanceProfileCredentialsProvider(false)).build();
        String queue_url = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
        List<Message> messages = sqs.receiveMessage(queue_url).getMessages();
        Message sqsMessage = messages.get(0);
        final String messageReceiptHandle = messages.get(0).getReceiptHandle();
        sqs.deleteMessage(new DeleteMessageRequest(QUEUE_NAME, messageReceiptHandle));
        String jsonMsg = sqsMessage.getBody();
        sqs.changeMessageVisibility(QUEUE_NAME, sqsMessage.getReceiptHandle(), 60 * 60);
        JSONParser parser = new JSONParser();
        try {
            sqsJson = (JSONObject) parser.parse(jsonMsg);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String days = sqsJson.get("Days").toString();
        LocalDate dueDate = LocalDate.now().plusDays(Long.parseLong(days));
        Date billDuedate = java.sql.Date.valueOf(dueDate);
        List<Bill> all_bills = billRepository.findByDueDate(user.getUuid(), billDuedate);
        if (all_bills.isEmpty())
            message = "No bill found";
        else {
            //if user has no bills
            List<String> dueBillLinks = new ArrayList<>();
            for (Bill b :
                    all_bills) {
                dueBillLinks.add(prefix + b.getId());
            }

            String json = new Gson().toJson(dueBillLinks);

            JsonObject dueBill = new JsonObject();
            dueBill.addProperty("Email", user.getEmailId());
            dueBill.addProperty("DueBill", json);

            message = dueBill.toString();
        }

        AmazonSNS snsClient = AmazonSNSClient.builder().withRegion("us-east-1")
                .withCredentials(new InstanceProfileCredentialsProvider(false)).build();

        PublishRequest publishRequest = new PublishRequest(topicArn, message);
        PublishResult publishResult = snsClient.publish(publishRequest);
        logger.info("SNS Publish Result: " + publishResult);
    }

    boolean checkDynamoDB(String userName) {
        logger.info("DYNAMO DB CHECK");
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDB = new DynamoDB(client);
        Item existUser = dynamoDB.getTable(TABLE_NAME).getItem("id", userName);
        if (existUser != null) {
            return true;
        } else {
            return false;
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

