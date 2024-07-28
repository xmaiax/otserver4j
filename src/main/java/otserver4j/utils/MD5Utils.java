package otserver4j.utils;

import static java.math.BigInteger.ONE;

@lombok.extern.slf4j.Slf4j public class MD5Utils {
  private static final String SALT = "9b783618935148a5fd";
  private java.security.MessageDigest messageDigest;
  private MD5Utils() {
    try { this.messageDigest = java.security.MessageDigest.getInstance("MD5"); }
    catch(java.security.NoSuchAlgorithmException nsae) {
      log.error("Unable to load MD5 algorithm: ", nsae);
      System.exit(-ONE.intValue());
    }
  }
  public static final MD5Utils INSTANCE = new MD5Utils();
  public String str2md5(final String sensitiveData) {
    if(sensitiveData == null || sensitiveData.isBlank()) return null;
    return String.format("%32s", new java.math.BigInteger(ONE.intValue(),
      this.messageDigest.digest(SALT.concat(sensitiveData).getBytes())).toString(16))
        .replace(' ', '0');
  }
}
