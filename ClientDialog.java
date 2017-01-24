package com.my;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.*;

import static com.my.Manager.RESOURCE_PATH;

public class ClientDialog extends JFrame {

    private static ResourceBundle res = ResourceBundle.getBundle(RESOURCE_PATH + "settings");

    private JTextField tfDomain;
    private JTextField tfIP;
    private JLabel lbDomain;
    private JLabel lbIP;
    private JLabel lbStatus;
    private static JLabel lbStatusLog;
    private JLabel lbAnswer;
    private static JLabel lbAnswerText;
    private JButton btnStart;
    private JButton btnStop;
    public static String domain;
    public static String ip;
    private ClientDDNS clientDDNS;
    private JCheckBox remember;
    public JCheckBox autodetectIP;
    public static boolean start = false;
    public static boolean isTray = false;

    public static final String APPLICATION_NAME = res.getString("APPLICATION.NAME");

    public ClientDialog(Frame parent, final ClientDDNS clientDDNS, final String username, final String password, LoginDialog loginDialog) {

        super("Dynamic DNS service");

        // icon for window
        URL iconURL = getClass().getResource(res.getString("ICON.STR.TRAY128"));
        ImageIcon icon = new ImageIcon(iconURL);
        setIconImage(icon.getImage());
        //

        this.clientDDNS = clientDDNS;

        // create the first block
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;
        cs.ipadx = 30;
        //cs.ipady = 20;

        // create border for the first block
        //Border border1 = BorderFactory.createEtchedBorder();
        //panel.setBorder(border1);
        Border border1 = BorderFactory.createEtchedBorder();
        Border titled1 = BorderFactory.createTitledBorder(border1, "Main");
        panel.setBorder(titled1);

        // fill in the first block
        lbStatus = new JLabel("Program: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridheight = 1;
        cs.gridwidth = 1;
        //panel.add(lbStatus, cs);

        lbStatusLog = new JLabel();
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridheight = 1;
        cs.gridwidth = 1;
        panel.add(lbStatusLog, cs);

        lbDomain = new JLabel("Domain: ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridheight = 1;
        cs.gridwidth = 1;
        panel.add(lbDomain, cs);

        tfDomain = new JTextField(15);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridheight = 1;
        cs.gridwidth = 1;
        panel.add(tfDomain, cs);

        lbIP = new JLabel("IP-address: ");
        cs.gridx = 0;
        cs.gridy = 2;
        cs.gridheight = 1;
        cs.gridwidth = 1;
        panel.add(lbIP, cs);

        tfIP = new JTextField(15);
        cs.gridx = 1;
        cs.gridy = 2;
        cs.gridheight = 1;
        cs.gridwidth = 1;
        panel.add(tfIP, cs);

        //auto detect IP
        autodetectIP = new JCheckBox("Auto detect IP");
        cs.gridx = 0;
        cs.gridy = 4;
        cs.gridheight = 1;
        cs.gridwidth = 1;
        panel.add(autodetectIP, cs);

        //save settings
        remember = new JCheckBox("Remember me");
        cs.gridx = 0;
        cs.gridy = 6;
        cs.gridheight = 1;
        cs.gridwidth = 1;
        panel.add(remember, cs);

        // if was autoload
        if (loginDialog.remember.isSelected()){
            this.remember.setSelected(true);
            try {
                this.remember.setSelected(true);
                String[] loadData = Helper.loadDomainFromFile().split(" : ");
                tfDomain.setText(loadData[0]);
                tfIP.setText(loadData[1]);
                String detectIp = loadData[2];
                if (detectIp.equals("yes")) {
                    autodetectIP.setSelected(true);
                    tfIP.setText(Helper.getRealIP());
                    tfIP.setEditable(false);
                } else autodetectIP.setSelected(false);
                // run program
                setStart();
                // if was autoload successfully - run and hide to tray
                isTray = true;
            }catch (NullPointerException e)
            {
                // do noting
            }
        }

        autodetectIP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (autodetectIP.isSelected()){
                    tfIP.setText(Helper.getRealIP());
                    tfIP.setEditable(false);
                } else
                {
                    tfIP.setText("");
                    tfIP.setEditable(true);
                }
            }
        });

        btnStart = new JButton("Start/Update");
        btnStart.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!Helper.checkIP(tfIP.getText())){
                    JOptionPane.showMessageDialog(ClientDialog.this,
                            "Invalid IP-address!",
                            "IP-address",
                            JOptionPane.ERROR_MESSAGE);
                } else if( (tfDomain.getText().length() <= 0) || (tfDomain.getText().contains(",")) || (!tfDomain.getText().contains(".")) ) {
                    JOptionPane.showMessageDialog(ClientDialog.this,
                            "Invalid domain name!",
                            "domain name",
                            JOptionPane.ERROR_MESSAGE);
                } else {

                    setStart();

                    // save settings in file only when RUN
                    if (remember.isSelected()) {
                        String detectIp;
                        if (autodetectIP.isSelected()){
                            detectIp = "yes";
                        } else {detectIp = "no";}
                        Helper.saveDomainToFile(username, password, tfDomain.getText(), tfIP.getText(), detectIp);
                    } else {
                        Helper.deleteAuthFile();
                    }
                }
                //dispose();
            }
        });
        btnStop = new JButton("Stop");
        btnStop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setStop();
                //dispose();
            }
        });

        // minimizing to tray
        /*
        addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent e) {
                if (e.getNewState() == ICONIFIED) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    setVisible(false);
                }
            }
        });
        */

        JPanel bp = new JPanel();
        // create border for the third block
        Border border2 = BorderFactory.createEtchedBorder();
        bp.setBorder(border2);
        bp.add(btnStart);
        bp.add(btnStop);

        // create the second block
        JPanel pStat = new JPanel(new GridBagLayout());
        GridBagConstraints cs2 = new GridBagConstraints();

        cs2.fill = GridBagConstraints.HORIZONTAL;
        cs2.ipadx = 30;

        // create border
        Border border3 = BorderFactory.createEtchedBorder();
        Border titled = BorderFactory.createTitledBorder(border3, "Status");
        pStat.setBorder(titled);

        lbAnswer = new JLabel("  ");
        cs2.gridx = 0;
        cs2.gridy = 11;
        cs2.gridheight = 1;
        cs2.gridwidth = 1;
        pStat.add(lbAnswer, cs2);

        lbAnswerText = new JLabel();
        cs2.gridx = 1;
        cs2.gridy = 10;
        cs2.gridheight = 2;
        cs2.gridwidth = 1;
        pStat.add(lbAnswerText, cs2);

        // add all blocks
        getContentPane().add(panel, BorderLayout.PAGE_START);
        getContentPane().add(pStat, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        // упаковать окно в размер всех ячеек
        // http://java-online.ru/swing-windows.xhtml
        pack();
        // менять размер окна
        setResizable(false);
        // появление в центре
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        //setSize(400, 200);
    }

    public ClientDDNS getClientDDNS() {
        return clientDDNS;
    }

    private void setStart()
    {
        lbStatusLog.setForeground(Color.green.darker());
        lbStatusLog.setText("RUN");
        start = true;
        domain = tfDomain.getText();
        ip = tfIP.getText();
        TrayExecutor.trayIcon.displayMessage(APPLICATION_NAME, "ddns client running!", TrayIcon.MessageType.INFO);
    }

    private static void setStop()
    {
        lbStatusLog.setForeground(Color.red);
        lbStatusLog.setText("STOPPED");
        start = false;
        TrayExecutor.trayIcon.displayMessage(APPLICATION_NAME, "ddns client stopped!", TrayIcon.MessageType.ERROR);
    }

    public static void setAnswer(String line){
        if (line.contains("Denied") || line.contains("NOT correct")  || line.contains("error")) {
            lbAnswerText.setText("Denied update! Check domain name.");
            lbAnswerText.setForeground(Color.red);
            setStop();
        } else {
            lbAnswerText.setText(line);
            lbAnswerText.setForeground(Color.blue);
        }
    }

    private static final java.util.List<Image> ICONS = Arrays.asList(
            //new ImageIcon("icon_16.png").getImage(),
            //new ImageIcon("icon_32.png").getImage(),
            //new ImageIcon("icon_64.png").getImage());
            new ImageIcon(res.getString("ICON.STR")).getImage());

    public String getIp() {
        return tfIP.getText();
    }

    public void setIp(String ip) {
        tfIP.setText(ip);
    }
}


