package com.example.baang.chattingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ClientActivity extends Activity {
    private static final int PORT = 10001;
    String ip =
//            "192.168.0.4" //집
            "172.20.10.7" //폰
    ;

    // 아아 깃허브 테스트

    Socket socket;
    DataInputStream is;
    DataOutputStream os;

    TextView text_msg, tv_uname;
    EditText edit_msg;
    ScrollView scroll_v;

    String msg = "";
    String uname = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        text_msg = (TextView)findViewById(R.id.text_massage_from_server);
        edit_msg = (EditText)findViewById(R.id.edit_message_to_server);
        scroll_v = (ScrollView)findViewById(R.id.scrollView);
        tv_uname = (TextView)findViewById(R.id.tv_uname);

        Intent intent = getIntent();
        uname = intent.getStringExtra("uname");
        tv_uname.setText(" [" + uname + "] 님");

        Conn_Th conn_th = new Conn_Th();
        conn_th.start();
    }

    @Override
    public void onBackPressed() {}

    public void mOnClick(View v) {
        switch(v.getId()) {
            case R.id.btn_send_client:
                if(os == null) break;
                Send_Th send_th = new Send_Th();
                send_th.start();
                break;

            case R.id.btn_logout:
                Log_out go = new Log_out();
                go.run();
                break;
        }
    } // mOnClick method

    public class Conn_Th extends Thread {
        @Override
        public void run() {

            if (socket == null) {
                try {
                    socket = new Socket(InetAddress.getByName(ip), PORT);
                    is = new DataInputStream(socket.getInputStream());
                    os = new DataOutputStream(socket.getOutputStream());

                    os.writeUTF(uname);
                    os.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            while (true) {
                try {
                    msg = is.readUTF();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            text_msg.append(msg);
                            scroll_v.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                } catch (IOException e) {
                    break;
                }
            }
            if(is!=null)
                try {is.close();} catch(Exception e) {}

        }// run method
    }

    public class Send_Th extends Thread {
        public void run() {
            String msg = edit_msg.getText().toString();

            if(msg.equals("") || msg==null) return;

            try {
                os.writeUTF(msg);
                os.flush();
                // 에딧 텍스트 비우기
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        edit_msg.setText("");
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } //run method__
    }

    public class Log_out extends Thread {
        public void run() {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    new ContextThemeWrapper(ClientActivity.this, R.style.AlertDialogCustom));

            // 제목셋팅
            alertDialogBuilder.setTitle("로그아웃");

            // AlertDialog 셋팅
            alertDialogBuilder
                    .setMessage("로그아웃 하시겠습니까?")
                    .setCancelable(false)
                    .setPositiveButton("네",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialog, int id) {
                                    // 로그아웃한다
                                    if(is != null) try {is.close();} catch(Exception e) {}
                                    if(os != null) try {os.close();} catch(Exception e) {}
                                    if(socket != null) try {socket.close();} catch(Exception e) {}
                                    finish();
                                }
                            })
                    .setNegativeButton("아니오",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialog, int id) {
                                    // 다이얼로그를 취소한다
                                    dialog.cancel();
                                }
                            });
            // 다이얼로그 생성
            AlertDialog alertDialog = alertDialogBuilder.create();
            // 다이얼로그 보여주기
            alertDialog.show();
        }
    }
}
