package com.tk.fcmb.utils;


import java.util.Date;

public class SMSBuilder {
    public static String sendFTSMS(String ref, String debitOrCredit, String accountNumber, Double amount, String description, String narration, Date transDate, Double balance) {

        if (debitOrCredit.equals("C")) {
            debitOrCredit = "CREDIT";
        } else {
            debitOrCredit = "DEBIT";
        }


        return "Txn: " + debitOrCredit + "\n" +
                "Acct: " + formatAccountNumber(accountNumber) + "\n" +
                "Amt: NGN" + formatDecimal(amount) + "\n" +
                "Desc: " + description + "\n" +
                "Nar:" + narration + "\n" +
                "Date:" + DateUtil.formatDate(transDate) + "\n" +
                "Bal: NGN" + formatDecimal(balance) + "\n" +
                "Ref:" + ref + "\n";
    }

    public static String sendInternalFTSMS(String ref, String debitOrCredit, String accountNumber, Double amount, String description, String narration, Date transDate, Double balance) {

        if (debitOrCredit.equals("C")) {
            debitOrCredit = "CREDIT";
        } else {
            debitOrCredit = "DEBIT";
        }


        return "Txn: " + debitOrCredit + "\n" +
                "Acct: " + formatAccountNumber(accountNumber) + "\n" +
                "Amt: NGN" + formatDecimal(amount) + "\n" +
                "Desc: " + description + "\n" +
                "Nar:" + narration + "\n" +
                "Date:" + DateUtil.formatDate(transDate) + "\n" +
                "Bal: NGN" + formatDecimal(balance) + "\n" +
                "Ref:" + ref + "\n";
    }

    public static String sendBillPaymentSMS(String ref, String serviceId, String billType, Double amount) {
        return "Thanks for using our FCMBMFB Bill Payments Services, Your Purchase was successful. BILL TYPE:" + billType + ", AMOUNT: NGN:" + amount + ", SERVICE ID:" + serviceId + "\n" + " REF " + ref;
    }

    public static String sendAirtimeSMS(String ref, Double amount, String network) {
        return "Thanks for using our FCMBMFB Airtime TopUp Services, Your Airtime Purchase was successful. AMOUNT: NGN:" + amount + ", NETWORK:" + network + "\n" + " REF " + ref;
    }

    public static String accountOpeningSMS(String accountNumber) {
        return "Thank you for opening a new Account with us, Your account number is  " + accountNumber + ".\nPlease, Go to the bank and update your KYC Information";
    }

    private static String formatDecimal(Double amount) {
        return String.format("%,.2f", amount).trim();
    }

    private static String formatAccountNumber(String accountNumber) {
        return accountNumber.substring(0, 1) + "XXXX" + accountNumber.substring(5, 10);
    }
}
