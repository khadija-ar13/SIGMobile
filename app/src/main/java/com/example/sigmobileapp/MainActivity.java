package com.example.sigmobileapp;
import org.json.JSONObject;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialiser le WebView
        webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        // Ajouter l'interface JavaScript pour la communication bidirectionnelle
        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        // Charger la page HTML locale
        webView.loadUrl("file:///android_asset/assets_cartes_thématiques/exemple.html");

        // Attendre que la page soit complètement chargée avant d'envoyer des données à JavaScript
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Charger et envoyer les données GeoJSON (regions.json) à JavaScript
                String geoJsonData = loadJSONFromAsset("regions.json");
                if (geoJsonData != null) {
                    // Envoyer les données GeoJSON à JavaScript
                    webView.evaluateJavascript("loadGeoJsonData(" + JSONObject.quote(geoJsonData) + ")", null);
                } else {
                    Toast.makeText(MainActivity.this, "Impossible de charger le fichier JSON", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Méthode pour charger le fichier JSON depuis les assets
    private String loadJSONFromAsset(String fileName) {
        StringBuilder json = new StringBuilder();
        try {
            InputStream is = getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return json.toString();
    }

    // Classe interne pour gérer la communication avec JavaScript
    public class WebAppInterface {

        @JavascriptInterface
        public void showToast(String message) {
            // Afficher un toast
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();

            // Préparer un message à envoyer à JavaScript
            String messageAndroid = "<b style='color:red;'>Salut depuis Android !!</b>";
            String encodedMessage = messageAndroid.replace("'", "\\'").replace("\"", "\\\"");

            // Envoyer des données à JavaScript
            webView.post(() -> webView.evaluateJavascript(
                    String.format(Locale.US, "receiveDataFromAndroid('%s')", encodedMessage), null
            ));
        }
    }
}
