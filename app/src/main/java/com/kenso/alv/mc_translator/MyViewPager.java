package com.kenso.alv.mc_translator;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import java.lang.reflect.Method;

/**
 * Created by alv on 29/12/16.
 */

public class MyViewPager extends ViewPager {
    Method scroller;
    public MyViewPager(Context context) {
        super(context);

    }
    public MyViewPager(Context context, AttributeSet attr) {
        super(context,attr);

    }

    void smoothScrollTo(int x, int y, int velocity) {
        //super.smoothScrollTo(x, y, 1);
    }

}
