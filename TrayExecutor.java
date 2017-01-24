package com.my;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ResourceBundle;

import static com.my.Manager.*;

public class TrayExecutor {

    public static TrayIcon trayIcon;
    public static SystemTray tray;

    private static ResourceBundle res = ResourceBundle.getBundle(RESOURCE_PATH + "settings");
    public static final String APPLICATION_NAME = res.getString("APPLICATION.NAME");
    public static final String ICON_STR = res.getString("ICON.STR.TRAY64");

    public TrayExecutor() {

        if(! SystemTray.isSupported() ) {
            return;
        }

        PopupMenu trayMenu = new PopupMenu();

        MenuItem exit = new MenuItem("Exit");
        MenuItem trayAction = new MenuItem("Show/Hide");
        MenuItem logout = new MenuItem("Logout");

        trayAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Manager.clientDialog.isVisible()){
                    Manager.clientDialog.setVisible(false);
                } else {
                    Manager.clientDialog.setVisible(true);
                }

            }
        });
        trayMenu.add(trayAction);

        logout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Helper.restartProgram();
            }
        });
        trayMenu.add(logout);

        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        trayMenu.add(exit);

        /*
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    System.out.println("This shows after a left-click on tray icon");
                }
            }
        });
        */

        URL imageURL = ClientDDNS.class.getResource(ICON_STR);

        Image icon = Toolkit.getDefaultToolkit().getImage(imageURL);
        trayIcon = new TrayIcon(icon, APPLICATION_NAME, trayMenu);
        trayIcon.setImageAutoSize(true);

        tray = SystemTray.getSystemTray();
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }

        //trayIcon.displayMessage(APPLICATION_NAME, "", TrayIcon.MessageType.INFO);
    }
}
