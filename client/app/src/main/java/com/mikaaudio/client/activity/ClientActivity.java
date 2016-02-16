package com.mikaaudio.client.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikaaudio.client.R;
import com.mikaaudio.client.interf.OnDispatchKeyEventListener;
import com.mikaaudio.client.interf.UICallbackListener;
import com.mikaaudio.client.manager.ModuleManager;
import com.mikaaudio.client.manager.P2PManager;
import com.mikaaudio.client.manager.StatusManager;
import com.mikaaudio.client.module.InputModule;
import com.mikaaudio.client.widget.InterceptKeyEventLinearLayout;


public class ClientActivity extends Activity implements UICallbackListener {
    private boolean sendViewShown;

    private Button apps;
    private Button back;
    private Button click;
    private Button down;
    private Button frame;
    private Button home;
    private Button input;
    private Button keyboard;
    private Button left;
    private Button right;
    private Button send;
    private Button string;
    private Button toggle;
    private Button up;
    private EditText sendView;
    private ImageView display;
    private InterceptKeyEventLinearLayout contentView;

    private ModuleManager moduleManager;
    private InputMethodManager inputMethodManager;
    private P2PManager p2pManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

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

        click = (Button) findViewById(R.id.click);
        click.setOnClickListener(new View.OnClickListener() {
            @Override
        public void onClick(View v) {
                moduleManager.getInputModule().sendInput(InputModule.KEY_CLICK);
            }
        });

        back = (Button) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
        public void onClick(View v) {
                moduleManager.getInputModule().sendInput(InputModule.KEY_BACK);
            }
        });

        home = (Button) findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
        public void onClick(View v) {
                moduleManager.getInputModule().sendInput(InputModule.KEY_HOME);
            }
        });

        apps = (Button) findViewById(R.id.apps);
        apps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moduleManager.getInputModule().sendInput(InputModule.KEY_APPS);
            }
        });

        up = (Button) findViewById(R.id.up);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moduleManager.getInputModule().sendInput(InputModule.KEY_UP);
            }
        });

        down = (Button) findViewById(R.id.down);
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moduleManager.getInputModule().sendInput(InputModule.KEY_DOWN);
            }
        });

        left = (Button) findViewById(R.id.left);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moduleManager.getInputModule().sendInput(InputModule.KEY_LEFT);
            }
        });

        right = (Button) findViewById(R.id.right);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moduleManager.getInputModule().sendInput(InputModule.KEY_RIGHT);
            }
        });

        keyboard = (Button) findViewById(R.id.keyboard);
        keyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendViewShown) {
                    sendView.setVisibility(View.GONE);
                    send.setVisibility(View.GONE);
                    sendViewShown = false;
                    sendView.clearFocus();
                }

                contentView.requestFocus();

                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });

        string = (Button) findViewById(R.id.string);
        string.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendViewShown) {
                    sendView.setVisibility(View.GONE);
                    send.setVisibility(View.GONE);
                    sendViewShown = false;
                    sendView.clearFocus();
                } else {
                    sendView.setVisibility(View.VISIBLE);
                    send.setVisibility(View.VISIBLE);
                    sendViewShown = true;
                    sendView.requestFocus();
                }
            }
        });

        sendView = (EditText) findViewById(R.id.message_to_send);

        send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moduleManager.getInputModule().sendInput(sendView.getText().toString());
                sendView.setText("");
            }
        });

        toggle = (Button) findViewById(R.id.toggle);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moduleManager.getFrameModule().start();
            }
        });

        input = (Button) findViewById(R.id.input);
        input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moduleManager.switchModule(ModuleManager.MODULE_INPUT);
            }
        });

        frame = (Button) findViewById(R.id.frame);
        frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moduleManager.switchModule(ModuleManager.MODULE_FRAME);
            }
        });

        display = (ImageView) findViewById(R.id.display);

        inputMethodManager = (InputMethodManager) ClientActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);

        moduleManager = new ModuleManager(this);

        p2pManager = new P2PManager(this, moduleManager);
        p2pManager.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        p2pManager.onDestroy();
        moduleManager.onDestroy();
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
        string.setEnabled(enabled);
        sendView.setEnabled(enabled);
        send.setEnabled(enabled);
    }

    @Override
    public void onConnect() {
        input.setEnabled(true);
        frame.setEnabled(true);
    }

    @Override
    public void onDisconnect() {
        input.setEnabled(false);
        frame.setEnabled(false);

        setEnabled(false);
    }

    @Override
    public void onFrame(Bitmap frame) {
        display.setImageBitmap(frame);
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
