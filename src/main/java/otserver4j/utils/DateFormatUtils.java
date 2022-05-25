package otserver4j.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateFormatUtils {

  public static final String FULL_DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
  public static final String TIME_FORMAT = "HH:mm:ss";

  private SimpleDateFormat fullSDF;
  private SimpleDateFormat timeSDF;

  private static final DateFormatUtils INSTANCE = new DateFormatUtils();
  private DateFormatUtils() {
    this.fullSDF = new SimpleDateFormat(FULL_DATE_TIME_FORMAT);
    this.timeSDF = new SimpleDateFormat(TIME_FORMAT);
  }
  public static DateFormatUtils getInstance() { return INSTANCE; }

  public String formatFullDate(Date date) {
    return this.fullSDF.format(date == null ? Calendar.getInstance().getTime() : date);
  }

  public String formatFullDate(Calendar cal) {
    return this.fullSDF.format(cal == null ? Calendar.getInstance().getTime() : cal.getTime());
  }

  public String formatTime(Date date) {
    return this.timeSDF.format(date == null ? Calendar.getInstance().getTime() : date);
  }

  public String formatTime(Calendar cal) {
    return this.timeSDF.format(cal == null ? Calendar.getInstance().getTime() : cal.getTime());
  }

  public String fullDateTimeNow() {
    return this.formatFullDate(Calendar.getInstance());
  }

  public String timeNow() {
    return this.formatTime(Calendar.getInstance());
  }

}
