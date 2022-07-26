package com.reactnativethalespaysdkwrapper.model;

import com.gemalto.mfs.mwsdk.mobilegateway.MGTransactionRecord;

public class TransactionHistory {
    private String transactionId;
    private String transactionAmount;
    private String merchantName;
    private String transactionStatus;
    private String transactionDate;

    public TransactionHistory(MGTransactionRecord allRecords) {
        this.transactionId = allRecords.getTransactionId();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(String transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

  public String getTransactionStatus() {
    return transactionStatus;
  }

  public void setTransactionStatus(String transactionStatus) {
    this.transactionStatus = transactionStatus;
  }

  public String getTransactionDate() {
    return transactionDate;
  }

  public void setTransactionDate(String transactionDate) {
    this.transactionDate = transactionDate;
  }
}
