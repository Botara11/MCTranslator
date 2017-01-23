package com.kenso.alv.mc_translator;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity  implements KeyEvent.Callback, RecognitionListener {

    private EditText editText;
    private TextView button, txtSpeechInput, text_btnSpeak;
    private ImageButton btnSpeak;
    private AudioManager auman, am;
    private boolean toggle, speakerON;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final int REQ_CODE_SLIDES = 200;
    private TextToSpeech ttobj;
    private TextToSpeech ttobj_esp;
    private MediaRecorder recorder;

    private BluetoothAdapter btAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private BluetoothHeadset btHeadset;
    private SpeechRecognizer speech = null;
    private String TAG = "MAIN";
    private BluetoothDevice enabledDevice;
    private String langSelected = "en";
    private String[] languaCode = {"de_DE","fr_FR","el_CY","en_US","pl_PL","ru_RU","zh-CN","it_IT","pt_PT","ro_RO","sv_SE","no","ko_KP","fi_FI","cs_CZ","da_DK"};  //zh_HK_#Hans
    private String errorString;
    private char mode = 'H'; //MODE 'H'headphone ; 'S' speakers
    private boolean recognizing = false;
    private RemoteControlReceiver nuevo;
    private SharedPreferences prefs;
    private readFile rw;
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SetupBluetooth();
        getWindow().getDecorView().setSystemUiVisibility(4);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] nueva = {
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.BROADCAST_STICKY,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(nueva,1);
        }

        //userTouchEvent();
        Intent intent = getIntent();
        int lan = intent.getIntExtra("Press", 0);
        langSelected = languaCode[lan];
        String[] languaError = {"ERROR, halten Sie die Taste","ERROR, maintenez le bouton","ΣΦΑΛΜΑ, κρατήστε πατημένο το πλήκτρο","ERROR, Hold down the button","BŁĄD, należy przytrzymać przycisk","ОШИБКА, удерживайте кнопку","ERROR，按住按钮","ERRORE, tenere premuto il pulsante","ERRO, mantenha pressionado o botão","EROARE, țineți apăsat butonul","FEL, håll nere","FEIL, hold knappen","ERROR, 버튼을 길게","ERROR, pidä painiketta","ERROR, přidržením tlačítka","FEJL, holde knappen"};
        errorString = languaError[lan];
        Log.d(TAG, lan + "code:" +langSelected);
        IntentFilter musicPauseFilter = new IntentFilter(
                "android.media.action.CLOSE_AUDIO_EFFECT_CONTROL_SESSION");
        IntentFilter musicPlayFilter = new IntentFilter(
                "android.media.action.OPEN_AUDIO_EFFECT_CONTROL_SESSION");
        nuevo = new RemoteControlReceiver();
        registerReceiver(nuevo, musicPauseFilter);
        registerReceiver(nuevo, musicPlayFilter);
        prefs = this.getSharedPreferences(
                "mypreferences", getApplicationContext().MODE_PRIVATE);


        // hide the action bar
        //getActionBar().hide();
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        takeKeyEvents(true);
        toggle = true;
        button = (TextView) findViewById(R.id.button);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        auman = (AudioManager) getApplicationContext().getSystemService(getApplicationContext().AUDIO_SERVICE);
        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        text_btnSpeak = (TextView) findViewById(R.id.text_btnSpeak);

        speakerON = false;
        recorder = new MediaRecorder();
        rw = new readFile();



        ttobj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                Locale[] available = Locale.getAvailableLocales();
                for (Locale i : available){
                    if (i.toString().contains(langSelected)) {
                        try{ ttobj.setLanguage(i); }catch (MissingResourceException e){}
                        Log.d(TAG, i.toString());
                        Log.d(TAG, ttobj.isLanguageAvailable(i) + "");
                        if (ttobj.isLanguageAvailable(i)!=-2)
                            break;
                    }
                }
                }
            }
        });

        ttobj_esp = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    ttobj_esp.setLanguage(Locale.getDefault());
                }
            }
        });

        //MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.hey);
        //mediaPlayer.start();

        button.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                if(recognizing){
                    recognizing = false;
                    speech.stopListening();

                }
                Log.d(TAG,"Recognizing = " + recognizing);
                recognizing = true;
                //userTouchEvent();

                MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.beep);
                mediaPlayer.start();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mode = 'H';
                SharedPreferences preferences = getSharedPreferences(
                        "mypreferences", getApplicationContext().MODE_PRIVATE);
                Log.d(TAG,"Write mode 1 H __" + preferences.edit().putInt("mode",1).commit());

                //enableHeadphoneSpeaker();
                //enableHeadphoneMIC();
                recognize("es_ES");


            }

        });

        btnSpeak.setOnTouchListener(new View.OnTouchListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    //userTouchEvent();
                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int width = size.x;
                    int height = size.y;
                    Log.d(TAG,"TAMAÑOS:" + width + " x " + height);
                    Log.d(TAG,"DOWN");
                    btnSpeak.setImageDrawable(getDrawable(R.mipmap.ico_mic_stop));
                    mode = 'S';SharedPreferences preferences = getApplicationContext().getSharedPreferences(
                            "mypreferences", getApplicationContext().MODE_PRIVATE);
                    Log.d(TAG,"Write mode 2 S __" + preferences.edit().putInt("mode",2).commit());
                    enableSpeaker();
                    recognize(langSelected);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {

                    btnSpeak.setImageDrawable(getDrawable(R.mipmap.ico_mic));
                    Log.d(TAG,"UP");
                    speech.stopListening();
                }
                return true;
            }
        });



        am = (AudioManager) getApplicationContext().getSystemService(getApplicationContext().AUDIO_SERVICE);
        am.setMode(AudioManager.MODE_CURRENT);
        am.setMode(AudioManager.MODE_IN_CALL);
        Log.d("INIT"," ++ mode" +am.getMode() + " Speaker" + am.isSpeakerphoneOn() + " BluetooA2dp" + am.isBluetoothA2dpOn() + " BluetooOffCall" + am.isBluetoothScoAvailableOffCall());
        //am.registerMediaButtonEventReceiver(new ComponentName(this, RemoteControlReceiver.class));
    }



    private void enableSpeaker() {
        //am.setMode(AudioManager.MODE_IN_COMMUNICATION);
        Log.d(TAG, "MODEIN_COMMUNICATION " + am.getMode());
        //am.setBluetoothScoOn(false);
        //am.setSpeakerphoneOn(true);
        Log.d("HEAD"," ++ mode" +am.getMode() + " Speaker" + am.isSpeakerphoneOn() + " BluetooA2dp" + am.isBluetoothA2dpOn() + " BluetooOffCall" + am.isBluetoothScoAvailableOffCall());

    }

    private void enableHeadphoneMIC() {

        if (btAdapter.isEnabled()) {
            for (BluetoothDevice tryDevice : pairedDevices) {
                //This loop tries to start VoiceRecognition mode on every paired device until it finds one that works(which will be the currently in use bluetooth headset)
                if (btHeadset.startVoiceRecognition(tryDevice)) {
                    enabledDevice = tryDevice;
                    Log.d(TAG, "Bluetooth encontrado: " + tryDevice.getName());
                    break;
                }
            }
        }
    }

    private void stopHeadphoneMIC() {
        if (btAdapter.isEnabled()) {
            for (BluetoothDevice tryDevice : pairedDevices) {
                if (btHeadset.stopVoiceRecognition(tryDevice)) {
                    enabledDevice = tryDevice;
                    Log.d(TAG, "Bluetooth stopVoiceRecognition: " + tryDevice.getName());
                    break;
                }
            }
        }
    }

    private void enableHeadphoneSpeaker() {
        am.setMode(AudioManager.MODE_NORMAL);
        am.setMode(AudioManager.MODE_IN_CALL);
        //am.setMode(AudioManager.MODE_IN_COMMUNICATION);
        //am.setMode(AudioManager.MODE_RINGTONE);
        //am.setBluetoothScoOn(true);
        //am.startBluetoothSco();

        am.setSpeakerphoneOn(true);
        //am.setStreamVolume(AudioManager.STREAM_MUSIC,0,1);
        Log.d(TAG, "MODEIN_IN_CALL" + am.getMode());
        Log.d(TAG,"Esta enchufado: " + am.isSpeakerphoneOn());
        Log.d("SPEAK"," ++ mode" +am.getMode() + " Speaker" + am.isSpeakerphoneOn() + " BluetooA2dp" + am.isBluetoothA2dpOn() + " BluetooOffCall" + am.isBluetoothScoAvailableOffCall());

    }

    /**
     * Showing google speech input dialog
     */
    private void recognize(String langCode) {


        if (speech != null) {
            speech.stopListening();
            speech.cancel();
            speech.destroy();
            speech = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
            speech.setRecognitionListener(MainActivity.this);
        }

        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, langCode);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                langCode);//langCode
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE,langCode);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                langCode);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        try {
            SharedPreferences preferences = this.getSharedPreferences(
      "mypreferences", getApplicationContext().MODE_PRIVATE);
            int pref_mode = preferences.getInt("mode",0);
            if(pref_mode == 2) {
                Log.d(TAG, "MODE=" + mode + " and prefmode="+pref_mode);
                //startActivityForResult(recognizerIntent, REQ_CODE_SPEECH_INPUT);
                speech.startListening(recognizerIntent);
            }
            else {
                Log.d(TAG, "MODE=" + mode + " and prefmode="+pref_mode);
                speech.startListening(recognizerIntent);
            }
            Log.d(TAG, "startListening");
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String ols = "";
        switch (requestCode) {
            case REQ_CODE_SLIDES:
                if (resultCode == RESULT_OK && null != data) {
                    String st = data.getStringExtra("RES");
                    Log.d(TAG,st);
                    if (st.compareToIgnoreCase("LANGUAGE") == 0){
                        unregisterReceiver(nuevo);
                        handler.removeCallbacksAndMessages(null);
                        finish();
                    }
                }
                break;
            case REQ_CODE_SPEECH_INPUT: {

                break;

            }
        }
    }




    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d("onKey", "Apretado");
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                System.out.println(keyCode + "PREVIOUS");
                ttobj_esp.speak("Ya estas en hablar. Pulsa en boton central para hablar", TextToSpeech.QUEUE_FLUSH, null);
                return true;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                System.out.println(keyCode + "NEXT");
                ttobj_esp.speak("Lenguaje. seleccione un idioma", TextToSpeech.QUEUE_FLUSH, null);
                unregisterReceiver(nuevo);
                handler.removeCallbacksAndMessages(null);
                finish();
                return true;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                System.out.println(keyCode + "PAUSE");
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                if(recognizing){
                    recognizing = false;
                    speech.stopListening();
                    return true;
                }
                Log.d(TAG,"Recognizing = " + recognizing);
                recognizing = true;
                //userTouchEvent();

                MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.beep);
                mediaPlayer.start();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(keyCode + "PLAY");
                mode = 'H';
                SharedPreferences preferences = this.getSharedPreferences(
                        "mypreferences", getApplicationContext().MODE_PRIVATE);
                Log.d(TAG,"Write mode 1 H __" + preferences.edit().putInt("mode",1).commit());

                enableHeadphoneSpeaker();
                enableHeadphoneMIC();
                recognize("es_ES");

                return true;
            case KeyEvent.KEYCODE_BACK:
                System.out.println(keyCode + "BACK");
                unregisterReceiver(nuevo);
                handler.removeCallbacksAndMessages(null);
                finish();
            default:
                System.out.println(keyCode + event.toString());
                System.out.println(keyCode + "UNKNOWN");

                return super.onKeyUp(keyCode, event);
        }
    }


    private void SetupBluetooth() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        pairedDevices = btAdapter.getBondedDevices();
        BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == BluetoothProfile.HEADSET) {
                    btHeadset = (BluetoothHeadset) proxy;
                    Log.d(TAG, "ALERRTAA----------------------------------------------------------------"+String.valueOf(btHeadset));
                }
                Log.d(TAG,"Ha entrado");
            }

            public void onServiceDisconnected(int profile) {
                if (profile == BluetoothProfile.HEADSET) {
                    btHeadset = null;
                }
            }
        };

        boolean ola = btAdapter.getProfileProxy(MainActivity.this, mProfileListener, BluetoothProfile.HEADSET);
        Log.d(TAG,"getProfileProxy returned: " + ola);

    }

    @Override
    public void onResume(){
        getWindow().getDecorView().setSystemUiVisibility(4);
        //userTouchEvent();
        super.onResume();
    }



    @Override
    protected void onPause() {
        super.onPause();
        if (speech != null) {
            speech.destroy();
            Log.i(TAG, "destroy");
        }

    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(TAG, "onBeginningOfSpeech");
        //progressBar.setIndeterminate(false);
        //progressBar.setMax(10);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(TAG, "onEndOfSpeech");
        //progressBar.setIndeterminate(true);
        //toggleButton.setChecked(false);
        speech.stopListening();
    }

    @Override
    public void onError(int errorCode) {
        stopHeadphoneMIC();
        String errorMessage = getErrorText(errorCode);

        Log.d(TAG, "FAILED " + errorMessage);
        txtSpeechInput.setText(errorString);
        rw.write("Error: errorcode:"+errorMessage+"\n");

        //returnedText.setText(errorMessage);
        //toggleButton.setChecked(false);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(TAG, "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {

        recognizing = false;

        stopHeadphoneMIC();

        enableHeadphoneSpeaker();

        Log.i(TAG, "onResults");
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches)
            text += result + "\n";
        Log.i(TAG, "RECONOCIMIENTO: " + text);
        String traducido = "";
        Translator nu = new Translator();

        String[] data1;
        if(mode=='H'){
            data1 = new String[] {matches.get(0),"es",langSelected};
        }else{
            data1 = new String[] {matches.get(0),langSelected,"es"};
        }
        AsyncTask<String, Void, String> async;
        async = nu.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data1);
        try {

            traducido = URLDecoder.decode(async.get(),"UTF-8");
            //traducido = async.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e) {e.printStackTrace();}
        Log.d(TAG, "TRADUCCIÓN: " + traducido);


        Log.d(TAG,"MODE is " + mode);
        if (mode == 'H'){
            txtSpeechInput.setText(traducido);
            Log.d(TAG,"ENTRA EN MODE h --------------");
            enableHeadphoneSpeaker();
            ttobj_esp.speak(matches.get(0), TextToSpeech.QUEUE_FLUSH, null);

        }else{
            txtSpeechInput.setText(matches.get(0));
            enableHeadphoneSpeaker();
            ttobj_esp.speak(traducido, TextToSpeech.QUEUE_FLUSH, null);
        }
        rw.write("Success "+langSelected+": From: <" + matches.get(0) +"> to <" + traducido + ">\n");
        mode = ' ';

    }


    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(TAG, "onRmsChanged: " + rmsdB);
        //progressBar.setProgress((int) rmsdB);
    }

    public static String getErrorText(int errorCode) {
        String message;

        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
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
        }, 300000);//for 5 minutos 300000
    }
}
