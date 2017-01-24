package com.my;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import sun.net.util.IPAddressUtil;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.regex.Pattern;

import static com.my.ClientDialog.isTray;
import static com.my.Manager.*;

public class Helper {

    private static ResourceBundle res = ResourceBundle.getBundle(RESOURCE_PATH + "settings");
    public static final String currentDir = System.getProperty("user.dir");
    public static final String iniFile = currentDir + res.getString("FILE.INI");
    public static final String logFile = currentDir + res.getString("FILE.LOG");

    /* crypt */
    private static String encodeKey = res.getString("ENCODE.KEY");
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static BASE64Encoder enc = new BASE64Encoder();
    private static BASE64Decoder dec = new BASE64Decoder();
    /* crypt */


    public static void writeMessage(String line)
    {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String message = dateFormat.format(date) + ": " + line;
        System.out.println(message);
        saveLog(message);
    }

    public static String getRealIP() {

        URL whatismyip = null;
        String ip = null;
        try {
            whatismyip = new URL(res.getString("URL1"));
            BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            ip = in.readLine(); //you get the IP as a String

        } catch (Exception e) {
            writeMessage(e.toString());
        }
        return ip;
    }

    public static String md5(String input) throws NoSuchAlgorithmException {

        String md5 = null;
        if(null == input) return null;

        try {
            //Create MessageDigest object for MD5
            MessageDigest digest = MessageDigest.getInstance("MD5");
            //Update input string in message digest
            digest.update(input.getBytes(), 0, input.length());
            //Converts message digest value in base 16 (hex)
            md5 = new BigInteger(1, digest.digest()).toString(16);

        } catch (NoSuchAlgorithmException e) {

            e.printStackTrace();
        }
        return md5;
    }

    public static String getNSLookupName(String domain){

        InetAddress address = null;
        try {
            address = InetAddress.getByName(domain);
        } catch (UnknownHostException e) {
            writeMessage(e.toString());
        }
        return address.getHostAddress();
    }

    public static void saveAuthDataToFile(String username, String password)
    {
        try {
            File file = new File(iniFile);
            if(!file.exists()) file.createNewFile();  //if the file !exist create a new one

            /* crypt all data */
            username = encodeString(username);
            password = encodeString(password);
            /* crypt all data */

            BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsolutePath()));
            bw.write(username); //write the name
            bw.newLine(); //leave a new Line
            bw.write(password); //write the password
            bw.close(); //close the BufferdWriter
        } catch (Exception e) {
            writeMessage(e.toString());
        }
    }

    public static String loadAuthDataFromFile(){
        String result = null;
        try {
            File file = new File(iniFile);
            if(file.exists()){    //if this file exists

                Scanner scan = new Scanner(file);   //Use Scanner to read the File
                String username = scan.nextLine();  //append the text to name field
                String password = scan.nextLine(); //append the text to password field
                scan.close();

                /* decode all data */
                username = decodeString(username);
                password = decodeString(password);
                /* decode all data */

                result = username + " : " + password;
            }

        } catch (FileNotFoundException e) {
            Helper.writeMessage(e.toString());
        }
        return result;
    }

    public static void saveDomainToFile(String username, String password, String domain, String IP, String detectIp)
    {
        try {
            File file = new File(iniFile);
            if(!file.exists()) file.createNewFile();  //if the file !exist create a new one

            /* crypt all data */
            username = encodeString(username);
            password = encodeString(password);
            String data = domain + " : " + IP + " : " + detectIp;
            data = encodeString(data);
            /* crypt all data */

            BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsolutePath()));
            bw.write(username);
            bw.newLine();
            bw.write(password);
            bw.newLine();
            //bw.write(domain + " : " + IP + " : " + detectIp);
            bw.write(data);
            bw.close(); //close the BufferdWriter
        } catch (Exception e) {
            writeMessage(e.toString());
        }
    }

    public static String loadDomainFromFile(){
        String result = null;
        try {
            File file = new File(iniFile);
            if(file.exists()){    //if this file exists
                Scanner scan = new Scanner(file);   //Use Scanner to read the File
                String passUsername = scan.nextLine(); // pass the first line
                String passPassword = scan.nextLine(); // pass the second line
                result = scan.nextLine();  //append the text to name field
                scan.close();

                /* decode all data */
                result = decodeString(result);
                /* decode all data */

                return result;
            }

        } catch (FileNotFoundException e) {
            Helper.writeMessage(e.toString());
        }
        return result;
    }

    public static void deleteAuthFile(){
        try {
            File file = new File(iniFile);
            file.delete();
        }catch (Exception e){
            writeMessage(e.toString());
        }
    }

    public static boolean checkIP(String ipAddressString) {
        final Pattern PATTERN_IP = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
        boolean isIP = false;
        if (PATTERN_IP.matcher(ipAddressString).matches()) {
            isIP = IPAddressUtil.isIPv4LiteralAddress(ipAddressString);
        }
        return isIP;
    }

    public static void saveLog(String line)
    {
        //Date date = new Date();
        //DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        File file = new File(logFile);
        try {
            if(!file.exists()){
                file.createNewFile();
            }
            FileOutputStream fop = new FileOutputStream(file,true);
            if(line!=null) {
                //line = "\n" + dateFormat.format(date) + ": " + line;
                fop.write(line.getBytes());
                fop.write("\n".getBytes());
            }
            fop.flush();
            fop.close();

        }catch (Exception e){
            writeMessage(e.toString());
            saveLog(e.toString());
        }
    }

    /* crypt */
    public static String base64encode(String text) {
            try {
                return enc.encode(text.getBytes(DEFAULT_ENCODING));
            } catch (UnsupportedEncodingException e) {
                return null;
            }
    }//base64encode

    public static String base64decode(String text) {
            try {
                return new String(dec.decodeBuffer(text), DEFAULT_ENCODING);
            } catch (IOException e) {
                return null;
            }
    }//base64decode

    public static String xorMessage(String message, String key) {
        try {
            if (message == null || key == null) return null;

            char[] keys = key.toCharArray();
            char[] mesg = message.toCharArray();

            int ml = mesg.length;
            int kl = keys.length;
            char[] newmsg = new char[ml];

            for (int i = 0; i < ml; i++) {
                newmsg[i] = (char)(mesg[i] ^ keys[i % kl]);
            }//for i

            return new String(newmsg);
        } catch (Exception e) {
            return null;
        }
    }//xorMessage

    public static String encodeString(String line) {
        //System.out.println("Line for encode: " + line);
        String line1 = Helper.xorMessage(line, encodeKey);
        String encodedLine = Helper.base64encode(line1);
        //System.out.println("Encoded line: " + encodedLine);
        return encodedLine;
    }
    public static String decodeString(String line){
        //System.out.println("Line for decode: " + line);
        String line1 = Helper.base64decode(line);
        String decodedLine = Helper.xorMessage(line1, encodeKey);
        //System.out.println("Decoded line: " + decodedLine);
        return decodedLine;
    }
    /* crypt */

    public static void restartProgram(){
        try {

            // del auth file
            Helper.deleteAuthFile();

            // close socket
            serverSocket.close();

            // close client
            clientDialog.setVisible(false);
            clientDialog = null;

            // close tray
            trayExecutor.tray.remove(trayExecutor.trayIcon);
            trayExecutor.tray = null;
            trayExecutor = null;
            isTray = false;

            //set auth to false for manager
            Manager.isAuth = false;

            // create new thread for main
            Thread t = new Thread(){
                @Override
                public void run(){
                    String[] args = new String[0]; // Or String[] args = {};
                    try {
                        main(args);
                    } catch (Throwable throwable) {
                        Helper.writeMessage(throwable.toString());
                    }
                }
            };
            // run new thread
            t.start();
            Helper.writeMessage("Program restarted!");

        } catch (Throwable throwable) {
            Helper.writeMessage(throwable.toString());
        }

    }

}
