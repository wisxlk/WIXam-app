package com.example.wixam;


import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class VerfierCours extends AppCompatActivity {


    private NfcAdapter nfcAdapter;
    private TextView boite;
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_verfier_cours);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        StrictMode.setThreadPolicy(policy);
        boite = findViewById(R.id.boite);

        boite.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                boite.setBackgroundColor(R.color.white);
            }
        });
    }

    private void handleIntent(@NonNull Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);
            } else {
                Log.d(TAG, "Wrong mime type: " + type); }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();
            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break; } }
        } else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            new NdefReaderTask().execute(tag);
            Toast.makeText(this, byteArrayToHex(tag.getId()), Toast.LENGTH_SHORT).show();
        }
    }



    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setupForegroundDispatch(this, nfcAdapter);
    }

    @Override
    protected void onPause() {

        stopForegroundDispatch(this, nfcAdapter);

        super.onPause();
    }

    public static void setupForegroundDispatch(final VerfierCours activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[3];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[1] = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        filters[2] = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    public static void stopForegroundDispatch(final VerfierCours activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private String verifieretudiant(String idetudiant) {
        String res = null;
        URL url;
        try {

            String ulr = "http://" + MainActivity.IP + "verifieretudiant.php/? Idetudiant=" + URLEncoder.encode(String.valueOf(idetudiant), "UTF-8")+"&enseignant="+ URLEncoder.encode(String.valueOf(ConnectActivity.userEnseignant), "UTF-8");

            url = new URL(ulr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            String response = readStream(con.getInputStream());
            //System.out.println("verifierentree : " + response);
            res = response.trim();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
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

    @SuppressLint("StaticFieldLeak")
   private class  NdefReaderTask extends AsyncTask <Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];


            String tagId = null;
            String res = "0";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                tagId = byteArrayToHex(tag.getId());
                tagId = convertir(tagId);
            }


            if (tagId != null) {
                res = verifieretudiant(tagId);

            }else System.out.println("carte non valide");

            return res.trim();
        }


        private String convertir(String tagId) {
            byte[] messageDigest = new byte[0];
            String tagchaine = tagId.substring(6, 8) + tagId.substring(4, 6) + tagId.substring(2, 4) + tagId.substring(0, 2);
            BigInteger tagb = new BigInteger(tagchaine, 16);
            String tagstring = String.valueOf(tagb);
            byte[] byteChaine = tagstring.getBytes();
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("MD5");
                messageDigest = md.digest(byteChaine);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }

        @Override
        protected void onPostExecute(String res) {


            if (res.equals("0")) {
                    //System.out.println("onPostExecute : " + "green");
                boite.setBackgroundColor(Color.rgb(189,183,107));
                    boite.setText(R.string.accepte);
                    Toast.makeText(VerfierCours.this, res, Toast.LENGTH_SHORT).show();
                } else if (res.equals("1")) {
                    //System.out.println("onPostExecute : " + "red");
                boite.setBackgroundColor(Color.rgb(176,53,53) );
                    boite.setText(R.string.etudiantInconnu);
                    Toast.makeText(VerfierCours.this, res, Toast.LENGTH_SHORT).show();



        }




}}}