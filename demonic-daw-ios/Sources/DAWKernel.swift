import Foundation
import Combine

@MainActor
final class DAWKernel: ObservableObject {
    let projects = ProjectStore()
    let audio = AudioEngine()
    let midi = MIDIService()
    lazy var commands = CommandBridge(projects: projects, audio: audio)
    @Published var lastError: String?

    func boot() async {
        do {
            try projects.prepare()
            try audio.start()
            midi.start { [weak self] event in
                guard let self else { return }
                self.audio.handle(event)
            }
        } catch { lastError = error.localizedDescription }
    }
}
