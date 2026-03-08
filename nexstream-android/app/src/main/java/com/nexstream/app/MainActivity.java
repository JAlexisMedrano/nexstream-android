package com.nexstream.app;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pantalla completa sin barra de título
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        // Mantener pantalla encendida mientras reproduce
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Orientación horizontal (TV siempre es landscape)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        setupWebView();

        // Cargar NEXSTREAM desde assets
        webView.loadUrl("file:///android_asset/nexstream.html");
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();

        // JavaScript habilitado
        settings.setJavaScriptEnabled(true);

        // ═══ CORS DESHABILITADO ═══
        // Permite que el WebView cargue cualquier URL (streams IPTV, .ts, etc.)
        // sin restricciones de Same-Origin Policy
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Rendimiento y media
        settings.setMediaPlaybackRequiresUserGesture(false); // autoplay sin tap
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Hardware acceleration para video
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // Ocultar scrollbars (TV fullscreen)
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);

        // User-Agent que indica soporte de TV
        String ua = settings.getUserAgentString();
        settings.setUserAgentString(ua + " NexstreamTV/5.0 AndroidTV");

        // WebViewClient: manejar navegación
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Todo dentro del WebView, no abrir browser externo
                return false;
            }
        });

        // WebChromeClient: permisos de media y pantalla completa
        webView.setWebChromeClient(new WebChromeClient() {

            // Permitir reproducción de video protegido y EME
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }

            // Log de consola para debugging (visible en Android Studio Logcat)
            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                android.util.Log.d("NEXSTREAM_JS",
                    cm.message() + " [" + cm.sourceId() + ":" + cm.lineNumber() + "]");
                return true;
            }
        });
    }

    // ═══ CONTROL REMOTO ═══
    // Pasar todas las teclas al WebView (flechas, OK, Back, numérico, etc.)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (webView.canGoBack() && keyCode == KeyEvent.KEYCODE_BACK) {
            // Back: primero intentar navegación interna en la WebApp
            webView.dispatchKeyEvent(event);
            return true;
        }
        if (webView != null) {
            return webView.dispatchKeyEvent(event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (webView != null) {
            return webView.dispatchKeyEvent(event);
        }
        return super.onKeyUp(keyCode, event);
    }

    // ═══ LIFECYCLE ═══
    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
        webView.resumeTimers();
        // Mantener inmersivo en Android TV
        hideSystemUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
        webView.pauseTimers();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }

    // Modo inmersivo: oculta barra de estado y navegación
    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }
}
