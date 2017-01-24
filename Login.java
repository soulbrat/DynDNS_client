package com.my;

public class Login {

    private static int count;

    public static boolean authenticate(String username, String password, ClientDDNS clientDDNS) {

        boolean isAuth = false;

        try {
            String line;

            String passwordMD5 = Helper.md5(password);
            Helper.writeMessage("MD5 in hex: " + Helper.md5(password));

            Helper.writeMessage("Send auth data");

            clientDDNS.writeMessage(username + " : " + passwordMD5);

            Helper.writeMessage("Wait response from auth server");

            line = clientDDNS.readInput(); // ждем пока сервер отошлет строку текста.

            if (line.equals("Authentication successfully!")) {
                isAuth = true;
                count = 0;
                Manager.username = username;
                Manager.password = password;
            } else {
                count++;
                Helper.writeMessage("Authentication failed!");
                clientDDNS.getSocket().close();
                clientDDNS.setSocket(null);
                clientDDNS.createSocket();
                if (count == 4){
                    Helper.writeMessage("Close program!");
                    System.exit(0);
                }
            }

        } catch (Throwable throwable) {
            Helper.writeMessage("Login class Exception");
            Helper.writeMessage(throwable.toString());
        }

        return isAuth;
    }

}
