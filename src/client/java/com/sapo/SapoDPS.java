package com.sapo;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.Set;

public class SapoDPS {
    private static final Set<Integer> processedEntities = new HashSet<>();
    public static double totalDamage = 0;
    public static long firstHitTime = 0;
    public static long lastHitTime = 0;

    public static void onTick(Minecraft client) {
        if (!Config.active || !Config.dpsHudEnabled || client.level == null || client.player == null) {
            return;
        }

        // Run every 10 ticks (half a second)
        if (client.player.tickCount % 10 == 0) {
            AABB box = client.player.getBoundingBox().inflate(10.0);
            for (Entity entity : client.level.getEntities(client.player, box)) {
                if (processedEntities.contains(entity.getId())) continue;

                if (entity instanceof Display.TextDisplay textDisplay) {
                    if (textDisplay.getText() != null) {
                        String rawText = textDisplay.getText().toString();
                        if (!rawText.equals("empty")) {
                            String plainText = textDisplay.getText().getString();
                            double damage = parseTelosDamage(plainText);
                            
                            if (Config.devMode) {
                                StringBuilder hex = new StringBuilder();
                                for (char c : plainText.toCharArray()) {
                                    hex.append(String.format("\\u%04X ", (int) c));
                                }
                                System.out.println("[Sapo DPS Debug] Text: " + plainText);
                                System.out.println("[Sapo DPS Debug] Hex: " + hex.toString().trim());
                                System.out.println("[Sapo DPS Debug] Parsed Damage: " + damage);
                            }

                            if (damage > 0) {
                                registerDamage(damage);
                                if (Config.hideDamageNumbers) {
                                    textDisplay.discard();
                                }
                            }
                            processedEntities.add(entity.getId());
                        }
                    }
                }
            }
        }
    }

    public static double parseTelosDamage(String plainText) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < plainText.length(); i++) {
            char c = plainText.charAt(i);
            if (c == '\uD818' && i + 1 < plainText.length()) {
                char next = plainText.charAt(i + 1);
                if (next >= '\uDC25' && next <= '\uDC2E') {
                    sb.append((char) ('0' + (next - '\uDC25')));
                } else if (next == '\uDC4F') {
                    sb.append('.');
                }
                i++;
            }
        }
        if (sb.length() > 0) {
            try {
                return Double.parseDouble(sb.toString());
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    public static void registerDamage(double damage) {
        long now = System.currentTimeMillis();
        // Reset if more than 5 seconds out of combat
        if (now - lastHitTime > 5000) {
            totalDamage = 0;
            firstHitTime = now;
        }
        if (totalDamage == 0) {
            firstHitTime = now;
        }
        totalDamage += damage;
        lastHitTime = now;
    }

    public static double getCurrentDPS() {
        if (totalDamage == 0) return 0;
        double seconds = (System.currentTimeMillis() - firstHitTime) / 1000.0;
        if (seconds < 1.0) seconds = 1.0;
        
        // Timeout DPS display if inactive for 5 seconds
        if (System.currentTimeMillis() - lastHitTime > 5000) {
            totalDamage = 0;
            return 0;
        }
        return totalDamage / seconds;
    }
}
