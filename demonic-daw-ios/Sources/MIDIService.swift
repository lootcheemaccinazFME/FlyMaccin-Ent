import CoreMIDI

final class MIDIService {
    private var client = MIDIClientRef()
    private var input = MIDIPortRef()
    private var handler: ((MIDIEvent) -> Void)?

    func start(handler: @escaping (MIDIEvent) -> Void) {
        self.handler = handler
        MIDIClientCreateWithBlock("DemonicDAW" as CFString, &client) { _ in }
        MIDIInputPortCreateWithProtocol(client, "Input" as CFString, ._1_0, &input) { [weak self] list, _ in
            guard let self else { return }
            let count = MIDIEventListGetNumPackets(list)
            guard count > 0 else { return }
            var packet = list.pointee.packet
            for _ in 0..<count {
                let words = Mirror(reflecting: packet.words).children.compactMap { $0.value as? UInt32 }
                for word in words {
                    let status = UInt8((word >> 16) & 0xff)
                    if status != 0 { self.handler?(MIDIEvent(status: status, data1: UInt8((word >> 8) & 0xff), data2: UInt8(word & 0xff), timestamp: packet.timeStamp)) }
                }
                packet = MIDIEventPacketNext(&packet).pointee
            }
        }
        for i in 0..<MIDIGetNumberOfSources() { MIDIPortConnectSource(input, MIDIGetSource(i), nil) }
    }
}
