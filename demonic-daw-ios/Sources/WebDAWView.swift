import SwiftUI
import WebKit

struct WebDAWView: UIViewRepresentable {
    let kernel: DAWKernel

    func makeCoordinator() -> Coordinator { Coordinator(kernel: kernel) }

    func makeUIView(context: Context) -> WKWebView {
        let config = WKWebViewConfiguration()
        config.userContentController.add(context.coordinator, name: "demonic")
        let web = WKWebView(frame: .zero, configuration: config)
        if let url = Bundle.main.url(forResource: "offline", withExtension: "html") { web.loadFileURL(url, allowingReadAccessTo: url.deletingLastPathComponent()) }
        return web
    }
    func updateUIView(_ uiView: WKWebView, context: Context) {}

    final class Coordinator: NSObject, WKScriptMessageHandler {
        let kernel: DAWKernel
        init(kernel: DAWKernel) { self.kernel = kernel }
        func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
            guard let body = message.body as? [String: Any] else { return }
            do { _ = try kernel.commands.execute(body) } catch { kernel.lastError = error.localizedDescription }
        }
    }
}
