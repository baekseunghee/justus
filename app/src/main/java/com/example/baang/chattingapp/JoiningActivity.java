package com.example.baang.chattingapp;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

public class JoiningActivity extends Activity {
    private SendPostRequest myJoinRequest;
    private EditText edit_id, edit_pw, edit_name;
    private String id, pw, name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joining);

        edit_id = (EditText)findViewById(R.id.edit_join_id);
        edit_pw = (EditText)findViewById(R.id.edit_join_pw);
        edit_name = (EditText)findViewById(R.id.edit_join_name);
    }

    public void mOnClick(View v) {
        switch(v.getId()) {
            case R.id.btn_join_confirm:
                myJoinRequest = new SendPostRequest();
                id = edit_id.getText().toString();
                pw = edit_pw.getText().toString();
                name = edit_name.getText().toString();

                myJoinRequest.execute();
        }
    }

    public class SendPostRequest extends AsyncTask<String, Void, String> {
        String result = "";

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... arg0) {
            if(id.isEmpty() || pw.isEmpty() || name.isEmpty())
                return result = "빈 항목을 채워주세요";
            if(id.length() > 10) return result = "아이디는 10자 이내로 해주세요.";
            if(pw.length() > 16) return result = "비밀번호는 16자 이내로 해주세요.";
            if(name.length() > 6) return result = "닉네임은 6자 이내로 해주세요.";

            try {
                URL url = new URL("http://172.20.10.7:8080/Chatting/Chat/joining");

                // 데이터 입력
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("id", id);
                postDataParams.put("pw", pw);
                postDataParams.put("name", name);
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

                // 조인 리턴되는 것 받기******************************************************************

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
                        result = "가입 성공!";
                    } else if(page.toString().equals("unavailable id")) {
                        result = "이미 존재하는 아이디입니다!";
                    } else if(page.toString().equals("unavailable name")){
                        result = "이미 존재하는 닉네임입니다!";
                    } else {
                        result = "error";
                    }
                } else {
                        result = "http failure";
                }
            } catch (Exception e) {
                result = "exception";
            } finally {
                return result;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), this.result, Toast.LENGTH_LONG).show();

            if(this.result.equals("가입 성공!") == true) {
                finish();
            }
        }

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
