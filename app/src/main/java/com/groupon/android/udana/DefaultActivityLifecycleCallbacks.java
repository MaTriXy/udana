package com.groupon.android.udana;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Default empty implementation of {@code Application.ActivityLifecycleCallbacks}.
 */
public abstract class DefaultActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    @Override public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override public void onActivityStarted(Activity activity) {

    }

    @Override public void onActivityResumed(Activity activity) {

    }

    @Override public void onActivityPaused(Activity activity) {

    }

    @Override public void onActivityStopped(Activity activity) {

    }

    @Override public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override public void onActivityDestroyed(Activity activity) {

    }
}
