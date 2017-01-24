package com.my;

import javax.naming.AuthenticationException;
import javax.swing.*;
import java.io.File;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ResourceBundle;

public class Manager {

    public static final String RESOURCE_PATH = "com.my.resources.";
    private static ResourceBundle res = ResourceBundle.getBundle(RESOURCE_PATH + "settings");

    public static boolean isAuth = false;
    private static boolean firstConnection = true;
    public static String username;
    public static String password;
    public static ClientDialog clientDialog;
    public static ServerSocket serverSocket;
    public static TrayExecutor trayExecutor;


    public static void main(String[] args) throws Throwable {

        Helper.writeMessage("Start new process");
        // control of run only one copy
        // prevent run some copies of program
        try {
            serverSocket = new ServerSocket(65535, 1, InetAddress.getLocalHost());
        }catch (BindException e){
            Helper.writeMessage(e.toString());
            Helper.writeMessage("The program is already running! Exit.");
            JFrame fr1 = new JFrame();
            JOptionPane.showMessageDialog(fr1,
                    "The program is already running!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        //////////////////////////////////////////////////////////

        // re-create log file //
        File file = new File(Helper.logFile);
        if(file.exists()) {
            file.delete();
        }
        ////////////////////////

        // start tray
        trayExecutor = new TrayExecutor();
        // set vars
        String domain;
        String ip;
        String line;
        // start client socket
        ClientDDNS clientDDNS = new ClientDDNS();

        /////////////////////////////
        // AUTH block ///////////////
        // run only one time for auth
        /////////////////////////////
        if (!isAuth) {
            final JFrame frame = new JFrame("Dynamic DNS service");
            // create login dialog
            LoginDialog loginDlg = new LoginDialog(frame, clientDDNS);
            // if is autologin? try to auth - push button Login
            if (loginDlg.pushed) loginDlg.btnLogin.doClick();
            // Was autologin successful? if no - start LoginDialog
            if (!loginDlg.isSucceeded()) {
                loginDlg.setVisible(true);
            }

            if (loginDlg.isSucceeded()) {
                isAuth = true;
                clientDialog = new ClientDialog(frame, clientDDNS, username, password, loginDlg);
                // if was autoload successfully - run and hide to tray
                if (!clientDialog.isTray) {
                    clientDialog.setVisible(true);
                }
            }

            if (!loginDlg.isSucceeded()) {
                isAuth = false;
                System.exit(0);
            }

            if (!isAuth) {
                Helper.writeMessage("AUTH FAILED!");
                throw new AuthenticationException();
            }

            Helper.writeMessage("Authentication successfully!");
            firstConnection = true;

        }
        ////////////////////////////

        // general loop
        while (true) {

            try {

                domain = clientDialog.domain;
                ip = clientDialog.ip;

                if (clientDialog.start && (clientDialog.autodetectIP.isSelected())) {
                    if (!clientDialog.getIp().equals(Helper.getRealIP())) {
                        clientDialog.setIp(Helper.getRealIP());
                        ip = Helper.getRealIP();
                        Helper.writeMessage("External IP was changed! Change DNS...");
                    }
                }

                if (clientDialog.start && (!ip.equals(Helper.getNSLookupName(domain)))) {

                    // re-create socket only if it's not the first connection
                    if (!firstConnection) {
                        Helper.writeMessage("Not the first connection. Re-create client socket.");
                        clientDDNS.reCreateSocket();
                        Login.authenticate(username, password, clientDDNS);
                    }

                    // wait server
                    Thread.sleep(1000);
                    //Helper.writeMessage("Waiting for the server...");

                    // update DNS zone
                    clientDDNS.writeMessage(ip + " : " + domain);
                    Helper.writeMessage("Send new IP and domain: " + ip + " : " + domain);

                    // wait answer from server
                    line = clientDDNS.readInput();
                    Helper.writeMessage("ANSWER from server : " + line);
                    clientDialog.setAnswer(line);

                    Thread.sleep(3000);

                } else if (clientDialog.start) {
                    line = "Zone updated.";
                    //Helper.writeMessage("Zone updated.");
                    clientDialog.setAnswer(line);
                    Thread.sleep(3000);

                } else {
                    //Helper.writeMessage("Do nothing...");
                    Thread.sleep(3000);
                }

            } catch (Exception e) {
                Helper.writeMessage("General Exception");
                Helper.writeMessage(e.toString());
                Thread.sleep(3000);
                firstConnection = false;
                clientDialog.setAnswer("error");
            }
            firstConnection = false;

        } // general loop
    }
}
