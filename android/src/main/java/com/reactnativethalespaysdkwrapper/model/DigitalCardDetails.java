package com.reactnativethalespaysdkwrapper.model;

public class DigitalCardDetails {

  private String panExpiry;
  private String lastFourDigitsDPAN;
  private String lastFourDigitsFPAN;


  public String getPanExpiry() {
    return panExpiry;
  }

  public void setPanExpiry(String panExpiry) {
    this.panExpiry = panExpiry;
  }

  public String getLastFourDigitsDPAN() {
    return lastFourDigitsDPAN;
  }

  public void setLastFourDigitsDPAN(String lastFourDigitsDPAN) {
    this.lastFourDigitsDPAN = lastFourDigitsDPAN;
  }

  public String getLastFourDigitsFPAN() {
    return lastFourDigitsFPAN;
  }

  public void setLastFourDigitsFPAN(String lastFourDigitsFPAN) {
    this.lastFourDigitsFPAN = lastFourDigitsFPAN;
  }
}
