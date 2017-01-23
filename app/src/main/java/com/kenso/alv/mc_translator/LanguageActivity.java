package com.kenso.alv.mc_translator;

/**
 * Created by alv on 8/12/16.
 */


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;
import java.util.Set;

public class LanguageActivity  extends Activity implements KeyEvent.Callback {
    private String TAG = "LanguageActivity";
    private TextToSpeech ttobj_esp;
    private final int REQ_CODE_SLIDES = 200;

    final Handler handler = new Handler();

    public class MyAdapter extends BaseAdapter {


        private String[] languaName = {"Deutsch","Français","ελληνικά","English","Polski","русский","中國","Italiano","Português","Românesc","Svenska","Norsk","한국의","suomalainen","český","Dansk"};
        private Bitmap[] bitmap = new Bitmap[languaName.length];
        private Context context;
        private LayoutInflater layoutInflater;

        MyAdapter(Context c){
            context = c;
            layoutInflater = LayoutInflater.from(context);

            //init dummy bitmap,
            //using R.drawable.icon for all items
            for(int i = 0; i < languaName.length; i++){
                int iden = getResources().getIdentifier("lan"+((i+1)),"drawable",LanguageActivity.this.getPackageName());
                bitmap[i] = BitmapFactory.decodeResource(context.getResources(), iden);
            }
        }


        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return bitmap.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return bitmap[position];
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub

            View grid;
            if(convertView==null){
                grid = new View(context);
                grid = layoutInflater.inflate(R.layout.gridlayout, null);
            }else{
                grid = (View)convertView;
            }

            ImageView imageView = (ImageView)grid.findViewById(R.id.image);
            imageView.setImageBitmap(bitmap[position]);
            imageView.setAdjustViewBounds(true);
            imageView.setMaxHeight(300);
            TextView textView = (TextView)grid.findViewById(R.id.text);
            textView.setText(languaName[position]);

            return grid;
        }

    }

    GridView gridView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.languagelayout);
        gridView = (GridView)findViewById(R.id.grid);
        getWindow().getDecorView().setSystemUiVisibility(4);
        userTouchEvent();

        MyAdapter adapter = new MyAdapter(this);
        SetupBluetooth();
        gridView.setAdapter(adapter);
        ttobj_esp = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    ttobj_esp.setLanguage(Locale.getDefault());
                }
            }
        });
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                handler.removeCallbacksAndMessages(null);
                Log.d(TAG,"Apretado: " + position);
                String[] languaCode = {"Alemán","Francés","Griego","Inglés","Polaco","Ruso","Chino","Italiano","Portugués","Rumano","Sueco","Noruego","Coreano","Finlandés","Checo","Danés"};
                ttobj_esp.speak(languaCode[position], TextToSpeech.QUEUE_FLUSH, null);
                Intent intent = new Intent(getApplication(), MainActivity.class);
                intent.putExtra("Press", position);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume(){
        userTouchEvent();
        getWindow().getDecorView().setSystemUiVisibility(4);
        super.onResume();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d("onKey", "Apretado");

        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                System.out.println(keyCode + "PREVIOUS");
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                System.out.println(keyCode + "NEXT");
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                System.out.println(keyCode + "PAUSE");
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                ttobj_esp.speak("Seleccione un idioma", TextToSpeech.QUEUE_FLUSH, null);
                return true;
            case KeyEvent.KEYCODE_BACK:
                System.out.println(keyCode + "BACK");
                super.onBackPressed();
                return true;
            default:
                System.out.println(keyCode + event.toString());
                System.out.println(keyCode + "UNKNOWN");
                return true;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String ols = "";
        switch (requestCode) {
            case REQ_CODE_SLIDES:
                if (resultCode == RESULT_OK && null != data) {
                    String st = data.getStringExtra("RES");
                    Log.d(TAG,st);
                    if (st.compareToIgnoreCase("MAIN") == 0){
                        ttobj_esp.speak("No se puede ir a HABLAR. seleccione un idioma", TextToSpeech.QUEUE_FLUSH, null);
                    }else{
                        ttobj_esp.speak("Lenguaje: seleccione un idioma", TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
                break;
        }
    }


                private void SetupBluetooth() {
        BluetoothAdapter btAdapter;
        Set<BluetoothDevice> pairedDevices;

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        pairedDevices = btAdapter.getBondedDevices();
        BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
            BluetoothHeadset btHeadset;
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == BluetoothProfile.HEADSET) {
                    btHeadset = (BluetoothHeadset) proxy;
                    Log.d("Fragment", "ALERRTAA----------------------------------------------------------------"+String.valueOf(btHeadset));
                }
                Log.d("Fragment","Ha entrado");
            }

            public void onServiceDisconnected(int profile) {
                if (profile == BluetoothProfile.HEADSET) {
                    btHeadset = null;
                }
            }
        };

        boolean ola = btAdapter.getProfileProxy(this, mProfileListener, BluetoothProfile.HEADSET);
        Log.d("Fragment","getProfileProxy returned: " + ola);

    }
    public void userTouchEvent (){
        Log.d(TAG,"USER TOUCH EVENT");
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplication(), ScreenSlidePagerActivity.class);
                startActivityForResult(intent,REQ_CODE_SLIDES);
            }
        }, 300000);//5 minutos 300000
    }

}