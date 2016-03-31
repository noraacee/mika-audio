package com.mikaaudio.client.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mikaaudio.client.R;
import com.mikaaudio.client.interf.OnDispatchKeyEventListener;
import com.mikaaudio.client.interf.UICallbackListener;
import com.mikaaudio.client.manager.ModuleManager;
import com.mikaaudio.client.manager.P2PManager;
import com.mikaaudio.client.manager.StatusManager;
import com.mikaaudio.client.module.InputModule;
import com.mikaaudio.client.widget.FrameView;
import com.mikaaudio.client.widget.InterceptKeyEventLinearLayout;
import com.mikaaudio.client.widget.ButtonView;


public class ClientActivity extends Activity implements UICallbackListener {
    private boolean connected;
    private boolean sendViewShown;

    private ButtonView frame;
    private ButtonView input;
    private Button toggle;
    private ButtonView apps;
    private ButtonView home;
    private ButtonView back;
    private ButtonView click;
    private ButtonView up;
    private ButtonView down;
    private ButtonView left;
    private ButtonView right;
    private ButtonView keyboard;
    private FrameView display;
    private InterceptKeyEventLinearLayout contentView;

    private ModuleManager moduleManager;
    private P2PManager p2pManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        connected = false;
        sendViewShown = false;

        StatusManager.getInstance().setStatusView((TextView) findViewById(R.id.status));

        contentView = (InterceptKeyEventLinearLayout) findViewById(R.id.content_view);
        contentView.setOnDispatchKeyEventListener(new OnDispatchKeyEventListener() {
            @Override
            public boolean onDispatchKeyEvent(KeyEvent keyEvent) {
                if (!sendViewShown && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    moduleManager.getInputModule().sendInput(keyEvent.getKeyCode());
                    return true;
                }

                return false;
            }
        });

        click = (ButtonView) findViewById(R.id.click);
        click.setOnClickListener(new View.OnClickListener() {
            @Override
        public void onClick(View v) {
                moduleManager.getInputModule().sendInput(InputModule.KEY_CLICK);
            }
        });

        back = (ButtonView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
        public void onClick(View v) {
                moduleManager.getInputModule().sendInput(InputModule.KEY_BACK);
            }
        });

        home = (ButtonView) findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
        public void onClick(View v) {
                moduleManager.getInputModule().sendInput(InputModule.KEY_HOME);
            }
        });

        apps = (ButtonView) findViewById(R.id.apps);
        apps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moduleManager.getInputModule().sendInput(InputModule.KEY_APPS);
            }
        });

        up = (ButtonView) findViewById(R.id.up);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moduleManager.getInputModule().sendInput(InputModule.KEY_UP);
            }
        });

        down = (ButtonView) findViewById(R.id.down);
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moduleManager.getInputModule().sendInput(InputModule.KEY_DOWN);
            }
        });

        left = (ButtonView) findViewById(R.id.left);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moduleManager.getInputModule().sendInput(InputModule.KEY_LEFT);
            }
        });

        right = (ButtonView) findViewById(R.id.right);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moduleManager.getInputModule().sendInput(InputModule.KEY_RIGHT);
            }
        });

        keyboard = (ButtonView) findViewById(R.id.keyboard);

        toggle = (Button) findViewById(R.id.toggle);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                display.setVisibility(View.VISIBLE);
                moduleManager.getFrameModule().start();
            }
        });

        input = (ButtonView) findViewById(R.id.input);
        input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moduleManager.switchModule(ModuleManager.MODULE_INPUT);
            }
        });

        frame = (ButtonView) findViewById(R.id.frame);
        frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moduleManager.switchModule(ModuleManager.MODULE_FRAME);
            }
        });

        display = (FrameView) findViewById(R.id.display);

        moduleManager = new ModuleManager(this, display);

        p2pManager = new P2PManager(this, moduleManager);
        p2pManager.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        p2pManager.onDestroy();
        moduleManager.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!connected)
            p2pManager.connect();
    }

    private void setEnabled(boolean enabled) {
        click.setEnabled(enabled);
        back.setEnabled(enabled);
        home.setEnabled(enabled);
        apps.setEnabled(enabled);
        up.setEnabled(enabled);
        down.setEnabled(enabled);
        left.setEnabled(enabled);
        right.setEnabled(enabled);
        keyboard.setEnabled(enabled);
    }

    @Override
    public void onConnect() {
        input.setEnabled(true);
        frame.setEnabled(true);

        connected = true;
    }

    @Override
    public void onDisconnect() {
        input.setEnabled(false);
        frame.setEnabled(false);

        setEnabled(false);

        connected = false;
    }

    @Override
    public void onModuleChanged(int module) {
        if (module == ModuleManager.MODULE_INPUT) {
            input.setEnabled(false);
            toggle.setEnabled(false);
            setEnabled(true);
        } else if (module == ModuleManager.MODULE_FRAME) {
            frame.setEnabled(false);
            toggle.setEnabled(true);
            setEnabled(false);
        }
    }
}
