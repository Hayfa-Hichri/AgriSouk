package tn.isetsb.agrisouk.ui;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import tn.isetsb.agrisouk.R;
import tn.isetsb.agrisouk.data.ProductRepository;
import tn.isetsb.agrisouk.model.Product;
import tn.isetsb.agrisouk.util.FormatUtils;
import tn.isetsb.agrisouk.util.SessionManager;

/**
 * WebView : bulletin local généré à partir de SQLite et portail agriculture.tn.
 */
public class WebViewActivity extends AppCompatActivity {

    public static final String EXTRA_REMOTE = "remote";
    private static final String REMOTE_URL = "https://www.agriculture.tn/";

    private ProductRepository repository;
    private SessionManager session;
    private WebView webView;
    private ProgressBar progressWeb;
    private View errorPanel;
    private TextView tvWebError;
    private MaterialButton btnLocal;
    private MaterialButton btnRemote;
    private boolean remoteMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        repository = new ProductRepository(this);
        session = new SessionManager(this);
        webView = findViewById(R.id.webView);
        progressWeb = findViewById(R.id.progressWeb);
        errorPanel = findViewById(R.id.webErrorPanel);
        tvWebError = findViewById(R.id.tvWebError);
        btnLocal = findViewById(R.id.btnLocal);
        btnRemote = findViewById(R.id.btnRemote);

        ((MaterialToolbar) findViewById(R.id.toolbarWeb))
                .setNavigationOnClickListener(view -> finish());

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressWeb.setVisibility(View.GONE);
                errorPanel.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {
                if (request.isForMainFrame()) {
                    showError(getString(R.string.webview_error_sub));
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressWeb.setProgress(newProgress);
                progressWeb.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
            }
        });

        btnLocal.setOnClickListener(view -> open(false));
        btnRemote.setOnClickListener(view -> open(true));
        findViewById(R.id.btnWebRetry).setOnClickListener(view -> open(remoteMode));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (remoteMode && webView.canGoBack()) {
                    webView.goBack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        open(getIntent().getBooleanExtra(EXTRA_REMOTE, false));
    }

    private void open(boolean remote) {
        remoteMode = remote;
        btnLocal.setAlpha(remote ? 0.55f : 1f);
        btnRemote.setAlpha(remote ? 1f : 0.55f);
        errorPanel.setVisibility(View.GONE);
        progressWeb.setProgress(0);
        progressWeb.setVisibility(View.VISIBLE);
        if (remote) {
            webView.loadUrl(REMOTE_URL);
        } else {
            webView.loadDataWithBaseURL(null, buildBulletinHtml(), "text/html", "UTF-8", null);
        }
    }

    private void showError(String message) {
        progressWeb.setVisibility(View.GONE);
        tvWebError.setText(message);
        errorPanel.setVisibility(View.VISIBLE);
    }

    private String buildBulletinHtml() {
        List<Product> products = repository.getProducts(
                session.isLoggedIn() ? session.getUserId() : 0);

        StringBuilder body = new StringBuilder();
        String currentCategory = null;
        for (Product product : products) {
            if (!product.getCategory().equals(currentCategory)) {
                if (currentCategory != null) {
                    body.append("</table></div>");
                }
                currentCategory = product.getCategory();
                body.append("<div class=\"card\"><h2>")
                        .append(Html.escapeHtml(currentCategory))
                        .append("</h2><table><tr><th>Réf.</th><th>Produit</th>")
                        .append("<th class=\"right\">Prix</th>")
                        .append("<th class=\"right\">Variation</th></tr>");
            }

            double percent = product.getTrendPercent();
            String trendClass = percent > 0.05 ? "up" : percent < -0.05 ? "down" : "flat";
            String arrow = percent > 0.05 ? "▲ " : percent < -0.05 ? "▼ " : "= ";

            body.append("<tr><td>").append(Html.escapeHtml(product.getRef()))
                    .append("</td><td>").append(Html.escapeHtml(product.getName()))
                    .append("</td><td class=\"right\">")
                    .append(Html.escapeHtml(FormatUtils.price(product.getPrice())))
                    .append("<span class=\"unit\">/")
                    .append(Html.escapeHtml(product.getUnit()))
                    .append("</span></td><td class=\"right ").append(trendClass)
                    .append("\">").append(arrow)
                    .append(Html.escapeHtml(FormatUtils.change(percent)))
                    .append("</td></tr>");
        }
        if (currentCategory != null) {
            body.append("</table></div>");
        }
        if (products.isEmpty()) {
            body.append("<div class=\"card\"><p>Aucun produit dans le catalogue.</p></div>");
        }

        String subtitle = getString(R.string.bulletin_subtitle,
                Html.escapeHtml(FormatUtils.dateLong(System.currentTimeMillis())));

        return "<!DOCTYPE html><html lang=\"fr\"><head><meta charset=\"utf-8\"/>"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>"
                + "<style>"
                + "*{box-sizing:border-box}"
                + "body{margin:0;padding:16px;background:#F5F8F3;color:#1C2923;"
                + "font-family:-apple-system,Roboto,Helvetica,Arial,sans-serif}"
                + "header{background:linear-gradient(135deg,#1B5E20,#388E3C);color:#fff;"
                + "border-radius:18px;padding:18px;margin-bottom:14px}"
                + "h1{font-size:20px;margin:0 0 4px}"
                + ".sub{margin:0;font-size:12px;opacity:.85}"
                + ".card{background:#fff;border:1px solid #E2E9DF;border-radius:16px;"
                + "padding:14px;margin-bottom:12px}"
                + "h2{font-size:15px;color:#2E7D32;margin:0 0 8px;text-transform:capitalize}"
                + "table{width:100%;border-collapse:collapse;font-size:13px}"
                + "th{text-align:left;color:#6B7A70;font-weight:600;font-size:11px;"
                + "text-transform:uppercase;letter-spacing:.03em;padding:4px 2px;"
                + "border-bottom:1px solid #E2E9DF}"
                + "td{padding:8px 2px;border-bottom:1px solid #F0F5EE;vertical-align:middle}"
                + "tr:last-child td{border-bottom:none}"
                + ".right{text-align:right}"
                + ".unit{font-size:11px;color:#6B7A70;font-weight:400}"
                + ".up{color:#C62828;font-weight:700;font-size:12px}"
                + ".down{color:#2E7D32;font-weight:700;font-size:12px}"
                + ".flat{color:#6B7A70;font-weight:600;font-size:12px}"
                + "footer{font-size:10px;color:#9BA79F;text-align:center;"
                + "margin-top:18px;line-height:1.5}"
                + "</style></head><body>"
                + "<header><h1>" + Html.escapeHtml(getString(R.string.bulletin_title)) + "</h1>"
                + "<p class=\"sub\">" + subtitle + "</p></header>"
                + body
                + "<footer>" + Html.escapeHtml(getString(R.string.bulletin_footer)) + "</footer>"
                + "</body></html>";
    }
}
