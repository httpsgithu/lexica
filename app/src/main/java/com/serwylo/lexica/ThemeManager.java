package com.serwylo.lexica;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.preference.PreferenceManager;

/**
 * This class is based on GPLv3 licensed code from F-Droid client:
 * <p>
 * https://gitlab.com/fdroid/fdroidclient/-/blob/master/app/src/main/java/org/fdroid/fdroid/FDroidApp.java
 */
public class ThemeManager {

    private static ThemeManager instance;

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }

        return instance;
    }

    public static final String PREFERENCE_NAME = "theme";

    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_HIGH_CONTRAST = "high_contrast";

    private String currentTheme = null;

    /**
     * Force reload the {@link Activity to make theme changes take effect.}
     */
    public void forceRestartActivityToRetheme(Activity activity) {
        Intent intent = activity.getIntent();
        if (intent == null) { // when launched as LAUNCHER
            return;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.finish();
        activity.overridePendingTransition(0, 0);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    public void rememberTheme(Context context) {
        currentTheme = PreferenceManager.getDefaultSharedPreferences(context).getString(PREFERENCE_NAME, THEME_LIGHT);
    }

    public String applyTheme(Activity activity) {
        activity.setTheme(getCurThemeResId(activity));
        return currentTheme;
    }

    private int getCurThemeResId(Context context) {
        if (currentTheme == null) {
            rememberTheme(context);
        }

        if (THEME_DARK.equals(currentTheme)) {
            return R.style.AppTheme_Dark;
        } else if (THEME_HIGH_CONTRAST.equals(currentTheme)) {
            return R.style.AppTheme_HighContrast;
        } else {
            return R.style.AppTheme_Light;
        }
    }

    public String getCurrentTheme() {
        return currentTheme;
    }
}
