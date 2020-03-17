package com.cloud.controller;

import com.cloud.entity.Bill;
import com.cloud.entity.FileUpload;
import com.cloud.entity.User;
import com.cloud.repository.BillRepository;
import com.cloud.repository.FileRepository;
import com.cloud.service.AWSFileStorageService;
import com.cloud.service.FileStorageService;
import com.cloud.service.UserService;
import com.timgroup.statsd.StatsDClient;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Base64;
import java.util.UUID;

@RestController
public class FileController {

    private final static Logger logger = LogManager.getLogger(BillController.class);
    private final String fileHTTPGET = "endpoint.file.HTTP.GET";
    private final String fileHTTPPOST = "endpoint.file.HTTP.POST";
    private final String fileHTTPDELETE = "endpoint.file.HTTP.DELETE";

    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    BillRepository billRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AWSFileStorageService awsFileStorageService;

    @Autowired
    private StatsDClient statsd;

    @PostMapping("/v1/bill/{billId}/file")
    public ResponseEntity<?> uploadFile(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable("billId") String id,
                                        @RequestParam("file") MultipartFile file, HttpServletRequest request) throws Exception {
        statsd.incrementCounter(fileHTTPPOST);
        statsd.recordExecutionTime(fileHTTPPOST,3000);
        logger.info("File: Post Method");
        //check that authorization header is not missing
        if (token == null) {
            logger.debug("Bill: Post Method: User Unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        User user;
        Bill user_bill = null;
        UUID billId;
        //invalid url uuid
        try {
            billId = UUID.fromString(id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid URL");
        }

        String userDetails[] = decryptAuthenticationToken(token);

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
                String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
                if (!fileExtension.equals("pdf") && !fileExtension.equals("png") && !fileExtension.equals("jpg") && !fileExtension.equals("jpeg")) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("FileUpload of extension pdf,jpg,jpeg,png are only accepted");
                } else {
                    if (user_bill.getFileUpload() != null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File already Exist,First delete and upload new one.");
                    } else {
                        String actualPath = awsFileStorageService.storeFile(file);
                        FileUpload newFileUpload = fileStorageService.storeFile(file, actualPath);
                        user_bill.setFileUpload(newFileUpload);
                        billRepository.save(user_bill);
                        //System.out.println(fileDownloadUri);
                        return new ResponseEntity<FileUpload>(newFileUpload, HttpStatus.CREATED);
                    }
                }
            }
        }
    }

    @GetMapping("/v1/bill/{billId}/file/{fileId}")
    public ResponseEntity<?> getFile(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable("billId") String billId,
                                     @PathVariable("fileId") String fileId) throws Exception {
        statsd.incrementCounter(fileHTTPGET);
        statsd.recordExecutionTime(fileHTTPGET,3000);
        logger.info("File: GET Method");
        if (token == null) {
            logger.debug("Bill: Post Method: User Unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String userDetails[] = decryptAuthenticationToken(token);
        User user;
        Bill user_bill;
        FileUpload fileUpload;
        UUID tempBillId;
        UUID tempFileId;

        //invalid url uuid
        try {
            tempBillId = UUID.fromString(billId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid URL");
        }
        try {
            tempFileId = UUID.fromString(fileId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid URL");
        }

        if (!(userService.isEmailPresent(userDetails[0])))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        else {
            //if no bills are found
            try {
                user_bill = billRepository.findById(tempBillId).get();
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            user = userService.getUser(userDetails[0]);
            if (!user_bill.getOwner_id().equals(user.getUuid())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found");
            } else {
                try {
                    fileUpload = fileRepository.findById(tempFileId).get();
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                }
                if (!user_bill.getFileUpload().getId().equals(fileUpload.getId())) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found");
                } else {
                    return new ResponseEntity<FileUpload>(fileUpload, HttpStatus.OK);
                }

            }
        }
    }


    @DeleteMapping("/v1/bill/{billId}/file/{fileId}")
    public ResponseEntity<?> deleteFile(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable("billId") String billId,
                                        @PathVariable("fileId") String fileId) throws Exception {
        statsd.incrementCounter(fileHTTPDELETE);
        statsd.recordExecutionTime(fileHTTPDELETE,3000);
        logger.info("File: DELETE Method");
        if (token == null) {
            logger.debug("Bill: Post Method: User Unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String userDetails[] = decryptAuthenticationToken(token);
        User user;
        Bill user_bill;
        FileUpload fileUpload;
        UUID tempBillId;
        UUID tempFileId;
        //invalid url uuid
        try {
            tempBillId = UUID.fromString(billId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid URL");
        }
        try {
            tempFileId = UUID.fromString(fileId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid URL");
        }

        if (!(userService.isEmailPresent(userDetails[0])))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        else {
            //if no bills are found
            try {
                user_bill = billRepository.findById(tempBillId).get();
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            user = userService.getUser(userDetails[0]);
            if (!user_bill.getOwner_id().equals(user.getUuid())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found");
            } else {
                try {
                    fileUpload = fileRepository.findById(tempFileId).get();
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                }
                if (!user_bill.getFileUpload().getId().equals(fileUpload.getId())) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found");
                } else {
//                    Working
//                    String path = fileUpload.getUrl();
//                    File file = new File(path);
//                    file.delete();
                    if(!awsFileStorageService.deleteFile(fileUpload.getUrl())){
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                    }
                    user_bill.setFileUpload(null);
                    fileRepository.delete(fileUpload);
                    user_bill.setFileUpload(null);
                    billRepository.save(user_bill);
                    return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Deleted");
                }
            }
        }
    }

    //decryptToken
    public String[] decryptAuthenticationToken(String token) throws UnsupportedEncodingException {
        String[] basicAuthToken = token.split(" ");
        byte[] authKeys = Base64.getDecoder().decode(basicAuthToken[1]);
        return new String(authKeys, "utf-8").split(":");

    }

    //local file storage code
    public String fileUpload(MultipartFile uploadFile) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        String fileName = FilenameUtils.removeExtension(uploadFile.getOriginalFilename());
//        System.out.println(fileName);
        String rootPath = System.getProperty("user.dir");
//        System.out.println(rootPath);
        String uniqueID = UUID.randomUUID().toString();
        File dir = new File(rootPath + File.separator + "resource" + File.separator + "files");
        if (!dir.exists())
            dir.mkdirs();
        File newFile = new File(dir.getAbsolutePath() + File.separator + fileName + "_" + uniqueID + "." +
                FilenameUtils.getExtension(uploadFile.getOriginalFilename()));

        try {
            inputStream = uploadFile.getInputStream();

            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            outputStream = new FileOutputStream(newFile);
            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return newFile.getAbsolutePath();
    }
}
