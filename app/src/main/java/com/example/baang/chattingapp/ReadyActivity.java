package com.example.baang.chattingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;


public class ReadyActivity extends Activity {
    private SendPostRequest change_name_request;
    Intent intent;
    String uname = "";
    String ch_name = "";
    TextView tv_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready);

        intent = getIntent();
        uname = intent.getStringExtra("uname");

        tv_name = (TextView)findViewById(R.id.tv_ready_name);
        tv_name.setText("[" + uname + "] 님");
    }

    public void mOnClick(View v) {
        switch(v.getId()) {
            case R.id.btn_go:
                Intent intent = new Intent(this, ClientActivity.class);
                intent.putExtra("uname", uname);
                startActivity(intent);
                finish();
                break;
            case R.id.btn_change_name:
                AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));

                alert.setTitle("NEW NAME");
                alert.setMessage("Input your new name.");

                final EditText name = new EditText(this);
                name.setTextColor(Color.WHITE);
                alert.setView(name);

                alert.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ch_name = name.getText().toString();
                        //여기여기여기여기------------------------------------------
                        if(ch_name.length() > 6) {
                            Toast.makeText(getApplicationContext(), "닉네임은 6자 이하입니다", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        change_name_request = new SendPostRequest();
                        change_name_request.execute();
                    }
                });


                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });

                alert.show();
                break;
            case R.id.btn_exit:
                finish();
                break;
        }
    }

    public class SendPostRequest extends AsyncTask<String, Void, String> {
        String result = "";

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... arg0) {
            try {
                URL url = new URL("http://172.20.10.7:8080/Chatting/Chat/change_name");

                // 데이터 입력
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("before_name", uname);
                postDataParams.put("after_name", ch_name);
                Log.e("params", postDataParams.toString());

                //url 연결
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setReadTimeout(5 * 1000);
                conn.setConnectTimeout(5 * 1000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams)); // 제이슨 string으로 변환해서 전송

                writer.flush();

                // 로그인 리턴되는 것 받기******************************************************************

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    String line = "";
                    String page = "";

                    while ((line = in.readLine()) != null) {
                        page += line;
                    }

                    if(page.toString().equals("success")){
                        result = "변경 성공!";
                    } else if(page.toString().equals("unavailable name")){
                        result = "이미 존재하는 닉네임입니다!";
                    } else {
                        result = "error";
                    }
                } else {
                    return result = "http failure";
                }
            } catch (Exception e) {
                result = "exception";
            } finally {
                return result;
            }
        }

        // post 응답 후
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), this.result, Toast.LENGTH_LONG).show();

            if(this.result.equals("변경 성공!") == true) {
                uname = ch_name;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_name.setText("[" + uname + "] 님");
                    }
                });
            }
        }

        // 제이슨을 string으로 변환해준다
        public String getPostDataString(JSONObject params) throws Exception {
            StringBuilder result = new StringBuilder();
            boolean first = true;

            Iterator<String> itr = params.keys();

            while (itr.hasNext()) {
                String key = itr.next();
                Object value = params.get(key);

                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(key, "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(value.toString(), "UTF-8"));
                // ex) id="hello",pw="bye" 이런식으로
            }

            Log.d(">>", result.toString());

            return result.toString();
        }
    }
}
