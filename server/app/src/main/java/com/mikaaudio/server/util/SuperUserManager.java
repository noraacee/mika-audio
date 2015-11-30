package com.mikaaudio.server.util;

public class SuperUserManager {
    private static SuperUserManager instance;

    static {
        instance = new SuperUserManager();
    }

    private SuperUserManager() {

    }
}
