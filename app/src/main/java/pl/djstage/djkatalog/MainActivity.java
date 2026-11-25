package pl.djstage.djkatalog;
import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.graphics.Color;
import android.graphics.Insets;
import android.net.Uri;
import android.view.WindowInsets;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.widget.LinearLayout;
public class MainActivity extends Activity {
 private static final String HOME="https://djstage.pl/djkatalog/app/mobile_v300.php";
 private WebView web;private LinearLayout root;
 @Override public void onCreate(Bundle state){
  super.onCreate(state);
  getWindow().setStatusBarColor(Color.rgb(7,15,30));getWindow().setNavigationBarColor(Color.rgb(7,15,30));
  root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(Color.rgb(7,15,30));
  root.setOnApplyWindowInsetsListener((view,insets)->{
   int top,bottom,left,right;
   if(Build.VERSION.SDK_INT>=30){Insets safe=insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());top=safe.top;bottom=safe.bottom;left=safe.left;right=safe.right;}
   else{top=insets.getSystemWindowInsetTop();bottom=insets.getSystemWindowInsetBottom();left=0;right=0;}
   view.setPadding(left,top,right,bottom);return insets;
  });
  web=new WebView(this);WebSettings settings=web.getSettings();
  settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);
  settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);
  settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
  if(Build.VERSION.SDK_INT>=26)settings.setSafeBrowsingEnabled(true);
  WebView.setWebContentsDebuggingEnabled(false);
  CookieManager.getInstance().setAcceptCookie(true);CookieManager.getInstance().setAcceptThirdPartyCookies(web,false);
  web.setBackgroundColor(Color.rgb(7,15,30));web.setWebChromeClient(new WebChromeClient());
  web.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){
    Uri url=request.getUrl();return !"https".equals(url.getScheme())||!"djstage.pl".equals(url.getHost())||url.getPath()==null||!url.getPath().startsWith("/djkatalog/");
  }});
  root.addView(web,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);root.requestApplyInsets();web.loadUrl(HOME);
 }
 @Override public void onBackPressed(){if(web!=null&&web.canGoBack()){web.goBack();return;}super.onBackPressed();}
 @Override protected void onDestroy(){if(web!=null){root.removeView(web);web.destroy();web=null;}super.onDestroy();}
}