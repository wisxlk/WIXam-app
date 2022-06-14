package com.example.wixam;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class ConnectActivity extends AppCompatActivity {
    static String userEnseignant=null;
    private Button btn_1;

    //declare variables
    boolean passwordVisible;
    private boolean res=false;
    private NfcAdapter nfcAdapter;

    //private EditText user;

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";
    String[] ids = new String[2];
    EditText username=null;
    EditText password=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_connect);
        username = (EditText) findViewById(R.id.user);
        password = (EditText) findViewById(R.id.pass);

        //pass visible
        password.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int Right=2;
                if(event.getAction()==MotionEvent.ACTION_UP ){
                    if(event.getRawX()>=password.getRight()-password.getCompoundDrawables()[Right].getBounds().width()){
                        int selection=password.getSelectionEnd();
                        if(passwordVisible) {
                            //set drw img here
                            password.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_visibility_off_24, 0);
                            //for hide pass
                            password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            passwordVisible = false;
                        }else{
                            password.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_visibility_24, 0);
                            //for show pass
                            password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            passwordVisible = true;
                        }
                        password.setSelection(selection);
                        return true;
                    }
                }
                return false;
            }
        });

        btn_1=findViewById(R.id.btn_1);
        btn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (username.getText().toString().equals("") || password.getText().toString().equals("")) {
                    Toast.makeText(ConnectActivity.this, "USERNAME OU PASSWORD MANQUANT", Toast.LENGTH_SHORT).show();

                } else {
                    userEnseignant = username.getText().toString();
                    System.out.println(userEnseignant);
                    int reponse = verifierUser(username.getText().toString(),  password.getText().toString());
                    System.out.println("reponse ="+reponse);
                    if (reponse == 0) allerpageuivante();    else message();
                }
            }


        });


    }


    private void allerpageuivante() {
        Intent intent=new Intent(ConnectActivity.this,ChooseActivity.class);
        startActivity(intent);
    }
    private void message() {
        Toast.makeText(ConnectActivity.this, "PASSWORD OR USERNAME INVALIDE!!!", Toast.LENGTH_SHORT).show();


    }
    private String readStream(InputStream in) {
                BufferedReader reader = null;
                StringBuilder page = new StringBuilder();
                try {
                    reader = new BufferedReader(new InputStreamReader(in));
                    String line;

                    while ((line = reader.readLine()) != null) page.append(line);
                    System.out.println("resultat page =" + page);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return page.toString();
                }
            }

            private int verifierUser(String User, String pass) {
                int res = 0;
                URL url = null;
                try {
                    String ulr = "http://" + MainActivity.IP + "connexion.php/?username=" + URLEncoder.encode(User, "UTF-8") + "&password=" + URLEncoder.encode(pass, "UTF-8");
                    System.out.println(ulr);
                    url = new URL(ulr);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    String response = readStream(con.getInputStream());
                    System.out.println("message reçu par readstream = "+response);
                    if (!response.trim().equals("0")) res = 1;
                    else res = 0;

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return res;

            }



}