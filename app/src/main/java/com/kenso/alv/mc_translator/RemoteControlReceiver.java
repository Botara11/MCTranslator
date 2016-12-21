package com.kenso.alv.mc_translator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by alv on 5/12/16.
 */

public class RemoteControlReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Controll",intent.getAction().toString());
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = (KeyEvent) intent .getParcelableExtra(Intent.EXTRA_KEY_EVENT);

            if (event == null) {
                return;
            }

            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                Log.d("Controll", "ABAJO");
                //context.sendBroadcast(new Intent(Intents.ACTION_PLAYER_PAUSE));
            }
        }
    }

}
