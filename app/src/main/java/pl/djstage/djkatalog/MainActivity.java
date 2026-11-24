package pl.djstage.djkatalog;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.Gravity;
import android.view.WindowInsets;
import android.graphics.Insets;
import android.os.Build;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.widget.*;
import android.text.TextUtils;
import org.json.*;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
 private static final String BASE="https://djstage.pl";
 private static final int BG=0xff0b1023, PANEL=0xff1a2140, INK=Color.WHITE, MUTED=0xffb9c4dd, ACCENT=0xffad84ff;
 private final ExecutorService executor=Executors.newFixedThreadPool(3);
 private LinearLayout root,content;
 private ScrollView scrolling;
 private WebView browser;
 private int profile=0;
 private int dp(int value){return (int)(getResources().getDisplayMetrics().density*value+.5f);}
 @Override public void onCreate(Bundle state){super.onCreate(state); getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG); catalog("");}
 private TextView label(String txt,int sp,int color){TextView v=new TextView(this);v.setText(txt);v.setTextColor(color);v.setTextSize(sp);v.setPadding(dp(10),dp(10),dp(10),dp(10));return v;}
 private LinearLayout column(){LinearLayout x=new LinearLayout(this);x.setOrientation(1);return x;}
 private Button button(String title,Runnable fn){Button b=new Button(this);b.setAllCaps(false);b.setText(title);b.setOnClickListener(v->fn.run());return b;}
 private void screen(String title){
  if(browser!=null){browser.destroy();browser=null;}
  root=column();root.setBackgroundColor(BG);
  // Android 15+: adjust content insets for both 3-button and gesture navigation.
  root.setOnApplyWindowInsetsListener((view, insets) -> {
    int safeTop, safeBottom;
    if (Build.VERSION.SDK_INT >= 30) {
      Insets safe=insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
      safeTop=safe.top;safeBottom=safe.bottom;
    } else {
      safeTop=insets.getSystemWindowInsetTop();
      safeBottom=insets.getSystemWindowInsetBottom();
    }
    view.setPadding(0,safeTop,0,safeBottom);
    return insets;
  });
  setContentView(root);
  root.requestApplyInsets();
  root.addView(label("DJ-KATALOG   •   "+title,21,INK));
  scrolling=new ScrollView(this); scrolling.setFillViewport(true); content=column();content.setPadding(dp(8),dp(6),dp(8),dp(12));scrolling.addView(content);
  root.addView(scrolling,new LinearLayout.LayoutParams(-1,0,1));
  LinearLayout menu=new LinearLayout(this); root.addView(menu);
  Button a=button("DJ-e",()->catalog("")); Button b=button("Szukaj",()->catalog(""));Button c=button("Panel DJ-a",()->web(BASE+"/djkatalog/?a=panel"));
  menu.addView(a,new LinearLayout.LayoutParams(0,dp(56),1));menu.addView(b,new LinearLayout.LayoutParams(0,dp(56),1));menu.addView(c,new LinearLayout.LayoutParams(0,dp(56),1));
 }
 private JSONObject api(String query)throws Exception {
  URL url=new URL(BASE+"/djkatalog/app/api.php?"+query);
  HttpURLConnection conn=(HttpURLConnection)url.openConnection();
  conn.setConnectTimeout(12000);conn.setReadTimeout(12000);conn.setInstanceFollowRedirects(false);
  try{if(conn.getResponseCode()!=200)throw new Exception("HTTP "+conn.getResponseCode()); try(InputStream in=conn.getInputStream()){byte[] bytes=in.readNBytes(1024*1024);return new JSONObject(new String(bytes,StandardCharsets.UTF_8));}}
  finally{conn.disconnect();}
 }
 private void async(String query,java.util.function.Consumer<JSONObject> done,TextView status){
  executor.execute(()->{try{JSONObject data=api(query);if(!data.optBoolean("ok",false))throw new Exception(data.optString("error"));runOnUiThread(()->{if(isFinishing()||isDestroyed())return;done.accept(data);});}
  catch(Exception error){runOnUiThread(()->{if(status!=null)status.setText("Nie można wczytać danych. Sprawdź połączenie i spróbuj ponownie.");});}});
 }
 private void catalog(String value){
  profile=0;screen("ODKRYWAJ DJ-ÓW");
  TextView title=label("Odkryj swojego DJ-a",24,INK);content.addView(title);
  EditText search=new EditText(this);search.setHint("Nazwa DJ-a lub miasto");search.setTextColor(INK);search.setHintTextColor(MUTED);search.setSingleLine(true);search.setText(value);search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);content.addView(search);
  LinearLayout results=column();content.addView(results);
  Runnable load=()->{
   results.removeAllViews();TextView status=label("Wczytywanie katalogu…",14,MUTED);results.addView(status);
   try{String q=URLEncoder.encode(search.getText().toString(),"UTF-8");async("mode=catalog&q="+q, data->{
    results.removeAllViews();JSONArray items=data.optJSONArray("items");
    if(items==null||items.length()==0){results.addView(label("Brak profili",16,MUTED));return;}
    for(int i=0;i<items.length();i++){JSONObject p=items.optJSONObject(i);if(p!=null)card(results,p);}
   },status);}catch(Exception ignored){status.setText("Błędne wyszukiwanie");}
  };
  content.addView(button("Szukaj DJ-a",load));
  search.setOnEditorActionListener((v,action,event)->{load.run();return true;});
  load.run();
 }
 private void card(LinearLayout parent,JSONObject data){
  int id=data.optInt("id");LinearLayout row=new LinearLayout(this);row.setPadding(dp(9),dp(9),dp(9),dp(9));row.setBackgroundColor(PANEL);
  LinearLayout.LayoutParams margin=new LinearLayout.LayoutParams(-1,-2);margin.setMargins(0,0,0,dp(8));parent.addView(row,margin);
  ImageView picture=new ImageView(this);picture.setScaleType(ImageView.ScaleType.CENTER_CROP);row.addView(picture,new LinearLayout.LayoutParams(dp(94),dp(98)));image(picture,data.optString("image"));
  LinearLayout info=column();info.addView(label(data.optString("name"),19,INK));info.addView(label(data.optString("city")+" · "+data.optString("genres"),12,MUTED));
  row.addView(info,new LinearLayout.LayoutParams(0,-2,1));row.setOnClickListener(v->detail(id));
 }
 private void image(ImageView image,String path){
  if(path==null||path.isEmpty()||path.equals("null"))return;
  executor.execute(()->{HttpURLConnection c=null;try{URL url=new URL(path.startsWith("/")?BASE+path:path);if(!"https".equals(url.getProtocol()))return;c=(HttpURLConnection)url.openConnection();c.setConnectTimeout(9000);c.setReadTimeout(9000);try(InputStream in=c.getInputStream()){Bitmap b=BitmapFactory.decodeStream(in);if(b!=null)runOnUiThread(()->{if(!isFinishing()&&!isDestroyed())image.setImageBitmap(b);});}}
  catch(Exception ignored){}finally{if(c!=null)c.disconnect();}});
 }
 private void detail(int id){
  profile=id;screen("PROFIL");content.addView(button("‹ Katalog",()->catalog("")));
  TextView status=label("Wczytywanie profilu…",15,MUTED);content.addView(status);
  async("mode=profile&id="+id, d->{content.removeView(status);JSONObject p=d.optJSONObject("profile");if(p==null)return;
   ImageView hero=new ImageView(this);hero.setScaleType(ImageView.ScaleType.CENTER_CROP);content.addView(hero,new LinearLayout.LayoutParams(-1,dp(170)));image(hero,p.optString("hero"));
   content.addView(label(p.optString("name"),26,INK));content.addView(label(p.optString("city")+" • "+p.optString("genres"),14,MUTED));
   content.addView(label("O artyście",19,ACCENT));content.addView(label(p.optString("bio"),15,INK));
   JSONArray gallery=p.optJSONArray("gallery");if(gallery!=null&&gallery.length()>0){content.addView(label("Galeria zdjęć",19,ACCENT));HorizontalScrollView slider=new HorizontalScrollView(this);LinearLayout items=new LinearLayout(this);items.setOrientation(0);
    for(int i=0;i<gallery.length();i++){ImageView iv=new ImageView(this);iv.setScaleType(ImageView.ScaleType.CENTER_CROP);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(225),dp(180));lp.rightMargin=dp(8);items.addView(iv,lp);image(iv,gallery.optString(i));}
    slider.addView(items);content.addView(slider);
   }
   content.addView(button("Otwórz profil WWW",()->web(BASE+"/djkatalog/?a=profile&id="+id)));
  },status);
 }
 private void web(String url){
  profile=0;screen("PANEL DJ-A");
  // Panel replaces the scrollable catalog: no fixed-height WebView and no obstructive bottom app menu.
  root.removeView(scrolling);
  root.removeViewAt(root.getChildCount()-1);
  root.addView(button("← Powrót do katalogu",()->catalog("")));
  browser=new WebView(this);
  browser.getSettings().setJavaScriptEnabled(true);
  browser.getSettings().setDomStorageEnabled(true);
  browser.getSettings().setAllowFileAccess(false);
  browser.getSettings().setAllowContentAccess(false);
  browser.getSettings().setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW);
  if(Build.VERSION.SDK_INT>=26)browser.getSettings().setSafeBrowsingEnabled(true);
  WebView.setWebContentsDebuggingEnabled(false);
  android.webkit.CookieManager.getInstance().setAcceptCookie(true);
  android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(browser,false);
  browser.setWebChromeClient(new WebChromeClient());
  browser.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest r){UriCheck check=new UriCheck(r.getUrl().getScheme(),r.getUrl().getHost());return !check.allowed();}});
  root.addView(browser,new LinearLayout.LayoutParams(-1,0,1));
  browser.loadUrl(url);
 }
 private static class UriCheck{
  private final String scheme,host;UriCheck(String s,String h){scheme=s;host=h;}
  boolean allowed(){return "https".equals(scheme)&&"djstage.pl".equals(host);}
 }
 @Override public void onBackPressed(){if(browser!=null&&browser.canGoBack()){browser.goBack();return;}if(profile!=0||browser!=null){catalog("");return;}super.onBackPressed();}
 @Override protected void onDestroy(){if(browser!=null)browser.destroy();executor.shutdownNow();super.onDestroy();}
}
