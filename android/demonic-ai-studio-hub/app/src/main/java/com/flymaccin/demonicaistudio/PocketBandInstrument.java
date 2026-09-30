package com.flymaccin.demonicaistudio;

import java.io.File;

/**
 * Shared PocketBand instrument contract.
 *
 * App identity stays in the shell. Pocket Potna and Demonic can each adapt
 * their UI/storage layer to this engine without sharing application IDs.
 */
final class PocketBandInstrument {
    enum Format { WAV, SFZ, SF2 }

    final File source;
    final Format format;
    final String displayName;

    PocketBandInstrument(File source, Format format, String displayName) {
        this.source = source;
        this.format = format;
        this.displayName = displayName;
    }

    static Format detect(File file) {
        String name = file.getName().toLowerCase(java.util.Locale.US);
        if (name.endsWith(".sfz")) return Format.SFZ;
        if (name.endsWith(".sf2")) return Format.SF2;
        return Format.WAV;
    }

    boolean exists() {
        return source != null && source.isFile() && source.length() > 0;
    }
}
