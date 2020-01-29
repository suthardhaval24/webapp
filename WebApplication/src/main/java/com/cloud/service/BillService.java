package com.cloud.service;


import com.cloud.dao.BillRepository;
import com.cloud.errors.BillStatus;
import com.cloud.errors.UserRegistrationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.UUID;

@Service
public class BillService {

    @Autowired
    BillRepository billRepository;

    public BillStatus getBillStatus(BindingResult errors) {
        FieldError vendorError = errors.getFieldError("vendor");
        FieldError bill_dateError = errors.getFieldError("bill_date");
        FieldError due_dateError = errors.getFieldError("due_date");
        FieldError amount_dueError = errors.getFieldError("amount_due");
        FieldError payment_statusError = errors.getFieldError("payment_status");
        String vendorErrorMessage = vendorError == null ? "-" : vendorError.getCode();
        String bill_dateErrorMessage = bill_dateError == null ? "-" : bill_dateError.getCode();
        String due_dateErrorMessage = due_dateError == null ? "-" : due_dateError.getCode();
        String amount_dueErrorMessage = amount_dueError == null ? "-" : amount_dueError.getCode();
        String payment_statusErrorMessage = payment_statusError == null ? "-" : payment_statusError.getCode();
        BillStatus billStatus = new BillStatus(vendorErrorMessage, bill_dateErrorMessage, due_dateErrorMessage, amount_dueErrorMessage, payment_statusErrorMessage);
        return  billStatus;
    }

    public Boolean isBillPresent(UUID billId) {
        return billRepository.isBillPresent(billId) > 0 ? true : false;
    }

}