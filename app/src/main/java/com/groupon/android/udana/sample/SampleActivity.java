package com.groupon.android.udana.sample;

import android.app.Activity;
import android.os.Bundle;
import com.groupon.android.udana.Uda;
import com.groupon.android.udana.Udana;

/**
 */
public class SampleActivity extends Activity {
    String string;
    @Udana String udanaString;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isPreserved = Uda.preserveMembers(this, savedInstanceState);
        System.out.println("string : " + string);
        System.out.println("udanaString : " + udanaString);
        string = "a";
        if (!isPreserved) {
            udanaString = "b";
        }
    }
}
