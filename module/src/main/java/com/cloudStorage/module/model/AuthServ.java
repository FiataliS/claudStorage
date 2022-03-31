package com.cloudStorage.module.model;

import java.nio.file.Path;

public class AuthServ implements CloudMessage {

    String Nick;
    String pass;
    boolean auth;

    public AuthServ(String nick, String pass, boolean auth) {
        this.Nick = nick;
        this.pass = pass;
        this.auth = auth;
    }

    public boolean getAuth() {
        return auth;
    }

    public String getNick() {
        return Nick;
    }

    public String getPass() {
        return pass;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.AUTH_SERV;
    }
}
