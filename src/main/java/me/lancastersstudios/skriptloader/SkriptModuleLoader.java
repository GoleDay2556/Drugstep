package me.lancastersstudios.skriptloader;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class SkriptModuleLoader extends JavaPlugin {

    private File skriptTargetFolder;

    @Override
    public void onEnable() {

        getLogger().info("Starting Drugsters v" + getDescription().getVersion());

        // Check Skript dependency
        if (Bukkit.getPluginManager().getPlugin("Skript") == null) {
            getLogger().severe("Skript not found! Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        skriptTargetFolder = new File("plugins/Skript/scripts/drugsters");

        getLogger().info("Drugsters target folder: " + skriptTargetFolder.getAbsolutePath());

        // Create folders if missing
        if (!skriptTargetFolder.exists()) {
            boolean created = skriptTargetFolder.mkdirs();
            getLogger().info("Created Skript Drugsters folder: " + created);
        }

        // Copy bundled scripts only if missing
        extractBundledModules();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            getLogger().info("Drugsters plugin(s) were injected into plugins/Skript/scripts/drugsters.");
            getLogger().info("If you can't see the loaded plugin, run this command: /sk reload drugsters.sk");
        }, 100L); // ~5 seconds
    }

    private void extractBundledModules() {
        String[] bundledScripts = {"drugsters.sk"}; // Add bundled scripts here
        getLogger().info("Checking Drugsters scripts...");

        for (String script : bundledScripts) {
            try {
                File target = new File(skriptTargetFolder, script);
                if (target.exists()) {
                    getLogger().info("Drugsters script already exists, skipping: " + script);
                    continue;
                }

                try (InputStream in = getClass().getResourceAsStream("/drugsters/" + script)) {
                    if (in == null) {
                        getLogger().warning("Drugsters script not found in resources: " + script);
                        continue;
                    }
                    Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    getLogger().info("Drugsters script copied: ");
                }
            } catch (Exception e) {
                getLogger().severe("Failed to copy bundled script: " + script);
                e.printStackTrace();
            }
        }
    }

    

    @Override
    public void onDisable() {
        getLogger().info("Drugsters disabled.");
    }
}
