package com.mikaaudio.server.util;

import java.io.DataOutputStream;
import java.io.IOException;

public class SuperUserManager {
    private static final String COMMAND_EXIT = "exit\n";
    private static final String COMMAND_INPUT_KEYEVENT = "input keyevent ";
    private static final String SUPER_USER = "su";

    private DataOutputStream shell;
    private Process su;

    public SuperUserManager() throws IOException {
        su = Runtime.getRuntime().exec(SUPER_USER);
        shell = new DataOutputStream(su.getOutputStream());
    }

    public void inputKeyevent(int key) throws IOException {
        runCommand(COMMAND_INPUT_KEYEVENT + key + "\n");
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

    private void runCommand(String cmd) throws IOException {
        shell.writeBytes(cmd);
        shell.flush();
    }
}
