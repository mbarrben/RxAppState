package com.jenzz.appstate.internal;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.ComponentCallbacks2;
import com.jenzz.appstate.AppStateListener;
import com.jenzz.appstate.dummies.DummyAppStateListener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class AppStateRecognizerTest {

  @Mock Application mockApplication;
  @Mock AppStateListener mockAppStateListener;

  @Captor ArgumentCaptor<ActivityLifecycleCallbacks> activityCallbacksCaptor;

  private final AppStateRecognizer appStateRecognizer = new AppStateRecognizer();

  @Before
  public void setUp() {
    initMocks(this);
  }

  @Test
  public void registerCallbacks() {
    appStateRecognizer.start(mockApplication, new DummyAppStateListener());

    verify(mockApplication).registerActivityLifecycleCallbacks(any(ActivityLifecycleCallbacks.class));
    verify(mockApplication).registerComponentCallbacks(any(ComponentCallbacks2.class));
  }

  @Test
  public void unregistersCallbacks() {
    appStateRecognizer.stop(mockApplication);

    verify(mockApplication).unregisterActivityLifecycleCallbacks(any(ActivityLifecycleCallbacks.class));
    verify(mockApplication).unregisterComponentCallbacks(any(ComponentCallbacks2.class));
  }

  @Test
  public void doesNotReturnNullAppStateByDefault() {
    assertThat(appStateRecognizer.getAppState()).isNotNull();
  }

  @Test
  public void emitsForegroundIfActivityStartsOnFirstLaunch() {
    appStateRecognizer.start(mockApplication, mockAppStateListener);
    verify(mockApplication).registerActivityLifecycleCallbacks(activityCallbacksCaptor.capture());

    activityCallbacksCaptor.getValue().onActivityStarted(new Activity());

    verify(mockAppStateListener).onAppDidEnterForeground();
  }

  @Test
  public void doesNotEmitForegroundIfActivityStartsOnSuccessiveLaunches() {
    appStateRecognizer.start(mockApplication, mockAppStateListener);
    verify(mockApplication).registerActivityLifecycleCallbacks(activityCallbacksCaptor.capture());
    final ActivityLifecycleCallbacks lifecycleCallbacks = activityCallbacksCaptor.getValue();
    
    lifecycleCallbacks.onActivityStarted(new Activity());
    lifecycleCallbacks.onActivityStarted(new Activity());
    lifecycleCallbacks.onActivityStarted(new Activity());

    verify(mockAppStateListener, times(1)).onAppDidEnterForeground();
  }
}
