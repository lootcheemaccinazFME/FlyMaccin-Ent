package com.flymaccin.demonicaistudio;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/** Atomic app-private autosave with migration from the original preferences store. */
final class ProjectStore {
    private static final String PREFS = "demonic_studio_v110";
    private static final String PROJECT_KEY = "autosave_project";
    private final Context context;
    private final File projectFile;

    ProjectStore(Context context) {
        this.context = context.getApplicationContext();
        projectFile = new File(this.context.getFilesDir(), "studio/session.json");
    }

    StudioProject load() {
        File backup = new File(projectFile.getParentFile(), "session.previous");
        if (!projectFile.isFile() && backup.isFile()) backup.renameTo(projectFile);
        if (projectFile.isFile()) {
            try (FileInputStream input = new FileInputStream(projectFile)) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int count;
                while ((count = input.read(buffer)) != -1) bytes.write(buffer, 0, count);
                return StudioProject.fromJson(new String(bytes.toByteArray(), "UTF-8"));
            } catch (Exception ignored) {
                // Fall through to the previous version's autosave.
            }
        }
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return StudioProject.fromJson(preferences.getString(PROJECT_KEY, ""));
    }

    void save(StudioProject project) throws Exception {
        File parent = projectFile.getParentFile();
        if (parent == null || (!parent.isDirectory() && !parent.mkdirs()))
            throw new IllegalStateException("Cannot create project directory");
        File pending = new File(parent, "session.pending");
        byte[] data = project.toJson().getBytes("UTF-8");
        try (FileOutputStream output = new FileOutputStream(pending)) {
            output.write(data);
        }
        if (!pending.renameTo(projectFile)) {
            File backup = new File(parent, "session.previous");
            if (backup.exists() && !backup.delete()) throw new IllegalStateException("Cannot rotate project backup");
            boolean movedCurrent = !projectFile.exists() || projectFile.renameTo(backup);
            if (!movedCurrent || !pending.renameTo(projectFile)) {
                if (movedCurrent && backup.exists()) backup.renameTo(projectFile);
                pending.delete();
                throw new IllegalStateException("Cannot finish project save");
            }
            backup.delete();
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(PROJECT_KEY, project.toJson()).apply();
    }
}
