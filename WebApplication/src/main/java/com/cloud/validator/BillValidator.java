package com.cloud.validator;

import com.cloud.entity.Bill;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class BillValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return Bill.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "vendor", "vendor required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "bill_date", "bill_date required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "due_date", "due_date required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "amount_due", "amount required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "payment_status", "payment_status required");

        if (errors.hasErrors()) return;

        Bill bill = (Bill) target;

        if (!(bill.getAmount_due() > 0.01))
            errors.rejectValue("amount_due", "Cannot be less than 0.01");
        
    }

}
