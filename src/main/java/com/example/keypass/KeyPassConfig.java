package com.example.keypass;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public final class KeyPassConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("keypass.properties");
    private static final List<Entry> ENTRIES = new ArrayList<>();

    private KeyPassConfig() {}

    public record Entry(String ip, String command) {}

    public static List<Entry> entries() {
        return ENTRIES;
    }

    public static Entry find(String address) {
        String normalized = normalize(address);
        for (Entry entry : ENTRIES) {
            if (normalize(entry.ip()).equals(normalized)) return entry;
        }
        return null;
    }

    public static void add(String ip, String command) {
        ENTRIES.add(new Entry(ip.trim(), command.trim()));
        save();
    }

    public static void set(int index, String ip, String command) {
        if (index < 0 || index >= ENTRIES.size()) return;
        ENTRIES.set(index, new Entry(ip.trim(), command.trim()));
        save();
    }

    public static void remove(int index) {
        if (index < 0 || index >= ENTRIES.size()) return;
        ENTRIES.remove(index);
        save();
    }

    public static void load() {
        ENTRIES.clear();
        if (!Files.exists(FILE)) return;

        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            properties.load(in);
            int count = Integer.parseInt(properties.getProperty("count", "0"));
            for (int i = 0; i < count; i++) {
                String ip = properties.getProperty("entry." + i + ".ip", "").trim();
                String command = properties.getProperty("entry." + i + ".command", "").trim();
                if (!ip.isBlank() && !command.isBlank()) ENTRIES.add(new Entry(ip, command));
            }
        } catch (Exception e) {
            KeyPassClient.LOGGER.error("Could not load KeyPass config", e);
        }
    }

    public static void save() {
        Properties properties = new Properties();
        properties.setProperty("count", Integer.toString(ENTRIES.size()));
        for (int i = 0; i < ENTRIES.size(); i++) {
            properties.setProperty("entry." + i + ".ip", ENTRIES.get(i).ip());
            properties.setProperty("entry." + i + ".command", ENTRIES.get(i).command());
        }

        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                properties.store(out, "KeyPass server commands");
            }
        } catch (IOException e) {
            KeyPassClient.LOGGER.error("Could not save KeyPass config", e);
        }
    }

    private static String normalize(String value) {
        String result = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (result.startsWith("minecraft://")) result = result.substring("minecraft://".length());
        if (result.endsWith(":25565")) result = result.substring(0, result.length() - 6);
        return result;
    }
}
