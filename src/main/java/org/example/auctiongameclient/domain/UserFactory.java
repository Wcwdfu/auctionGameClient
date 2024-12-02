package org.example.auctiongameclient.domain;

import java.net.Socket;

public class UserFactory {

    public static User MY_INSTANCE;

    public static Socket MY_SOCKET;

    public static void initialize(User user,Socket socket){
        MY_INSTANCE=user;
        MY_SOCKET=socket;
    }
}
