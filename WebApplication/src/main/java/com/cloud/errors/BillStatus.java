package com.cloud.errors;

public class BillStatus {
    private String vendorError;
    private String billDateError;
    private String dueDateError;
    private String amountDueError;
    private String paymentStatusError;

    public BillStatus(){
        vendorError ="-";
        billDateError ="-";
        dueDateError ="-";
        amountDueError ="-";
        paymentStatusError ="-";
    }

    public BillStatus(String vendorError, String billDateError, String dueDateError, String amountDueError, String paymentStatusError) {
        this.vendorError = vendorError;
        this.billDateError = billDateError;
        this.dueDateError = dueDateError;
        this.amountDueError = amountDueError;
        this.paymentStatusError = paymentStatusError;
    }

    public String getVendorError() {
        return vendorError;
    }

    public void setVendorError(String vendorError) {
        this.vendorError = vendorError;
    }

    public String getBillDateError() {
        return billDateError;
    }

    public void setBillDateError(String billDateError) {
        this.billDateError = billDateError;
    }

    public String getDueDateError() {
        return dueDateError;
    }

    public void setDueDateError(String dueDateError) {
        this.dueDateError = dueDateError;
    }

    public String getAmountDueError() {
        return amountDueError;
    }

    public void setAmountDueError(String amountDueError) {
        this.amountDueError = amountDueError;
    }

    public String getPaymentStatusError() {
        return paymentStatusError;
    }

    public void setPaymentStatusError(String paymentStatusError) {
        this.paymentStatusError = paymentStatusError;
    }
}
