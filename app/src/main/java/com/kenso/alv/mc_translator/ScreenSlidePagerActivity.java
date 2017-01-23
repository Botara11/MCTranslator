package com.kenso.alv.mc_translator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class ScreenSlidePagerActivity extends FragmentActivity  implements KeyEvent.Callback {
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 11;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    FixedSpeedScroller scroller;
    private PagerAdapter mPagerAdapter;


    Timer timer;
    int page = 0;
    final Handler handler = new Handler();
    Field mScroller;
    int slidesMode = 1;

    private TextToSpeech ttobj_esp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_slide);
        getWindow().getDecorView().setSystemUiVisibility(2);
        SetupBluetooth();

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        pageSwitcher(5);
        mPager.setPageTransformer(true,new ParallaxPageTransformer());
        try {

            mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            scroller = new FixedSpeedScroller(mPager.getContext());
            // scroller.setFixedDuration(5000);
            //scroller.setSpeed(100);
            mScroller.set(mPager, scroller);

        } catch (NoSuchFieldException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        }

        ttobj_esp = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    ttobj_esp.setLanguage(Locale.getDefault());
                }
            }
        });

        ttobj_esp.speak("Poniendo anuncios", TextToSpeech.QUEUE_FLUSH, null);
    }


    public void pageSwitcher(int seconds) {
        timer = new Timer(); // At this line a new Thread will be create
        timer.scheduleAtFixedRate(new RemindTask(), 0, seconds * 1000); // delay
        // in
        // milliseconds
    }

    public void userTouchEvent (){
        timer.cancel();
        try {
            scroller.setSpeed(700);
            mScroller.set(mPager, scroller);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                pageSwitcher(5);
                try {
                    scroller.setSpeed(5000);
                    mScroller.set(mPager, scroller);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }, 60000);
    }
    // this is an inner class...
    class RemindTask extends TimerTask {

        @Override
        public void run() {

            // As the TimerTask run on a seprate thread from UI thread we have
            // to call runOnUiThread to do work on UI thread.
            runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                public void run() {

                    if (page == 0) {
                        slidesMode = 1;
                        mPager.setCurrentItem(page++);
                    } else if (page >= NUM_PAGES-1) {
                        slidesMode = 0;
                        mPager.setCurrentItem(page--);
                    } else if (slidesMode == 1) {
                        mPager.setCurrentItem(page++);
                    } else {
                        mPager.setCurrentItem(page--);
                    }

                    getWindow().getDecorView().setSystemUiVisibility(2);
                    getWindow().getDecorView().setOnDragListener(new View.OnDragListener() {
                        @Override
                        public boolean onDrag(View v, DragEvent event) {
                            System.out.println("Window drag");
                            return false;
                        }
                    });
                }
            });

        }
    }




    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d("onKey", "Apretado");

        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                System.out.println(keyCode + "PREVIOUS");

                Intent intent = this.getIntent();
                intent.putExtra("RES", "MAIN");
                this.setResult(RESULT_OK, intent);
                finish();
                return true;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                System.out.println(keyCode + "NEXT");
                Intent intent2 = this.getIntent();
                intent2.putExtra("RES", "LANGUAGE");
                this.setResult(RESULT_OK, intent2);
                finish();
                return true;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                System.out.println(keyCode + "PAUSE");
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                ttobj_esp.speak("Pulsa arriba para lenguaje y abajo para hablar", TextToSpeech.QUEUE_FLUSH, null);
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










    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            android.support.v4.app.Fragment fr = new ScreenSlidePageFragment();
            Bundle args = new Bundle();
            args.putInt("page", position);
            fr.setArguments(args);
            System.out.println("Position: " + position);
            return  fr;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }


    public class ParallaxPageTransformer implements ViewPager.PageTransformer {

        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }

        }
    }

    public class FixedSpeedScroller extends Scroller {

        private int mDuration = 5000;

        public FixedSpeedScroller(Context context) {
            super(context);
        }

        public FixedSpeedScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        public void setSpeed (int speed){
            mDuration = speed;
        }
        public FixedSpeedScroller(Context context, Interpolator interpolator, boolean flywheel) {
            super(context, interpolator, flywheel);
        }


        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
            if (duration != 200){
                userTouchEvent();
            }
            System.out.println("Duration =" + duration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }
    }

}

