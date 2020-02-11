package com.cloud.controller;

import com.cloud.entity.Bill;
import com.cloud.entity.FileUpload;
import com.cloud.entity.User;
import com.cloud.repository.BillRepository;
import com.cloud.repository.FileRepository;
import com.cloud.service.FileStorageService;
import com.cloud.service.UserService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Base64;
import java.util.UUID;

@RestController
public class FileController {

    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    BillRepository billRepository;

    @Autowired
    private UserService userService;

    @PostMapping("/v1/bill/{billId}/file")
    public ResponseEntity<?> uploadFile(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable("billId") String id,
                                        @RequestParam("file") MultipartFile file, HttpServletRequest request) throws Exception {

        //check that authorization header is not missing
        if (token == null) {
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
                        String actualPath = fileUpload(file);
                        System.out.println(actualPath);
                        FileUpload newFileUpload = fileStorageService.storeFile(file, actualPath, user_bill);
                        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/FileUpload/")
                                .path(newFileUpload.getId().toString())
                                .toUriString();
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

        if (token == null) {
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
                if (!user_bill.getId().equals(fileUpload.getBill().getId())) {
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
        if (token == null) {
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
                if (!user_bill.getId().equals(fileUpload.getBill().getId())) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found");
                } else {
//                    Working
                    String path = fileUpload.getUrl();
                    File file = new File(path);
                    file.delete();
                    user_bill.setFileUpload(null);
                    fileRepository.delete(fileUpload);
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
