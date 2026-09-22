import AVFoundation

struct MIDIEvent { let status: UInt8; let data1: UInt8; let data2: UInt8; let timestamp: UInt64 }

final class AudioEngine {
    private let engine = AVAudioEngine()
    private let sampler = AVAudioUnitSampler()

    init() { engine.attach(sampler); engine.connect(sampler, to: engine.mainMixerNode, format: nil) }

    func start() throws {
        let session = AVAudioSession.sharedInstance()
        try session.setCategory(.playAndRecord, mode: .default, options: [.mixWithOthers, .defaultToSpeaker, .allowBluetoothA2DP])
        try session.setActive(true)
        try engine.start()
    }

    func handle(_ event: MIDIEvent) {
        let type = event.status & 0xF0
        let channel = event.status & 0x0F // generic MIDI channel, never an instrument category
        switch type {
        case 0x90 where event.data2 > 0: sampler.startNote(event.data1, withVelocity: event.data2, onChannel: channel)
        case 0x80, 0x90: sampler.stopNote(event.data1, onChannel: channel)
        case 0xB0: sampler.sendController(event.data1, withValue: event.data2, onChannel: channel)
        default: break
        }
    }
}
