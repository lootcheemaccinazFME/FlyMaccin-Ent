import SwiftUI

@main
struct DemonicDAWApp: App {
    @StateObject private var kernel = DAWKernel()
    var body: some Scene {
        WindowGroup {
            ContentView().environmentObject(kernel)
        }
    }
}

struct ContentView: View {
    @EnvironmentObject var kernel: DAWKernel
    var body: some View {
        ZStack {
            WebDAWView(kernel: kernel)
            if let error = kernel.lastError {
                VStack { Spacer(); Text(error).font(.caption).padding(8).background(.ultraThinMaterial).clipShape(RoundedRectangle(cornerRadius: 8)).padding() }
            }
        }
        .task { await kernel.boot() }
    }
}
