package com.kenso.alv.mc_translator;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


//import com.google.android.gms.location.LocationListener;


/**
 * Created by alv on 5/12/16.
 */


public class Translator extends AsyncTask<String, Void, String> {
    private String TAG = "Translator";

    /*
    private String translate(String data){
        // Create a new HttpClient and Post Header
        String urlBase = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=en&dt=t&q=";
        urlBase = urlBase + URLEncoder.encode(data,"utf-8");
        URL url = new URL(urlBase);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            Log.d("Translator",readStream(in));

        } finally {
            urlConnection.disconnect();
        }

        return null;
    }
*/
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
            String nuevaString = params[0].replace(" ","%20");
           url = new  URL("http://translate.googleapis.com/translate_a/single?client=gtx&sl="+params[1]+"&tl="+params[2]+"&dt=t&q=" + nuevaString);
            //url = new URL("http://translate.google.com.tw/translate_a/t?client=t&hl=en&sl=" +
            //       "es" + "&tl=" + "en" + "&ie=UTF-8&oe=UTF-8&multires=1&oc=1&otf=2&ssel=0&tsel=0&sc=1&q=" +
            //        URLEncoder.encode("HOLA", "UTF-8"));
            Log.d(TAG,url.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        URLConnection urlConnection = null;
        try {
            urlConnection = url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        urlConnection.setRequestProperty("User-Agent", "Something Else");
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



        /*

        HttpURLConnection urlConnection = null ;
        //String urlBase = "http://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=en&dt=t&q=";
        String urlBase = "http://84.126.216.59:10000/q=";

        try {
            urlBase = urlBase + URLEncoder.encode(String.valueOf(params[0]),"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        URL url = null;
        try {
            url = new URL(urlBase);
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("Content-Language", "en-US");
            urlConnection.setUseCaches(false);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            Log.d("Translator",readStream(in));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }
        */

        return null;
    }
}

