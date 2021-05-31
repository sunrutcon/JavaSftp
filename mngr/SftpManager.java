/**
 * set password 2020-03-03
 *   https://www.programcreek.com/java-api-examples/?class=com.jcraft.jsch.Session&method=setPassword
 *   https://www.programcreek.com/java-api-examples/?code=zhuyuqing%2Fbestconf%2Fbestconf-master%2Fsrc%2Fmain%2Fcn%2Fict%2Fzyq%2FbestConf%2Fcluster%2FUtils%2FSFTPUtil.java#
 */

package mngr;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import logger.MyJschLogger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.ProxySOCKS4;
import com.jcraft.jsch.ProxySOCKS5;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

public class SftpManager {

  static String APP_VERSION = "1.7 (added bind interface)";

  static Options options = new Options();
  static CommandLineParser parser = new DefaultParser();
  static CommandLine cmd;

  /*
   * Below we have declared and defined the SFTP HOST, PORT, USER and Local
   * private key from where you will make connection
   */
  static String SFTPHOST;
  static int SFTPPORT;
  static String SFTPUSER;
  static String SFTPPASSWORD;
  
  // this file can be id_rsa or id_dsa based on which algorithm is used to
  // create the key
  // String privateKey = "/home/user/private_key.pk";
  static String privateKey;
  static String SFTPWORKINGDIR;

  static String remoteFileName;
  static String localFileName;

  static String proxyHost;
  static int proxyPort;
  static String proxyType;
  
  static String bindHost;
  static int bindPort;

  static String[] _args;

  static JSch jSch = new JSch();
  static Session session = null;
  static Channel channel = null;
  static ChannelSftp channelSftp = null;

  /**
   * @param args
   * @throws ParseException
   */
  public static void main(String[] args) throws ParseException {
    createOptions();
    parseArguments(args);
    handleCommand();
  }

  public static void listDirectory() {

    try {
      connect();

      channelSftp.cd(SFTPWORKINGDIR);
      // System.out.println("Changed the directory...");

      Vector<LsEntry> fileList = channelSftp.ls(".");

      for (LsEntry lsEntry : fileList) {
        System.out.println(lsEntry.getLongname());
      }

    } catch (JSchException e) {
      e.printStackTrace();
    } catch (SftpException e) {
      e.printStackTrace();
    } finally {
      disconnect();
    }
  }

  public static void listDirectoryCSV() {

    try {
      connect();

      channelSftp.cd(SFTPWORKINGDIR);

      Vector<LsEntry> fileList = channelSftp.ls(".");

      for (LsEntry lsEntry : fileList) {

        // uzmi sadržaj iz long name i kasnije iz njega izvuci polja
        String after = lsEntry.getLongname();

        SftpATTRS sftpAttrs = lsEntry.getAttrs();

        String[] fileAttributes = new String[9];

        int indexOfBlank = 0;

        for (int i = 0; i < 8; i++) {
          indexOfBlank = after.indexOf(" ");
          fileAttributes[i] = after.substring(0, indexOfBlank);
          after = after.substring(indexOfBlank).trim();
        }

        fileAttributes[8] = after;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String modifiedDate = sdf.format(new Date(sftpAttrs.getMTime() * 1000L));

        System.out.println(fileAttributes[0] + ";" + fileAttributes[1] + ";"
            + fileAttributes[2] + ";" + fileAttributes[3] + ";"
            + fileAttributes[4] + ";" + modifiedDate + ";" +
            // nećemo ove ružan je ispis
            // fileAttributes[5] + ";" + fileAttributes[6] + ";" +
            // fileAttributes[7] +
            fileAttributes[8]);
      }

    } catch (JSchException e) {
      e.printStackTrace();
    } catch (SftpException e) {
      e.printStackTrace();
    } finally {
      disconnect();
    }
  }

  static void getFile(String src, String dst) {
    try {
      connect();
      channelSftp.cd(SFTPWORKINGDIR);
      channelSftp.get(src, dst);
    } catch (JSchException e) {
      e.printStackTrace();
    } catch (SftpException e) {
      e.printStackTrace();
    } finally {
      disconnect();
    }
  }

  static void getFiles(String mask) {
    try {
      connect();
      channelSftp.cd(SFTPWORKINGDIR);

      Vector<LsEntry> fileList = channelSftp.ls(".");

      for (LsEntry lsEntry : fileList) {
        if (!lsEntry.getAttrs().isDir()) {

          if (mask.equals("*")) {
            channelSftp.get(lsEntry.getFilename(), localFileName + lsEntry.getFilename());
          }
          
          else if (lsEntry.getFilename().endsWith(mask.substring(1))) {
            channelSftp.get(lsEntry.getFilename(), localFileName + lsEntry.getFilename());
          }
        }
      }

    } catch (JSchException e) {
      e.printStackTrace();
    } catch (SftpException e) {
      e.printStackTrace();
    } finally {
      disconnect();
    }
  }

  private static void putFile(String src, String dst) {
    try {
      connect();

      channelSftp.cd(SFTPWORKINGDIR);

      channelSftp.put(src, dst);

    } catch (JSchException e) {
      e.printStackTrace();
    } catch (SftpException e) {
      e.printStackTrace();
    } finally {
      disconnect();
    }
  }

  public static void parseArguments(String[] args) throws ParseException {
    cmd = parser.parse(options, args);
    _args = args;
  }

  // used letters: a b d f g h i k l o p q r s t u v
  public static void createOptions() {
    options.addOption("l", "listDir", false, "List directory.");
    options.addOption("t", "test", false, "Test connection.");
    options.addOption("i", "help", false, "Help");
    options.addOption("a", "args", false, "List args.");

    options.addOption("h", "host", true, "Host");
    options.addOption("p", "port", true, "Port");
    
    options.addOption("u", "user", true, "User");
    options.addOption("q", "password", true, "Password");
    
    options.addOption("k", "privateKeyFile", true, "Private key location.");
    options.addOption("d", "dir", true, "Remote working directory.");

    options.addOption("s", "src", true, "File name on sftp location.");
    options.addOption("f", "trg", true, "Local file name.");

    options.addOption("g", "get", false, "Get file.");
    options.addOption("b", "put", false, "Put file.");
    options.addOption("r", "remove", false, "Remove file.");

    options.addOption("o", "csvOutput", false, "Get nice csv output for list directory.");
    options.addOption("v", "verbose", false, "Get loging from JSCH.");

    // proxy postavke
    options.addOption("x", "proxyHost", true, "Proxy host.");
    options.addOption("y", "proxyPort", true, "Proxy port.");
    options.addOption("z", "proxyType", true, "Proxy type. (ie. sockks5)");
    
    // bind interface - odabir sučelja za izlaz, 
    // ako sustav ima više, provjeri sa ifconfig -a ( broji inet redove)
    options.addOption("m", "bindHost", true, "Bind host.");
    options.addOption("n", "bindPort", true, "Bind port.");
  }

  public static void handleCommand() {
    if (cmd.hasOption("h"))
      SFTPHOST = cmd.getOptionValue("h");

    if (cmd.hasOption("p"))
      SFTPPORT = Integer.parseInt(cmd.getOptionValue("p"));

    if (cmd.hasOption("u"))
      SFTPUSER = cmd.getOptionValue("u");

    if (cmd.hasOption("q"))
      SFTPPASSWORD = cmd.getOptionValue("q");

    if (cmd.hasOption("k"))
      privateKey = cmd.getOptionValue("k");

    if (cmd.hasOption("d"))
      SFTPWORKINGDIR = cmd.getOptionValue("d");

    if (cmd.hasOption("s"))
      remoteFileName = cmd.getOptionValue("s");

    if (cmd.hasOption("f"))
      localFileName = cmd.getOptionValue("f");

    // dodali x,y,z 20210507
    // proxy - type, host, port
    if (cmd.hasOption("x"))
      proxyHost = cmd.getOptionValue("x");

    if (cmd.hasOption("y"))
      proxyPort = Integer.parseInt(cmd.getOptionValue("y"));

    if (cmd.hasOption("z"))
      proxyType = cmd.getOptionValue("z");
    
    // bind interface
    
    if (cmd.hasOption("m"))
      bindHost = cmd.getOptionValue("m");
    
    if (cmd.hasOption("n"))
      bindPort = Integer.parseInt(cmd.getOptionValue("n"));

    
    // verbose ispis
    if (cmd.hasOption("v"))
      JSch.setLogger(new MyJschLogger());

    // sftp akcije
    
    if (cmd.hasOption("l")) {
      if (cmd.hasOption("o"))
        listDirectoryCSV();
      else
        listDirectory();
    }
    
    else if (cmd.hasOption("t"))
      testConnection();

    else if (cmd.hasOption("a"))
      listArgs();

    else if (cmd.hasOption("b"))
      putFile(localFileName, remoteFileName);

    else if (cmd.hasOption("g")) {
      if (remoteFileName.startsWith("*")) {
        getFiles(remoteFileName);
      } else
        getFile(remoteFileName, localFileName);
    }

    else if (cmd.hasOption("r"))
      removeFile(remoteFileName);

    else {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("SFTP Manager " + APP_VERSION, options);
    }
  }

  private static void removeFile(String fileName) {
    try {
      connect();

      channelSftp.cd(SFTPWORKINGDIR);

      channelSftp.rm(fileName);

    } catch (JSchException e) {
      e.printStackTrace();
    } catch (SftpException e) {
      e.printStackTrace();
    } finally {
      disconnect();
    }
  }

  private static void listArgs() {
    for (String arg : _args) {
      System.out.println(arg);
    }
  }

  public static void testConnection() {

    try {
      connect();

      System.out.println("[OK]");

    } catch (JSchException e) {
      e.printStackTrace();
    } finally {
      disconnect();
    }
  }

  static void connect() throws JSchException {

    if (privateKey != null)
      jSch.addIdentity(privateKey);

    session = jSch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);

    if (SFTPPASSWORD != null)
      session.setPassword(SFTPPASSWORD);

    java.util.Properties config = new java.util.Properties();
    config.put("StrictHostKeyChecking", "no");

    session.setConfig(config);

    if (proxyHost != null && proxyType != null) {
      
      if (proxyType.equals("socks4")) {
        session.setProxy(new ProxySOCKS4(proxyHost, proxyPort));
      }
      
      else if (proxyType.equals("socks5")) {
        session.setProxy(new ProxySOCKS5(proxyHost, proxyPort));
      }
      
      else if (proxyType.equals("http")) {
        session.setProxy(new ProxyHTTP(proxyHost, proxyPort));
      }
    }
    
    if(bindHost != null && !bindHost.isEmpty()) {
      session.setSocketFactory(new MySocketFactory(bindHost, bindPort));
    }

    session.connect();

    channel = session.openChannel("sftp");
    channel.connect();

    channelSftp = (ChannelSftp) channel;
  }

  static void disconnect() {
    if (channelSftp != null) {
      channelSftp.disconnect();
      channelSftp.exit();
    }
    if (channel != null)
      channel.disconnect();

    if (session != null)
      session.disconnect();
  }
  
}
