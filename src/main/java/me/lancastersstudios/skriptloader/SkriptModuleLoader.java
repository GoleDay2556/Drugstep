package me.lancastersstudios.skriptloader;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class SkriptModuleLoader extends JavaPlugin {

    private File externalModulesFolder;
    private File skriptTargetFolder;

    @Override
    public void onEnable() {

        getLogger().info("=================================");
        getLogger().info("Starting SkriptModuleLoader v" + getDescription().getVersion());
        getLogger().info("=================================");

        // Check Skript dependency
        if (Bukkit.getPluginManager().getPlugin("Skript") == null) {
            getLogger().severe("Skript not found! Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Resolve folders
        externalModulesFolder = new File(getDataFolder(), "modules");
        skriptTargetFolder = new File("plugins/Skript/scripts/modules");

        getLogger().info("External modules folder: " + externalModulesFolder.getAbsolutePath());
        getLogger().info("Skript target folder: " + skriptTargetFolder.getAbsolutePath());

        // Create folders if missing
        if (!externalModulesFolder.exists()) {
            boolean created = externalModulesFolder.mkdirs();
            getLogger().info("Created modules folder: " + created);
        }
        if (!skriptTargetFolder.exists()) {
            boolean created = skriptTargetFolder.mkdirs();
            getLogger().info("Created Skript modules folder: " + created);
        }

        // Copy bundled scripts only if missing
        extractBundledModules();

        // Copy external scripts only if missing
        copyExternalModules();


        Bukkit.getScheduler().runTaskLater(this, () -> {
            getLogger().info("Skript plugin(s) were injected into plugins/Skript/scripts/modules.");
            getLogger().info("If you can't see the loaded plugin, run this command: /sk reload modules");
        }, 100L); // ~5 seconds
    }

    private void extractBundledModules() {
        String[] bundledScripts = {}; // Add bundled scripts here
        getLogger().info("Checking bundled scripts...");

        for (String script : bundledScripts) {
            try {
                File target = new File(skriptTargetFolder, script);
                if (target.exists()) {
                    getLogger().info("Bundled script already exists, skipping: " + script);
                    continue;
                }

                try (InputStream in = getClass().getResourceAsStream("/modules/" + script)) {
                    if (in == null) {
                        getLogger().warning("Bundled script not found in resources: " + script);
                        continue;
                    }
                    Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    getLogger().info("Bundled script copied: " + script);
                }
            } catch (Exception e) {
                getLogger().severe("Failed to copy bundled script: " + script);
                e.printStackTrace();
            }
        }
    }

    private void copyExternalModules() {
        getLogger().info("Checking external scripts in plugin modules folder...");

        if (!externalModulesFolder.exists()) {
            getLogger().warning("External modules folder missing, skipping external scripts.");
            return;
        }

        File[] files = externalModulesFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".sk"));

        if (files == null || files.length == 0) {
            getLogger().info("No external .sk files found.");
            return;
        }

        for (File file : files) {
            try {
                File target = new File(skriptTargetFolder, file.getName());
                if (target.exists()) {
                    getLogger().info("External script already exists, skipping: " + file.getName());
                    continue;
                }
                Files.copy(file.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                getLogger().info("External script copied: " + file.getName());
            } catch (Exception e) {
                getLogger().severe("Failed to copy external script: " + file.getName());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("SkriptModuleLoader disabled.");
    }
}
