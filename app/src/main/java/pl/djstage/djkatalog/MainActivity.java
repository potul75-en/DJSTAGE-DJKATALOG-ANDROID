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
  private static final String HOME="https://djstage.pl/djkatalog/app/mobile_v302.php";
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
        if(!url.startsWith("https://djstage.pl/djkatalog/"))return;
        if(url.contains("mobile_v302.php")){
          view.evaluateJavascript("(function(){document.documentElement.classList.add('dj-native-nav');})();",null);
        }else{
          // App-only presentation: no public website header navigation or external logo links.
          // Existing portal functions remain intact, including login, CMS, bookings and calendar.
          view.evaluateJavascript(
            "(function(){document.documentElement.classList.add('dj-native-app');"
            +"var h=document.querySelector('header');"
            +"if(h){var n=h.querySelector('nav');if(n){n.innerHTML='<a href=\"/djkatalog/?a=terms&djapp=1\">Regulamin</a> <a href=\"/djkatalog/?a=contact&djapp=1\">Kontakt</a>';}"
            +"var logo=h.querySelector('a.brand');if(logo){logo.removeAttribute('href');logo.style.pointerEvents='none';logo.setAttribute('aria-label','DJKATALOG');}}"
            +"var s=document.getElementById('djv302style');if(!s){s=document.createElement('style');s.id='djv302style';s.textContent="
            +"'html,body{max-width:100%;overflow-x:hidden!important;background:#07101e!important;color:#edf8ff!important}'"
            +"+'body main,body .dk-panel-body,body .dk-panel-tiles,body .dk-panel-layout,body .dk-panel-content{min-width:0!important;max-width:100%!important}'"
            +"+'body main{width:100%!important;padding:14px!important;box-sizing:border-box!important}'"
            +"+'body section,body form,body .dk-panel-tiles>*,body .dk-calendar{min-width:0!important;max-width:100%!important;box-sizing:border-box!important}'"
            +"+'body input:not([type=checkbox]),body textarea,body select,body img,body video{max-width:100%!important;box-sizing:border-box!important}'"
            +"+'body section,body .dk-panel-tiles>*,body .dk-panel-heading{background:#14283c!important;border:1px solid #23526a!important;border-radius:15px!important;color:#edf8ff!important;padding:12px!important}'"
            +"+'body h1,body h2,body h3{color:#f5faff!important}'"
            +"+'body a.cta,body button[type=submit]{background:#33e8c0!important;color:#062029!important;border-radius:12px!important}'"
            +"+'body header{background:#091b2f!important;display:flex!important;flex-wrap:wrap!important;gap:5px;padding:8px 12px!important;align-items:center}'"
            +"+'body header nav{display:flex!important;gap:12px!important;font-size:12px!important}'"
            +"+'body header nav a{color:#6df7d8!important}'"
            +"+'body .dk-panel-tiles{display:grid!important;grid-template-columns:repeat(auto-fit,minmax(min(180px,100%),1fr))!important;gap:10px!important}'"
            +"+'body .dk-gallery,body .dk-gallery-track,body .dk-booking,body .dk-day{min-width:0;max-width:100%}'"
            +";s.appendChild(document.head);}})();",null);
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
    button.setOnClickListener(v->web.loadUrl(HOME+"#"+page));
    nav.addView(button,new LinearLayout.LayoutParams(0,-1,1));
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