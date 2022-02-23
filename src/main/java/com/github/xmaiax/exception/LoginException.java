package com.github.xmaiax.exception;

import java.math.BigInteger;

import lombok.Getter;

@Getter
public final class LoginException extends GenericException {
  private static final long serialVersionUID = -1L;
  public static final int UNEXPECTED_ERROR_CODE = 0x0a;
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
  public LoginException(String message) {
    super(message); this.code = BigInteger.ONE.intValue(); }
  public LoginException(Integer code, String message) {
    super(message); this.code = code; }
  public LoginException(CommonError commonError) {
    super(commonError.getMessage());
    this.code = commonError.getCode();
  }
}
