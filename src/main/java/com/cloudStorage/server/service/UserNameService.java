package com.cloudStorage.server.service;

public class UserNameService {

    private static int userId = 0;

    public String getUserName() {
        userId++;
        return "user" + userId;
    }

}
