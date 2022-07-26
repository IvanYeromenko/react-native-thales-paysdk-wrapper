package com.reactnativethalespaysdkwrapper.model;

public class DigitalCard {

    private String tokenId;
    private String digitalizedCardId;
    private boolean isDefaultCardFlag;
    private boolean isRemotePaymentSupported;
    private String cardState;
    private DigitalCardDetails digitalCardDetails;



   public DigitalCard() {
  }

  public DigitalCard(String tokenId, String digitalizedCardId, boolean isDefaultCardFlag, boolean isRemotePaymentSupported, String cardState, DigitalCardDetails digitalCardDetails) {
    this.tokenId = tokenId;
    this.digitalizedCardId = digitalizedCardId;
    this.isDefaultCardFlag = isDefaultCardFlag;
    this.isRemotePaymentSupported = isRemotePaymentSupported;
    this.cardState = cardState;
    this.digitalCardDetails = digitalCardDetails;
  }

  public String getTokenId() {
    return tokenId;
  }

  public void setTokenId(String tokenId) {
    this.tokenId = tokenId;
  }

  public String getDigitalizedCardId() {
    return digitalizedCardId;
  }

  public void setDigitalizedCardId(String digitalizedCardId) {
    this.digitalizedCardId = digitalizedCardId;
  }

  public boolean isDefaultCardFlag() {
    return isDefaultCardFlag;
  }

  public void setDefaultCardFlag(boolean defaultCardFlag) {
    isDefaultCardFlag = defaultCardFlag;
  }

  public boolean isRemotePaymentSupported() {
    return isRemotePaymentSupported;
  }

  public void setRemotePaymentSupported(boolean remotePaymentSupported) {
    isRemotePaymentSupported = remotePaymentSupported;
  }

  public String getCardState() {
    return cardState;
  }

  public void setCardState(String cardState) {
    this.cardState = cardState;
  }

  public DigitalCardDetails getDigitalCardDetails() {
    return digitalCardDetails;
  }

  public void setDigitalCardDetails(DigitalCardDetails digitalCardDetails) {
    this.digitalCardDetails = digitalCardDetails;
  }

}
