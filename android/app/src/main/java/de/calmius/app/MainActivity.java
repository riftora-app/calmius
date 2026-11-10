package de.calmius.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/** Calmius: zeigt die Calmius-Seite als App an. Alle Daten bleiben im Speicher dieses Handys. */
public class MainActivity extends Activity {
  private static final String HOST = "riftora-app.github.io";
  private static final String START = "https://riftora-app.github.io/calmius/";
  private WebView web;
  private WebView printView;

  @Override protected void onCreate(Bundle b) {
    super.onCreate(b);
    web = new WebView(this);
    WebSettings s = web.getSettings();
    s.setJavaScriptEnabled(true);
    s.setDomStorageEnabled(true);
    s.setAllowFileAccess(false);
    s.setCacheMode(WebSettings.LOAD_DEFAULT);
    s.setUserAgentString(s.getUserAgentString() + " CalmiusApp");
    web.setBackgroundColor(0xFF15131C);
    web.addJavascriptInterface(new Bridge(), "CalmiusAndroid");
    web.setWebViewClient(new WebViewClient() {
      @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
        Uri u = r.getUrl();
        if (HOST.equals(u.getHost()) && u.getPath() != null && u.getPath().startsWith("/calmius")) return false;
        try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception e) { }
        return true;
      }
      @Override public void onReceivedError(WebView v, android.webkit.WebResourceRequest r, android.webkit.WebResourceError e) {
        if (r.isForMainFrame()) v.loadDataWithBaseURL(null, "<body style='background:#15131c;color:#eee;font:18px sans-serif;padding:32px'><h2>Calmius</h2><p>Bitte Calmius einmal mit Internet öffnen. Danach funktioniert die App auch offline.</p></body>", "text/html", "UTF-8", null);
      }
    });
    setContentView(web);
    if (b != null) web.restoreState(b); else web.loadUrl(START);
  }

  @Override protected void onSaveInstanceState(Bundle o) { super.onSaveInstanceState(o); web.saveState(o); }

  @Override public void onBackPressed() {
    web.evaluateJavascript("(function(){var b=document.getElementById('back');var o=document.querySelector('.overlay');if(o){o.remove();return 'ok';}if(b){b.click();return 'ok';}return 'no';})()",
      v -> { if (!"\"ok\"".equals(v)) MainActivity.super.onBackPressed(); });
  }

  class Bridge {
    @JavascriptInterface public void print(final String html, final String name) {
      runOnUiThread(() -> {
        final WebView pw = new WebView(MainActivity.this); printView = pw;
        pw.setWebViewClient(new WebViewClient() {
          @Override public void onPageFinished(WebView v, String url) {
            PrintManager pm = (PrintManager) getSystemService(Context.PRINT_SERVICE);
            String job = name.replace(".html", "");
            pm.print(job, v.createPrintDocumentAdapter(job), new PrintAttributes.Builder().build());
          }
        });
        pw.loadDataWithBaseURL(null, html.replace("window.print()", "0"), "text/html", "UTF-8", null);
      });
    }
  }
}
