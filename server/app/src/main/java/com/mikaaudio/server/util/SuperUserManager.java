package com.mikaaudio.server.util;

import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import java.io.DataOutputStream;
import java.io.IOException;

public class SuperUserManager {
    private static final String COMMAND_EXIT = "exit\n";
    private static final String COMMAND_INPUT_KEY_EVENT = "input keyevent ";
    private static final String SUPER_USER = "su";

    private DataOutputStream shell;
    private KeyCharacterMap keyCharMap;
    private Process su;

    public SuperUserManager() throws IOException {
        keyCharMap = KeyCharacterMap.load(KeyCharacterMap.ALPHA);
        su = Runtime.getRuntime().exec(SUPER_USER);
        shell = new DataOutputStream(su.getOutputStream());
    }

    public void inputKeyEvent(int key) throws IOException {
        runCommand(COMMAND_INPUT_KEY_EVENT + key + "\n");
    }

    public void inputText(String input) throws IOException {
        KeyEvent[] keyEvents = keyCharMap.getEvents(input.toCharArray());

        for (KeyEvent ke : keyEvents) {
            if (ke!= null && ke.getAction() == KeyEvent.ACTION_UP)
                runCommand(COMMAND_INPUT_KEY_EVENT + ke.getKeyCode() + "\n");
        }
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
