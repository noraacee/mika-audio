package com.mikaaudio.server.util;

import java.io.DataOutputStream;
import java.io.IOException;

public class SuperUserManager {
    private static final String COMMAND_EXIT = "exit\n";
    private static final String COMMAND_INPUT_KEY_EVENT = "input keyevent ";
    private static final String SUPER_USER = "su";

    private DataOutputStream shell;
    private Process su;

    public SuperUserManager() throws IOException {
        su = Runtime.getRuntime().exec(SUPER_USER);
        shell = new DataOutputStream(su.getOutputStream());
    }

    public void inputKeyEvent(int key) throws IOException {
        runCommand(COMMAND_INPUT_KEY_EVENT + key + "\n");
    }

    public void onDestroy() {
        try {
            shell.writeBytes(COMMAND_EXIT);
            shell.flush();
            su.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void runCommand(String cmd) throws IOException {
        shell.writeBytes(cmd);
        shell.flush();
    }
}
