package com.kenso.alv.mc_translator;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity  implements KeyEvent.Callback, RecognitionListener {

    private EditText editText;
    private TextView button, txtSpeechInput;
    private ImageButton btnSpeak;
    private AudioManager auman, am;
    private boolean toggle, speakerON;
    private final int REQ_CODE_SPEECH_INPUT = 100;
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
    private char mode = 'H'; //MODE 'H'headphone ; 'S' speakers
    private RemoteControlReceiver nuevo;
    private SharedPreferences prefs;

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

        Intent intent = getIntent();
        int lan = intent.getIntExtra("Press", 0);
        langSelected = languaCode[lan];
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


        //am = (AudioManager) getApplicationContext().getSystemService(getApplicationContext().AUDIO_SERVICE);
        //am.setMode(AudioManager.MODE_NORMAL);
        //am.setSpeakerphoneOn(false);
        //am.setBluetoothScoOn(true);
        speakerON = false;
        recorder = new MediaRecorder();
        /*
        RemoteControlReceiver mMediaButtonReceiver = new RemoteControlReceiver();
        IntentFilter mediaFilter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
        mediaFilter.setPriority(10000);
        registerReceiver(mMediaButtonReceiver, mediaFilter);
        */



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

        MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.hey);
        //mediaPlayer.start();

        button.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                if (mode=='H') {
                    am.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    //am.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    Log.d(TAG, "MODEIN_COMMUNICATION " + am.getMode());
                    am.setBluetoothScoOn(false);
                    //am.stopBluetoothSco();
                    am.setSpeakerphoneOn(true);
                    mode='S';

                    Log.d("HEAD"," ++ mode" +am.getMode() + " Speaker" + am.isSpeakerphoneOn() + " BluetooA2dp" + am.isBluetoothA2dpOn() + " BluetooOffCall" + am.isBluetoothScoAvailableOffCall());
                    //recognize("es_ES");
                }else{
                    mode='H';
                    am.setMode(AudioManager.MODE_NORMAL);
                    //am.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    Log.d(TAG, "MODEIN_IN_CALL" + am.getMode());
                    //am.setSpeakerphoneOn(false);
                    Log.d(TAG,"Esta enchufado: " + am.isSpeakerphoneOn());
                    //am.setBluetoothScoOn(true);
                    //am.startBluetoothSco();
                    AudioDeviceInfo ol;

                    Log.d("SPEAK"," ++ mode" +am.getMode() + " Speaker" + am.isSpeakerphoneOn() + " BluetooA2dp" + am.isBluetoothA2dpOn() + " BluetooOffCall" + am.isBluetoothScoAvailableOffCall());
                    //recognize("es_ES");

                }

            }

        });

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mode = 'S';SharedPreferences preferences = getApplicationContext().getSharedPreferences(
                        "mypreferences", getApplicationContext().MODE_PRIVATE);
                Log.d(TAG,"Write mode 2 S __" + preferences.edit().putInt("mode",2).commit());
                enableSpeaker();
                recognize(langSelected);
            }
        });

        am = (AudioManager) getApplicationContext().getSystemService(getApplicationContext().AUDIO_SERVICE);
        am.setMode(AudioManager.MODE_IN_CALL);
        Log.d("INIT"," ++ mode" +am.getMode() + " Speaker" + am.isSpeakerphoneOn() + " BluetooA2dp" + am.isBluetoothA2dpOn() + " BluetooOffCall" + am.isBluetoothScoAvailableOffCall());
        //am.registerMediaButtonEventReceiver(new ComponentName(this, RemoteControlReceiver.class));
    }



    private void enableSpeaker() {


        am.setMode(AudioManager.MODE_IN_COMMUNICATION);
        Log.d(TAG, "MODEIN_COMMUNICATION " + am.getMode());
        am.setBluetoothScoOn(false);
        am.setSpeakerphoneOn(true);
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
                    Log.d(TAG, "Bluetooth encontrado: " + tryDevice.getName());
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
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,true);
        //recognizerIntent.putExtra(RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE,true);
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true);
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,8000);
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,8000);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);


/*
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
       // intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
       //         getString(R.string.speech_prompt));
        intent.putExtra(RecognizerIntent.EXTRA_SECURE , RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE);
        */
        try {
            SharedPreferences preferences = this.getSharedPreferences(
      "mypreferences", getApplicationContext().MODE_PRIVATE);
            int pref_mode = preferences.getInt("mode",0);
            if(pref_mode == 2) {
                Log.d(TAG, "MODE=" + mode + " and prefmode="+pref_mode);
                startActivityForResult(recognizerIntent, REQ_CODE_SPEECH_INPUT);
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
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    stopHeadphoneMIC();

                    if (mode == 'H'){
                        enableSpeaker();
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else{
                        enableHeadphoneSpeaker();
                    }

                    Log.i(TAG, "onResults");
                    ArrayList<String> matches = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
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
                        traducido = async.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "TRADUCCIÓN: " + traducido);
                    //if (mode == 'H')
                    //    am.setStreamVolume(AudioManager.STREAM_MUSIC,am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)-2,1);
                    txtSpeechInput.setText(traducido);

                    Log.d(TAG,"MODE is " + mode);
                    if (mode == 'H'){
                        Log.d(TAG,"ENTRA EN MODE h --------------");
                        enableSpeaker();
                        ttobj.speak(traducido, TextToSpeech.QUEUE_FLUSH, null);

                    }else{
                        Log.d(TAG,"ENTRA EN MODE h --------------");
                        enableHeadphoneSpeaker();
                        ttobj_esp.speak(traducido, TextToSpeech.QUEUE_FLUSH, null);
                    }

                    mode = ' ';


                }
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
                return true;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                System.out.println(keyCode + "NEXT");
                return true;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                System.out.println(keyCode + "PAUSE");
            case KeyEvent.KEYCODE_MEDIA_PLAY:
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
                finish();
            default:
                System.out.println(keyCode + event.toString());
                System.out.println(keyCode + "UNKNOWN");

                return super.onKeyUp(keyCode, event);
        }
    }

/*
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int action = event.getAction();
        int keyCode = event.getKeyCode();

        Log.d("apretado","apretado");
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    //TODO
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    //TODO
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
  }
  */

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

        if (mode == 'S'){
            ttobj.speak("Error, Try again", TextToSpeech.QUEUE_FLUSH, null);
            txtSpeechInput.setText("Error, Try again");
        }else{
            ttobj_esp.speak("Error", TextToSpeech.QUEUE_FLUSH, null);

        }
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

        stopHeadphoneMIC();

        if (mode == 'H'){
            enableSpeaker();
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            enableHeadphoneSpeaker();
        }



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
            traducido = async.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "TRADUCCIÓN: " + traducido);
        //if (mode == 'H')
        //    am.setStreamVolume(AudioManager.STREAM_MUSIC,am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)-2,1);
        txtSpeechInput.setText(traducido);

        Log.d(TAG,"MODE is " + mode);
        if (mode == 'H'){
            Log.d(TAG,"ENTRA EN MODE h --------------");
            enableSpeaker();
            ttobj.speak(traducido, TextToSpeech.QUEUE_FLUSH, null);

        }else{
            Log.d(TAG,"ENTRA EN MODE h --------------");
            enableHeadphoneSpeaker();
            ttobj_esp.speak(traducido, TextToSpeech.QUEUE_FLUSH, null);
        }

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
}
