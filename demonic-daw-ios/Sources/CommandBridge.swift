import Foundation

final class CommandBridge {
    private let projects: ProjectStore
    private let audio: AudioEngine
    init(projects: ProjectStore, audio: AudioEngine) { self.projects = projects; self.audio = audio }

    func execute(_ payload: [String: Any]) throws -> [String: Any] {
        guard let command = payload["command"] as? String else { return ["ok": false, "error": "MISSING_COMMAND"] }
        switch command {
        case "project.create":
            let created = try projects.create(name: payload["name"] as? String ?? "Untitled")
            return ["ok": true, "projectId": created.0.uuidString, "revision": created.1.revision]
        default:
            return ["ok": false, "error": "UNSUPPORTED_COMMAND", "command": command]
        }
    }
}
