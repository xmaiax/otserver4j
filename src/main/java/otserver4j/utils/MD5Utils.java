package otserver4j.utils;

import java.math.BigInteger;
import java.security.MessageDigest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MD5Utils {
  private static final String SALT = "9b783618935148a5fd";
  private MessageDigest messageDigest;
  private static final MD5Utils INSTANCE = new MD5Utils();
  private MD5Utils() {
    try { this.messageDigest = MessageDigest.getInstance("MD5"); }
    catch(java.security.NoSuchAlgorithmException nsae) {
      log.error("Unable to load MD5 algorithm: ", nsae);
      System.exit(-BigInteger.ONE.intValue());
    }
  }
  public static MD5Utils getInstance() { return INSTANCE; }
  public String str2md5(String sensitiveData) {
    if(sensitiveData == null || sensitiveData.isEmpty()) return sensitiveData;
    return String.format("%32s", new java.math.BigInteger(1, this.messageDigest.digest(
      SALT.concat(sensitiveData).getBytes())).toString(16)).replace(' ', '0');
  }
}
