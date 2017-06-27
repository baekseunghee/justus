package com.example.baang.chattingapp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
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

public class Login extends Activity {
    private SendPostRequest mySPRequest;
    private EditText idEdit, pwEdit;
    private String id, pw, uname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        idEdit = (EditText) findViewById(R.id.edit_id);
        pwEdit = (EditText) findViewById(R.id.edit_pw);
    }

    public void mOnClick(View v) {
        switch(v.getId()) {
            case R.id.login_btn:
                mySPRequest = new SendPostRequest();
                id = idEdit.getText().toString();
                pw = pwEdit.getText().toString();

                mySPRequest.execute();
                break;
            case R.id.joining_btn:
                Intent intent = new Intent(this, JoiningActivity.class);
                startActivity(intent);
                break;
        }
    }

    public class SendPostRequest extends AsyncTask<String, Void, String> {
        String data;

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... arg0) {
            try {
                URL url = new URL("http://172.20.10.7:8080/Chatting/Chat/login2");

                // 데이터 입력
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("id", id);
                postDataParams.put("pw", pw);
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

                    JSONObject json = new JSONObject(page);
                    JSONArray jArr = json.getJSONArray("u_info");
                    JSONObject obj = jArr.getJSONObject(0);

                    uname = obj.get("u_name").toString();

                    return data = "success";
                } else {
                    return data = "failure";
                }

            } catch (Exception e) {
                return data = "exception";
            }
        }

        // post 응답 후
        @Override
        protected void onPostExecute(String result) {
            if(data.equals("success")) {
                Intent intent = new Intent(Login.this, ReadyActivity.class);
                intent.putExtra("uname", uname);
                startActivity(intent);
                idEdit.setText("");
                pwEdit.setText("");
                //finish();
            } else {
                Toast.makeText(getApplicationContext(), "Login Failure!", Toast.LENGTH_SHORT).show();
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

            return result.toString();
        }
    }
}