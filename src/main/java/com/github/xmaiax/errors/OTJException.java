package com.github.xmaiax.errors;

import java.math.BigInteger;

import lombok.Getter;

@Getter
public class OTJException extends Exception {
  private static final long serialVersionUID = -1L;
  @Getter public static enum CommonError {
    INSERT_ACCOUNT_NUMBER(2, "Please insert the account number."),
    INSERT_PASSWORD(4, "Please insert the password."),
    INCORRECT_PASSWORD(5, "Incorrect password.");
    private Integer code;
    private String message;
    CommonError(Integer code, String message) {
      this.code = code;
      this.message = message;
    }
  }
  private Integer code;
  public OTJException(String message) {
    super(message); this.code = BigInteger.ONE.intValue(); }
  public OTJException(Integer code, String message) {
    super(message); this.code = code; }
  public OTJException(CommonError commonError) {
    super(commonError.getMessage());
    this.code = commonError.getCode();
  }
}
