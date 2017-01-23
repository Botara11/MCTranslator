package com.kenso.alv.mc_translator;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;


//import com.google.android.gms.location.LocationListener;


/**
 * Created by alv on 5/12/16.
 */


public class Translator extends AsyncTask<String, Void, String> {
    private String TAG = "Translator";

    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }


    @Override
    protected String doInBackground(String... params) {
        // Create a new HttpClient and Post Header


        URL url = null;
        try {
            String nuevaString = URLEncoder.encode(params[0], "UTF-8");
           url = new  URL("http://translate.googleapis.com/translate_a/single?client=gtx&sl="+params[1]+"&tl="+params[2]+"&dt=t&q=" + nuevaString);
            Log.d(TAG,url.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        URLConnection urlConnection = null;
        try {
            urlConnection = url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String result = br.readLine();
            Log.d(TAG,result);
            br.close();

            String aux = result.replace("[[[\"","");
            Log.d(TAG,aux);
            if (aux.indexOf("\",\"") != -1) {
                aux = aux.substring(0,aux.indexOf("\",\""));
                return aux;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

