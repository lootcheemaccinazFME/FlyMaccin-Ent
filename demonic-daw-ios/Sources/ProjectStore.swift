import Foundation

struct ProjectDocument: Codable {
    var schemaVersion: Int = 2
    var revision: Int64 = 0
    var name: String
    var tempo: Double = 120
    var tracks: [Track] = []
    var clips: [Clip] = []
}
struct Track: Codable, Identifiable { var id: UUID; var name: String; var routeID: UUID? }
struct Clip: Codable, Identifiable { var id: UUID; var trackID: UUID; var startTick: Int64; var lengthTicks: Int64 }

final class ProjectStore {
    private let fm = FileManager.default
    private var root: URL { fm.urls(for: .applicationSupportDirectory, in: .userDomainMask)[0].appendingPathComponent("DemonicDAW/projects", isDirectory: true) }

    func prepare() throws { try fm.createDirectory(at: root, withIntermediateDirectories: true) }

    func load(_ id: UUID) throws -> ProjectDocument {
        let data = try Data(contentsOf: projectURL(id))
        let project = try JSONDecoder().decode(ProjectDocument.self, from: data)
        try validate(project)
        return project
    }

    func create(name: String) throws -> (UUID, ProjectDocument) {
        let id = UUID(); let p = ProjectDocument(name: name)
        try save(id, project: p, expectedRevision: nil)
        return (id, p)
    }

    func save(_ id: UUID, project input: ProjectDocument, expectedRevision: Int64?) throws {
        var project = input
        if fm.fileExists(atPath: projectURL(id).path), let expectedRevision {
            let current = try load(id)
            guard current.revision == expectedRevision else { throw StoreError.revisionConflict }
            project.revision = current.revision + 1
        }
        try validate(project)
        let dir = projectURL(id).deletingLastPathComponent()
        try fm.createDirectory(at: dir, withIntermediateDirectories: true)
        let data = try JSONEncoder().encode(project)
        try data.write(to: projectURL(id), options: [.atomic, .completeFileProtectionUnlessOpen])
    }

    private func projectURL(_ id: UUID) -> URL { root.appendingPathComponent(id.uuidString, isDirectory: true).appendingPathComponent("project.json") }

    private func validate(_ p: ProjectDocument) throws {
        guard (20...400).contains(p.tempo) else { throw StoreError.invalidProject }
        let trackIDs = Set(p.tracks.map(\.id))
        guard trackIDs.count == p.tracks.count else { throw StoreError.invalidProject }
        guard p.clips.allSatisfy({ trackIDs.contains($0.trackID) && $0.startTick >= 0 && $0.lengthTicks > 0 }) else { throw StoreError.invalidProject }
    }

    enum StoreError: Error { case revisionConflict, invalidProject }
}
