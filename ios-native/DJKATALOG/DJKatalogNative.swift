import SwiftUI
import WebKit
import UIKit

@main
struct DJKatalogNativeApp: App {
    @StateObject private var browser = DJBrowser()
    var body: some Scene {
        WindowGroup { DJKatalogRoot().environmentObject(browser) }
    }
}

private enum UITheme {
    static let bg = Color(red: 7/255, green: 16/255, blue: 30/255)
    static let bar = Color(red: 16/255, green: 39/255, blue: 59/255)
    static let accent = Color(red: 65/255, green: 237/255, blue: 203/255)
    static let text = Color(red: 194/255, green: 215/255, blue: 230/255)
}

private enum Section: String, CaseIterable, Identifiable {
    case home, search, favorites, chat, dashboard, profile
    var id: String { rawValue }
    var title: String {
        switch self {
        case .home: return "Start"
        case .search: return "Szukaj"
        case .favorites: return "Ulubione"
        case .chat: return "Czat"
        case .dashboard: return "Panel DJ"
        case .profile: return "Profil"
        }
    }
    var icon: String {
        switch self {
        case .home: return "house"
        case .search: return "magnifyingglass"
        case .favorites: return "heart"
        case .chat: return "bubble.left.and.bubble.right"
        case .dashboard: return "square.grid.2x2"
        case .profile: return "person.crop.circle"
        }
    }
}

private struct DJKatalogRoot: View {
    @EnvironmentObject var browser: DJBrowser
    var body: some View {
        VStack(spacing: 0) {
            if browser.canGoBack {
                HStack {
                    Button(action: browser.goBack) {
                        Label("Wróć", systemImage: "chevron.left")
                            .font(.system(size: 13, weight: .semibold))
                    }
                    Spacer()
                    Text("DJKATALOG").font(.system(size: 15, weight: .bold))
                    Spacer()
                }
                .foregroundStyle(UITheme.accent)
                .padding(.horizontal, 14)
                .frame(height: 42)
                .background(UITheme.bar)
            }
            DJWebView(browser: browser)
                .background(UITheme.bg)
            HStack(spacing: 0) {
                ForEach(Section.allCases) { item in
                    Button {
                        browser.navigate(item)
                    } label: {
                        VStack(spacing: 3) {
                            Image(systemName: item.icon)
                                .font(.system(size: 19, weight: .medium))
                                .frame(height: 23)
                            Text(item.title)
                                .font(.system(size: 9))
                                .lineLimit(1)
                                .minimumScaleFactor(0.7)
                        }
                        .foregroundStyle(item == browser.current ? UITheme.accent : UITheme.text)
                        .frame(maxWidth: .infinity)
                        .frame(height: 57)
                        .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel(item.title)
                }
            }
            .padding(.horizontal, 3)
            .background(UITheme.bar)
        }
        .background(UITheme.bg.ignoresSafeArea())
    }
}

private struct DJWebView: UIViewRepresentable {
    let browser: DJBrowser
    func makeUIView(context: Context) -> WKWebView { browser.webView }
    func updateUIView(_ view: WKWebView, context: Context) {}
}

final class DJBrowser: NSObject, ObservableObject, WKNavigationDelegate, WKUIDelegate {
    private static let home = URL(string: "https://djstage.pl/djkatalog/app/mobile_v310.php")!
    private static let origin = URL(string: "https://djstage.pl")!
    @Published var canGoBack = false
    @Published private(set) var current: Section = .home
    let webView: WKWebView

    override init() {
        let cfg = WKWebViewConfiguration()
        cfg.websiteDataStore = .default()
        cfg.defaultWebpagePreferences.allowsContentJavaScript = true
        cfg.allowsInlineMediaPlayback = true
        webView = WKWebView(frame: .zero, configuration: cfg)
        super.init()
        webView.navigationDelegate = self
        webView.uiDelegate = self
        webView.isOpaque = false
        webView.backgroundColor = UIColor(red: 7/255, green: 16/255, blue: 30/255, alpha: 1)
        webView.scrollView.backgroundColor = webView.backgroundColor
        webView.scrollView.contentInsetAdjustmentBehavior = .never
        // Keep horizontal gestures inside the DJ calendar.
        webView.allowsBackForwardNavigationGestures = false
        webView.load(URLRequest(url: Self.home))
    }

    func navigate(_ destination: Section) {
        current = destination
        if webView.url?.path == Self.home.path {
            let js = "(function(){try{return !!(window.DJKATALOG_NAV&&window.DJKATALOG_NAV.go('\(destination.rawValue)'));}catch(e){return false;}})()"
            webView.evaluateJavaScript(js) { [weak self] result, error in
                guard let self else { return }
                if error != nil || (result as? Bool) != true { self.openHome(destination) }
            }
        } else {
            openHome(destination)
        }
    }

    private func openHome(_ destination: Section) {
        guard let link = URL(string: Self.home.absoluteString + "#" + destination.rawValue) else { return }
        webView.load(URLRequest(url: link))
    }

    func goBack() {
        if webView.canGoBack { webView.goBack() }
        else { navigate(.dashboard) }
    }

    private func updateState() {
        DispatchQueue.main.async { self.canGoBack = self.webView.canGoBack }
    }

    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        updateState()
        guard let link = webView.url, link.host == "djstage.pl", link.path.hasPrefix("/djkatalog/") else { return }
        if link.path == Self.home.path {
            if let fragment = link.fragment, let tab = Section(rawValue: fragment) { current = tab }
            webView.evaluateJavaScript("document.documentElement.classList.add('dj-native-nav');", completionHandler: nil)
        } else {
            webView.evaluateJavaScript(Self.fullPanelStyle, completionHandler: nil)
        }
    }

    private static let fullPanelStyle = #"""
    (function(){
      document.documentElement.classList.add('dj-app-v310');
      if(!document.getElementById('dj-ios-app-style')){
        const css=document.createElement('link');
        css.id='dj-ios-app-style';css.rel='stylesheet';
        css.href='/djkatalog/app/dj_app_v310.css?v=310';
        document.head.appendChild(css);
      }
      if(!document.getElementById('dj-ios-app-gestures')){
        const s=document.createElement('script');
        s.id='dj-ios-app-gestures';s.src='/djkatalog/app/dj_app_v310.js?v=310';
        document.head.appendChild(s);
      }
      const header=document.querySelector('header');
      if(header){
        const nav=header.querySelector('nav');
        if(nav){nav.innerHTML='<a href="/djkatalog/?a=terms&djapp=1">Regulamin</a> <a href="/djkatalog/?a=contact&djapp=1">Kontakt</a>';}
        const logo=header.querySelector('a.brand');
        if(logo){logo.removeAttribute('href');logo.style.pointerEvents='none';}
      }
    })();
    """#

    func webView(_ webView: WKWebView, decidePolicyFor action: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
        guard let url = action.request.url else { decisionHandler(.cancel); return }
        if url.scheme == "https" && url.host == "djstage.pl" && url.path.hasPrefix("/djkatalog/") {
            decisionHandler(.allow)
        } else {
            decisionHandler(.cancel)
            if url.scheme == "https" || url.scheme == "tel" || url.scheme == "mailto" {
                DispatchQueue.main.async { UIApplication.shared.open(url) }
            }
        }
    }

    func webView(_ webView: WKWebView, runJavaScriptAlertPanelWithMessage message: String,
                 initiatedByFrame frame: WKFrameInfo, completionHandler: @escaping () -> Void) {
        let alert = UIAlertController(title: "DJKATALOG", message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "OK", style: .default) { _ in completionHandler() })
        guard let root = rootController() else { completionHandler(); return }
        root.present(alert, animated: true)
    }

    func webView(_ webView: WKWebView, runJavaScriptConfirmPanelWithMessage message: String,
                 initiatedByFrame frame: WKFrameInfo, completionHandler: @escaping (Bool) -> Void) {
        let alert = UIAlertController(title: "DJKATALOG", message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Nie", style: .cancel) { _ in completionHandler(false) })
        alert.addAction(UIAlertAction(title: "Tak", style: .default) { _ in completionHandler(true) })
        guard let root = rootController() else { completionHandler(false); return }
        root.present(alert, animated: true)
    }

    func webView(_ webView: WKWebView, runJavaScriptTextInputPanelWithPrompt prompt: String,
                 defaultText: String?, initiatedByFrame frame: WKFrameInfo,
                 completionHandler: @escaping (String?) -> Void) {
        let alert = UIAlertController(title: "DJKATALOG", message: prompt, preferredStyle: .alert)
        alert.addTextField { $0.text = defaultText }
        alert.addAction(UIAlertAction(title: "Anuluj", style: .cancel) { _ in completionHandler(nil) })
        alert.addAction(UIAlertAction(title: "OK", style: .default) { _ in completionHandler(alert.textFields?.first?.text) })
        guard let root = rootController() else { completionHandler(nil); return }
        root.present(alert, animated: true)
    }

    private func rootController() -> UIViewController? {
        let scenes = UIApplication.shared.connectedScenes.compactMap { $0 as? UIWindowScene }
        return scenes.flatMap { $0.windows }.first(where: { $0.isKeyWindow })?.rootViewController
    }
}
