package com.jenzz.appstate.internal;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import com.jenzz.appstate.AppState;
import com.jenzz.appstate.AppStateListener;
import com.jenzz.appstate.internal.adapters.ActivityLifecycleCallbacksAdapter;
import com.jenzz.appstate.internal.adapters.ComponentCallbacks2Adapter;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.content.Intent.ACTION_SCREEN_OFF;
import static com.jenzz.appstate.AppState.BACKGROUND;
import static com.jenzz.appstate.AppState.FOREGROUND;

public final class AppStateRecognizer {

  private final ActivityLifecycleCallbacks activityStartedCallback = new ActivityStartedCallback();
  private final ComponentCallbacks2 uiHiddenCallback = new UiHiddenCallback();
  private final BroadcastReceiver screenOffBroadcastReceiver = new ScreenOffBroadcastReceiver();

  private AppState appState = BACKGROUND;
  private AppStateListener appStateListener;
  private AtomicBoolean isFirstLaunch = new AtomicBoolean(true);

  public void start(@NonNull Application app, @NonNull AppStateListener appStateListener) {
    this.appStateListener = appStateListener;

    app.registerActivityLifecycleCallbacks(activityStartedCallback);
    app.registerComponentCallbacks(uiHiddenCallback);
    app.registerReceiver(screenOffBroadcastReceiver, new IntentFilter(ACTION_SCREEN_OFF));
  }

  public void stop(@NonNull Application app) {
    app.unregisterActivityLifecycleCallbacks(activityStartedCallback);
    app.unregisterComponentCallbacks(uiHiddenCallback);
    app.unregisterReceiver(screenOffBroadcastReceiver);
  }

  @NonNull
  public AppState getAppState() {
    return appState;
  }

  private void onAppDidEnterForeground() {
    appState = FOREGROUND;
    appStateListener.onAppDidEnterForeground();
  }

  private void onAppDidEnterBackground() {
    appState = BACKGROUND;
    appStateListener.onAppDidEnterBackground();
  }

  private class ActivityStartedCallback extends ActivityLifecycleCallbacksAdapter {

    @Override
    public void onActivityStarted(Activity activity) {
      if (isFirstLaunch.compareAndSet(true, false)) {
        appState = FOREGROUND;
      }

      if (appState == BACKGROUND) {
        onAppDidEnterForeground();
      }
    }
  }

  private class UiHiddenCallback extends ComponentCallbacks2Adapter {

    @Override
    public void onTrimMemory(int level) {
      if (level >= TRIM_MEMORY_UI_HIDDEN) {
        onAppDidEnterBackground();
      }
    }
  }

  private class ScreenOffBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      if (appState == FOREGROUND) {
        onAppDidEnterBackground();
      }
    }
  }
}
