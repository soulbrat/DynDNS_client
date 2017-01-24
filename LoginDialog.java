package com.my;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginDialog extends JDialog {

    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JLabel lbUsername;
    private JLabel lbPassword;
    public JButton btnLogin;
    private JButton btnCancel;
    private boolean succeeded = false;
    private ClientDDNS clientDDNS;
    public JCheckBox remember;
    public boolean pushed = false;

    public LoginDialog(Frame parent, final ClientDDNS clientDDNS) {
        super(parent, "Login DDNS client", true);
        //
        this.clientDDNS = clientDDNS;
        //
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        lbUsername = new JLabel("Username: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(lbUsername, cs);

        tfUsername = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(tfUsername, cs);

        lbPassword = new JLabel("Password: ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(lbPassword, cs);

        pfPassword = new JPasswordField(20);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(pfPassword, cs);

        //remember me
        remember = new JCheckBox("Remember me");
        cs.gridx = 2;
        cs.gridy = 2;
        cs.gridheight = 1;
        cs.gridwidth = 2;
        panel.add(remember, cs);

        panel.setBorder(new LineBorder(Color.GRAY));

        btnLogin = new JButton("Login");

        // check if is saved data for auth (remember me check box)
        try {
            String data = Helper.loadAuthDataFromFile();
            if (data != null) {
                String[] dataT = data.split(" : ");
                String username = dataT[0];
                String password = dataT[1];
                // if data is correct, fill in the form and push the button
                tfUsername.setText(username);
                pfPassword.setText(password);
                remember.setSelected(true);
                pushed = true;

            }
        }catch (Exception e){
            Helper.writeMessage("Error load auth data from file, do nothing.");
            /* do nothing */
        }
        //////////////////////////////////////////////////////////

        btnLogin.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                if (!clientDDNS.isSocketGood){
                    JOptionPane.showMessageDialog(LoginDialog.this,
                            "Cannot connect to server!",
                            "Login",
                            JOptionPane.ERROR_MESSAGE);
                    // try to re-connect
                    clientDDNS.createSocket();

                }else {

                    if (Login.authenticate(getUsername(), getPassword(), getClientDDNS())) {

                        succeeded = true;
                        dispose();

                    } else {
                        JOptionPane.showMessageDialog(LoginDialog.this,
                                "Invalid username or password",
                                "Login",
                                JOptionPane.ERROR_MESSAGE);
                        // reset username and password
                        tfUsername.setText("");
                        pfPassword.setText("");
                        remember.setSelected(false);
                        succeeded = false;
                        pushed = false;
                    }
                }
            }
        });
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel bp = new JPanel();
        bp.add(btnLogin);
        bp.add(btnCancel);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        // http://java-online.ru/swing-windows.xhtml
        // упаковать окно в размер ячеек
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    public String getUsername() {
        return tfUsername.getText().trim();
    }

    public String getPassword() {
        return new String(pfPassword.getPassword());
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public ClientDDNS getClientDDNS() {
        return clientDDNS;
    }
}