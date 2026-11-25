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
import android.widget.Button;
import android.widget.PopupMenu;
public class MainActivity extends Activity {
 private static final String HOME="https://djstage.pl/djkatalog/app/mobile_v301.php";
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
  }
  @Override public void onPageFinished(WebView view,String url){
    if(url.startsWith("https://djstage.pl/djkatalog/")&&!url.contains("mobile_v301.php")){
      view.evaluateJavascript("(function(){let h=document.querySelector('header');if(h){let nav=h.querySelector('nav');if(nav)nav.style.display='none';let a=h.querySelector('a.brand');if(a){a.removeAttribute('href');a.style.pointerEvents='none'}}})();",null);
    }
  }});
  LinearLayout bar=new LinearLayout(this);bar.setOrientation(LinearLayout.HORIZONTAL);bar.setBackgroundColor(0xff112d3e);
  Button start=new Button(this);start.setText("⌂ START");start.setAllCaps(false);start.setTextSize(12);start.setTextColor(0xffb1fbe8);start.setOnClickListener(v->web.loadUrl(HOME));
  Button panel=new Button(this);panel.setText("▣ PANEL DJ");panel.setAllCaps(false);panel.setTextSize(12);panel.setTextColor(0xffb1fbe8);panel.setOnClickListener(v->web.loadUrl(HOME+"#dashboard"));
  Button more=new Button(this);more.setText("⋮");more.setOnClickListener(v->{PopupMenu popup=new PopupMenu(this,v);popup.getMenu().add(0,1,0,"Regulamin");popup.getMenu().add(0,2,1,"Kontakt");popup.setOnMenuItemClickListener(item->{web.loadUrl("https://djstage.pl/djkatalog/?a="+(item.getItemId()==1?"terms":"contact")+"&djapp=1");return true;});popup.show();});
  bar.addView(start,new LinearLayout.LayoutParams(0,54,1));bar.addView(panel,new LinearLayout.LayoutParams(0,54,1));bar.addView(more,new LinearLayout.LayoutParams(50,54));
  root.addView(bar,new LinearLayout.LayoutParams(-1,(int)(56*getResources().getDisplayMetrics().density)));
  root.addView(web,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);root.requestApplyInsets();web.loadUrl(HOME);
 }
 @Override public void onBackPressed(){if(web!=null&&web.canGoBack()){web.goBack();return;}super.onBackPressed();}
 @Override protected void onDestroy(){if(web!=null){root.removeView(web);web.destroy();web=null;}super.onDestroy();}
}