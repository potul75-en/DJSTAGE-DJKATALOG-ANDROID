package pl.djstage.djkatalog;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.graphics.Color;
import android.graphics.Insets;
import android.net.Uri;
import android.view.View;
import android.view.WindowInsets;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;

public class MainActivity extends Activity {
  private static final String HOME="https://djstage.pl/djkatalog/app/mobile_v310.php";
  private WebView web;
  private LinearLayout root;
  private final int ink=Color.rgb(190,218,229), accent=Color.rgb(65,237,203);
  private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}

  @Override public void onCreate(Bundle state){
    super.onCreate(state);
    getWindow().setStatusBarColor(Color.rgb(7,15,30));
    getWindow().setNavigationBarColor(Color.rgb(7,15,30));
    root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(0xff07101e);
    root.setOnApplyWindowInsetsListener((view,insets)->{
      int top,bottom,left,right;
      if(Build.VERSION.SDK_INT>=30){
        Insets safe=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());
        top=safe.top;bottom=safe.bottom;left=safe.left;right=safe.right;
      }else{top=insets.getSystemWindowInsetTop();bottom=insets.getSystemWindowInsetBottom();left=0;right=0;}
      // Keep all Android app navigation ABOVE the Android system navigation/gesture bar.
      view.setPadding(left,top,right,bottom);
      return insets;
    });

    web=new WebView(this);
    WebSettings settings=web.getSettings();
    settings.setJavaScriptEnabled(true);
    settings.setDomStorageEnabled(true);
    settings.setAllowFileAccess(false);
    settings.setAllowContentAccess(false);
    settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
    if(Build.VERSION.SDK_INT>=26)settings.setSafeBrowsingEnabled(true);
    WebView.setWebContentsDebuggingEnabled(false);
    CookieManager.getInstance().setAcceptCookie(true);
    CookieManager.getInstance().setAcceptThirdPartyCookies(web,false);
    web.setBackgroundColor(0xff07101e);
    web.setWebChromeClient(new WebChromeClient());
    web.setWebViewClient(new WebViewClient(){
      @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){
        Uri uri=request.getUrl();
        return !"https".equals(uri.getScheme())||!"djstage.pl".equals(uri.getHost())
          ||uri.getPath()==null||!uri.getPath().startsWith("/djkatalog/");
      }
      @Override public void onPageFinished(WebView view,String url){
        if(url==null||!url.startsWith("https://djstage.pl/djkatalog/"))return;
        if(url.contains("/djkatalog/app/mobile_v310.php")){
          view.evaluateJavascript("document.documentElement.classList.add('dj-native-nav');",null);
        }else{
          // Android app-only styling; public WWW keeps its original presentation.
          // Do NOT set main width globally: profile gallery & booking fit their parents.
          view.evaluateJavascript("(function(){var d=document;d.documentElement.classList.add('dj-app-v310');"
            +"var head=d.head||d.documentElement;"
            +"if(!d.getElementById('djv310css')){var l=d.createElement('link');l.id='djv310css';l.rel='stylesheet';"
            +"l.href='/djkatalog/app/dj_app_v310.css?v=310';head.appendChild(l);}"
            +"if(!d.getElementById('djv310js')){var s=d.createElement('script');s.id='djv310js';"
            +"s.src='/djkatalog/app/dj_app_v310.js?v=310';s.defer=true;head.appendChild(s);}})();",null);
        }
      }
    });

    root.addView(web,new LinearLayout.LayoutParams(-1,0,1));
    LinearLayout nav=new LinearLayout(this);
    nav.setOrientation(LinearLayout.HORIZONTAL);
    nav.setBackgroundColor(0xff10273b);
    nav.setPadding(dp(2),dp(3),dp(2),dp(3));
    addNav(nav,"⌂","Start","home");
    addNav(nav,"⌕","Szukaj","search");
    addNav(nav,"♡","Ulubione","favorites");
    addNav(nav,"☏","Czat","chat");
    addNav(nav,"▣","Panel DJ","dashboard");
    addNav(nav,"♙","Profil","profile");
    root.addView(nav,new LinearLayout.LayoutParams(-1,dp(61)));
    setContentView(root);
    root.requestApplyInsets();
    web.loadUrl(HOME);
  }

  private void addNav(LinearLayout nav,String icon,String title,String page){
    LinearLayout button=new LinearLayout(this);
    button.setGravity(Gravity.CENTER);
    button.setOrientation(LinearLayout.VERTICAL);
    button.setMinimumHeight(dp(50));
    TextView symbol=new TextView(this);
    symbol.setGravity(Gravity.CENTER);
    symbol.setText(icon);symbol.setTextSize(21);symbol.setTextColor(accent);
    TextView text=new TextView(this);
    text.setGravity(Gravity.CENTER);
    text.setSingleLine(true);text.setTextSize(9);text.setTextColor(ink);text.setText(title);
    button.addView(symbol,new LinearLayout.LayoutParams(-1,dp(26)));
    button.addView(text,new LinearLayout.LayoutParams(-1,dp(20)));
    button.setContentDescription(title);
    button.setOnClickListener(v->navigateNative(page));
    nav.addView(button,new LinearLayout.LayoutParams(0,-1,1));
  }
  /** Native navigation calls the active SPA, rather than changing only an inert hash. */
  private void navigateNative(String page){
    if(!("home".equals(page)||"search".equals(page)||"favorites".equals(page)
      ||"chat".equals(page)||"dashboard".equals(page)||"profile".equals(page)))return;
    String current=web.getUrl();
    if(current!=null&&current.startsWith(HOME)){
      web.evaluateJavascript("(function(){return !!(window.DJKATALOG_NAV&&window.DJKATALOG_NAV.go('"+page+"'));})()",value->{
        if(!"true".equals(value)&&web!=null)web.loadUrl(HOME+"#"+page);
      });
    }else{web.loadUrl(HOME+"#"+page);}
  }
  @Override public void onBackPressed(){
    if(web!=null&&web.canGoBack()){web.goBack();return;}
    super.onBackPressed();
  }
  @Override protected void onDestroy(){
    if(web!=null){root.removeView(web);web.destroy();web=null;}
    super.onDestroy();
  }
}