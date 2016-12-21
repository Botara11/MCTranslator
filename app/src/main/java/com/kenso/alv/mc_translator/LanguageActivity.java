package com.kenso.alv.mc_translator;

/**
 * Created by alv on 8/12/16.
 */


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class LanguageActivity  extends Activity {
    private String TAG = "LanguageActivity";
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

        MyAdapter adapter = new MyAdapter(this);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                Log.d(TAG,"Apretado: " + position);
                Intent intent = new Intent(getApplication(), MainActivity.class);
                intent.putExtra("Press", position);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume(){
        getWindow().getDecorView().setSystemUiVisibility(4);
        super.onResume();
    }

}