package logger;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class MyJschLogger implements com.jcraft.jsch.Logger {
  
  static java.util.Hashtable name = new java.util.Hashtable();
  
  static {
    name.put(new Integer(DEBUG), "DEBUG: ");
    name.put(new Integer(INFO), "INFO: ");
    name.put(new Integer(WARN), "WARN: ");
    name.put(new Integer(ERROR), "ERROR: ");
    name.put(new Integer(FATAL), "FATAL: ");
  }

  public boolean isEnabled(int level) {
    return true;
  }

  public void log(int level, String message) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.sss");
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    String loggerTag = "<Logger>";
    String timestampTag = String.format("<%s>", sdf.format(timestamp));
    String levelTag = (String) name.get(new Integer(level));

    System.err.print(loggerTag + " " + timestampTag + " " + levelTag + " ");
    System.err.println(message);
  }
}
