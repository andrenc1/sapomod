package com.sapo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Config {
    // Mod variables
    public static boolean active = true;
    public static int minCroaks = 8;
    public static int maxCroaks = 14;
    public static boolean devMode = false;
    public static String triggerText = "";
    public static String alertText = "CAUTION!";
    public static int alertX = 100;
    public static int alertY = 50;
    public static float alertScale = 2.0f;
    public static int alertTime = 100;
    public static int alertColor = 0xFF5555;
    public static boolean aliveOrDeadMode = false;
    public static int aliveOrDeadX = 50;
    public static int aliveOrDeadY = 70;
    public static float aliveOrDeadScale = 2.0f;
    public static String soundTriggers = "";
    public static float soundVolume = 1.0f;
    public static boolean dpsHudEnabled = true;
    public static int dpsHudX = 10;
    public static int dpsHudY = 10;
    public static float dpsHudScale = 1.5f;
    public static int dpsHudColor = 0xFFFFFF;
    public static boolean hideDamageNumbers = false;

    // Tools for read/write JSON
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_DIR = new File(FabricLoader.getInstance().getConfigDir().toFile(), "sapo");
    private static final File FILE = new File(CONFIG_DIR, "sapo_config.json");

    // Method to load config
    public static void load() {
        if (FILE.exists()) {
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(FILE), StandardCharsets.UTF_8)) {
                SapoData data = GSON.fromJson(reader, SapoData.class);
                active = data.active;
                minCroaks = data.minCroaks;
                maxCroaks = data.maxCroaks;
                devMode = data.devMode;
                if (data.triggerText != null) triggerText = data.triggerText;
                if (data.alertText != null) alertText = data.alertText;
                alertX = data.alertX != 0 ? data.alertX : 100;
                alertY = data.alertY != 0 ? data.alertY : 50;
                alertScale = data.alertScale != 0.0f ? data.alertScale : 2.0f;
                alertTime = data.alertTime != 0 ? data.alertTime : 100;
                alertColor = data.alertColor != 0 ? data.alertColor : 0xFF5555;
                aliveOrDeadMode = data.aliveOrDeadMode;
                aliveOrDeadX = data.aliveOrDeadX != 0 ? data.aliveOrDeadX : 50;
                aliveOrDeadY = data.aliveOrDeadY != 0 ? data.aliveOrDeadY : 70;
                aliveOrDeadScale = data.aliveOrDeadScale != 0.0f ? data.aliveOrDeadScale : 2.0f;
                if (data.soundTriggers != null) soundTriggers = data.soundTriggers;
                soundVolume = data.soundVolume != 0.0f ? data.soundVolume : 1.0f;
                dpsHudEnabled = data.dpsHudEnabled;
                dpsHudX = data.dpsHudX != 0 ? data.dpsHudX : 10;
                dpsHudY = data.dpsHudY != 0 ? data.dpsHudY : 10;
                dpsHudScale = data.dpsHudScale != 0.0f ? data.dpsHudScale : 1.5f;
                dpsHudColor = data.dpsHudColor != 0 ? data.dpsHudColor : 0xFFFFFF;
                hideDamageNumbers = data.hideDamageNumbers;
            } catch (IOException e) {
                System.out.println("Error loading Sapo configurations.");
            }
        } else {
            save(); // Create default if missing
        }
        extractDefaultSound();
    }

    private static void extractDefaultSound() {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
        }
        File soundFile = new File(CONFIG_DIR, "sapo_alerta.wav");
        if (!soundFile.exists()) {
            try (InputStream in = Config.class.getResourceAsStream("/sapo_alerta.wav")) {
                if (in != null) {
                    Files.copy(in, soundFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("[Sapo] Default sapo_alerta.wav file extracted successfully.");
                } else {
                    System.out.println("[Sapo] Warning: sapo_alerta.wav not found inside mod resources.");
                }
            } catch (IOException e) {
                System.out.println("[Sapo] Error extracting default sapo_alerta.wav: " + e.getMessage());
            }
        }
    }

    // Method to save
    public static void save() {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
        }
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(FILE), StandardCharsets.UTF_8)) {
            SapoData data = new SapoData(active, minCroaks, maxCroaks, devMode, triggerText, alertText, alertX, alertY, alertScale, alertTime, alertColor, aliveOrDeadMode, aliveOrDeadX, aliveOrDeadY, aliveOrDeadScale, soundTriggers, soundVolume, dpsHudEnabled, dpsHudX, dpsHudY, dpsHudScale, dpsHudColor, hideDamageNumbers);
            GSON.toJson(data, writer);
        } catch (IOException e) {
            System.out.println("Error saving Sapo configurations.");
        }
    }

    private static class SapoData {
        @SerializedName("ativo")
        boolean active;
        @SerializedName("minCroac")
        int minCroaks;
        @SerializedName("maxCroac")
        int maxCroaks;
        @SerializedName("modoDev")
        boolean devMode;
        @SerializedName("textoGatilho")
        String triggerText;
        @SerializedName("textoAlerta")
        String alertText;
        @SerializedName("alertaX")
        int alertX;
        @SerializedName("alertaY")
        int alertY;
        @SerializedName("alertaEscala")
        float alertScale;
        @SerializedName("alertaTempo")
        int alertTime;
        @SerializedName("alertaCor")
        int alertColor;
        @SerializedName("modoVivoOuMorto")
        boolean aliveOrDeadMode;
        @SerializedName("vivoMortoX")
        int aliveOrDeadX;
        @SerializedName("vivoMortoY")
        int aliveOrDeadY;
        @SerializedName("vivoMortoEscala")
        float aliveOrDeadScale;
        @SerializedName("somGatilhos")
        String soundTriggers;
        @SerializedName("somVolume")
        float soundVolume;
        @SerializedName("dpsHudAtivo")
        boolean dpsHudEnabled;
        @SerializedName("dpsHudX")
        int dpsHudX;
        @SerializedName("dpsHudY")
        int dpsHudY;
        @SerializedName("dpsHudEscala")
        float dpsHudScale;
        @SerializedName("dpsHudCor")
        int dpsHudColor;
        @SerializedName("ocultarDanoTexto")
        boolean hideDamageNumbers;

        SapoData(boolean active, int minCroaks, int maxCroaks, boolean devMode, String triggerText, String alertText, int alertX, int alertY, float alertScale, int alertTime, int alertColor, boolean aliveOrDeadMode, int aliveOrDeadX, int aliveOrDeadY, float aliveOrDeadScale, String soundTriggers, float soundVolume, boolean dpsHudEnabled, int dpsHudX, int dpsHudY, float dpsHudScale, int dpsHudColor, boolean hideDamageNumbers) {
            this.active = active;
            this.minCroaks = minCroaks;
            this.maxCroaks = maxCroaks;
            this.devMode = devMode;
            this.triggerText = triggerText;
            this.alertText = alertText;
            this.alertX = alertX;
            this.alertY = alertY;
            this.alertScale = alertScale;
            this.alertTime = alertTime;
            this.alertColor = alertColor;
            this.aliveOrDeadMode = aliveOrDeadMode;
            this.aliveOrDeadX = aliveOrDeadX;
            this.aliveOrDeadY = aliveOrDeadY;
            this.aliveOrDeadScale = aliveOrDeadScale;
            this.soundTriggers = soundTriggers;
            this.soundVolume = soundVolume;
            this.dpsHudEnabled = dpsHudEnabled;
            this.dpsHudX = dpsHudX;
            this.dpsHudY = dpsHudY;
            this.dpsHudScale = dpsHudScale;
            this.dpsHudColor = dpsHudColor;
            this.hideDamageNumbers = hideDamageNumbers;
        }
    }
}