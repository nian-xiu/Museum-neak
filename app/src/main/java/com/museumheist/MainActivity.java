package com.museumheist;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import com.museumheist.game.GameView;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {
    private GameView gameView;
    private Object backInvokedCallback;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        gameView = new GameView(this);
        setContentView(gameView);
        hideSystemUi();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) backInvokedCallback = Api33BackNavigation.register(this);
    }
    @Override protected void onResume() { super.onResume(); hideSystemUi(); gameView.resume(); }
    @Override protected void onPause() { gameView.pause(); super.onPause(); }
    @Override protected void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && backInvokedCallback != null) { Api33BackNavigation.unregister(this, backInvokedCallback); backInvokedCallback = null; }
        if (gameView != null) gameView.shutdown();
        super.onDestroy();
    }
    @Override public void onWindowFocusChanged(boolean hasFocus) { super.onWindowFocusChanged(hasFocus); if (hasFocus) hideSystemUi(); }
    @SuppressLint("GestureBackNavigation") @Override public void onBackPressed() { handleSystemBack(); }
    private void handleSystemBack() { if (gameView != null && gameView.handleBackPressed()) return; finish(); }
    private void hideSystemUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Window window=getWindow(); window.setDecorFitsSystemWindows(false); WindowInsetsController c=window.getDecorView().getWindowInsetsController();
            if(c!=null){ c.hide(WindowInsets.Type.statusBars()|WindowInsets.Type.navigationBars()); c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE); }
            return;
        }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
    @SuppressLint({"NewApi","InlinedApi","UseRequiresApi"}) private static final class Api33BackNavigation {
        static Object register(MainActivity a){ OnBackInvokedCallback cb=a::handleSystemBack; a.getOnBackInvokedDispatcher().registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT,cb); return cb; }
        static void unregister(MainActivity a,Object cb){ a.getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback((OnBackInvokedCallback)cb); }
    }
}
