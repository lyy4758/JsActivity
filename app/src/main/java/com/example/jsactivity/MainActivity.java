package com.example.jsactivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "JSActivity";
    private WebView webView;
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            int index = msg.arg1;
            MainActivity.this.setProgress(index * 1000);
        }
    };
    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new Object() {
            @SuppressWarnings("unused")
            public List<String> getList() {
                List<String> list = new ArrayList<>();
                for (int i = 0;i <= 10; i++) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e)  {
                        Log.e(TAG,"error:" + e.getMessage());
                    }
                    list.add("current index is:" + i);
                    Message msg = handler.obtainMessage();
                    msg.arg1 = i;
                    handler.sendMessage(msg);
                }
                success();
                return list;
            }
            public void success() {
                webView.loadUrl("javascript:success('congratulations')");
            }
        },"bridge");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("alert")
                        .setMessage(message)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        }).create().show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view,String url, String message,final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("confirm")
                        .setMessage(message)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MainActivity.this,"you click yes",Toast.LENGTH_SHORT).show();
                                result.confirm();
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.cancel();
                            }
                        }).create().show();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String messag, String defaultValue, final JsPromptResult result) {
                LayoutInflater inflater = getLayoutInflater();
                View prompt = inflater.inflate(R.layout.prompt,null);
                final EditText text = (EditText) prompt.findViewById(R.id.prompt_input);
                text.setHint(defaultValue);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("prompt")
                        .setView(prompt)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm(text.getText().toString());
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.cancel();
                            }
                        }).create().show();
                return true;
            }
        });
        webView.loadUrl("file:///android_asset/index.html");
        setContentView(webView);
    }
}
