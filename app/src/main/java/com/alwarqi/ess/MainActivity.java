package com.alwarqi.ess;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private SharedPreferences prefs;
    private static final String PREF_NAME = "alwarqi_ess_prefs";
    private static final String KEY_URL = "server_url";
    private static final String DEFAULT_URL = "http://192.168.11.59:5050";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        FrameLayout root = new FrameLayout(this);
        webView = new WebView(this);
        webView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
                if (req != null && req.isForMainFrame()) {
                    showServerUrlDialog("تعذر الاتصال بالخادم. تحقق من عنوان IP والمنفذ:");
                }
            }
        });

        ImageButton cfgBtn = new ImageButton(this);
        cfgBtn.setImageResource(android.R.drawable.ic_menu_manage);
        cfgBtn.setBackgroundColor(0xCC7F1D1D);
        cfgBtn.setColorFilter(0xFFFFFFFF);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(110, 110);
        lp.gravity = Gravity.TOP | Gravity.END;
        lp.setMargins(20, 20, 20, 20);
        cfgBtn.setLayoutParams(lp);
        cfgBtn.setOnClickListener(v -> showServerUrlDialog("تعديل عنوان خادم الشركة (IP:Port):"));

        root.addView(webView);
        root.addView(cfgBtn);
        setContentView(root);

        String url = prefs.getString(KEY_URL, "");
        if (url.isEmpty()) {
            showServerUrlDialog("أدخل عنوان خادم الخدمة الذاتية (مثال: http://192.168.11.59:5050):");
        } else {
            webView.loadUrl(url);
        }
    }

    private void showServerUrlDialog(String title) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(40, 24, 40, 10);
        final EditText input = new EditText(this);
        input.setText(prefs.getString(KEY_URL, DEFAULT_URL));
        box.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("إعداد اتصال تطبيق الورقي")
                .setMessage(title)
                .setView(box)
                .setCancelable(false)
                .setPositiveButton("اتصال وحفظ", (d, w) -> {
                    String u = input.getText().toString().trim();
                    if (!u.startsWith("http")) u = "http://" + u;
                    prefs.edit().putString(KEY_URL, u).apply();
                    webView.loadUrl(u);
                })
                .setNegativeButton("إعادة تحميل", (d, w) -> webView.reload())
                .show();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
