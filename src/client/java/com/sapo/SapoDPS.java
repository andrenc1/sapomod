package com.sapo;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SapoDPS {
    private static final Set<Integer> processedEntities = new HashSet<>();
    
    private static class DamageHit {
        double amount;
        long time;
        DamageHit(double amount, long time) {
            this.amount = amount;
            this.time = time;
        }
    }
    
    private static final List<DamageHit> recentHits = new ArrayList<>();
    public static long lastHitTime = 0;
    public static long combatStartTime = 0;
    
    private static double displayDps = 0;
    private static long lastDpsUpdateTime = 0;

    public static void onTick(Minecraft client) {
        if (!Config.active || !Config.dpsHudEnabled || client.level == null || client.player == null) {
            return;
        }

        // Clean up dead entities from memory every 100 ticks (5 seconds)
        if (client.level.getGameTime() % 100 == 0) {
            processedEntities.removeIf(id -> client.level.getEntity(id) == null);
        }

        AABB box = client.player.getBoundingBox().inflate(15.0);
        for (Entity entity : client.level.getEntities(client.player, box)) {
            if (processedEntities.contains(entity.getId())) continue;

            String rawText = null;
            String plainText = null;

            if (entity instanceof Display.TextDisplay textDisplay) {
                if (textDisplay.getText() != null) {
                    rawText = textDisplay.getText().toString();
                    plainText = textDisplay.getText().getString();
                }
            } else if (entity instanceof net.minecraft.world.entity.decoration.ArmorStand stand) {
                if (stand.hasCustomName()) {
                    rawText = stand.getCustomName().toString();
                    plainText = stand.getCustomName().getString();
                }
            }

            if (rawText != null && !rawText.equals("empty") && !plainText.trim().isEmpty()) {
                processedEntities.add(entity.getId());

                if (Config.devMode) {
                    StringBuilder hex = new StringBuilder();
                    for (char c : plainText.toCharArray()) {
                        hex.append(String.format("\\u%04X ", (int) c));
                    }
                    System.out.println("[Sapo DPS Debug] New Entity Text: " + plainText);
                    System.out.println("[Sapo DPS Debug] Hex: " + hex.toString().trim());
                }

                // Ignore health bars, percentages, and common boss health indicators
                String lowerText = plainText.toLowerCase();
                if (plainText.contains("/") || plainText.contains("%") || plainText.contains("❤") || plainText.contains("\u2764") ||
                    plainText.contains("[") || plainText.contains("]") || plainText.contains("|") || 
                    lowerText.contains("hp") || lowerText.contains("health") || plainText.contains(":")) {
                    continue;
                }

                double damage = parseTelosDamage(plainText);
                
                // Capped at 10,000,000 to prevent boss HP bar bug
                if (damage > 0 && damage < 10000000) {
                    registerDamage(damage);
                    if (Config.devMode) {
                        System.out.println("[Sapo DPS Debug] Parsed Custom Damage: " + damage);
                    }
                    if (Config.hideDamageNumbers) {
                        entity.discard();
                    }
                } else if (damage == 0) {
                    // Try to parse regular numbers for normal mobs
                    boolean hasInvalidLetters = false;
                    boolean hasDigits = false;
                    for (char c : lowerText.toCharArray()) {
                        if (Character.isDigit(c)) {
                            hasDigits = true;
                        } else if (Character.isLetter(c)) {
                            // Only allow 'k' and 'm' for thousands/millions
                            if (c != 'k' && c != 'm') {
                                hasInvalidLetters = true;
                                break;
                            }
                        }
                    }
                    
                    // If it has digits and no invalid letters, it's likely a normal damage indicator
                    if (hasDigits && !hasInvalidLetters) {
                        try {
                            String numStr = lowerText.replaceAll("[^0-9.km]", "");
                            double multiplier = 1.0;
                            if (numStr.endsWith("k")) {
                                multiplier = 1000.0;
                                numStr = numStr.substring(0, numStr.length() - 1);
                            } else if (numStr.endsWith("m")) {
                                multiplier = 1000000.0;
                                numStr = numStr.substring(0, numStr.length() - 1);
                            }
                            
                            numStr = numStr.replaceAll("[km]", ""); // clean any extra
                            
                            if (!numStr.isEmpty() && !numStr.equals(".")) {
                                double normalDamage = Double.parseDouble(numStr) * multiplier;
                                if (normalDamage > 0 && normalDamage < 10000000) {
                                    registerDamage(normalDamage);
                                    if (Config.devMode) {
                                        System.out.println("[Sapo DPS Debug] Parsed Normal Damage: " + normalDamage);
                                    }
                                    if (Config.hideDamageNumbers) {
                                        entity.discard();
                                    }
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
    }

    public static double parseTelosDamage(String plainText) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < plainText.length(); i++) {
            char c = plainText.charAt(i);
            
            // Old Telos font and new font 6-9
            if (c == '\uD818' && i + 1 < plainText.length()) {
                char next = plainText.charAt(i + 1);
                if (next >= '\uDC25' && next <= '\uDC2E') {
                    sb.append((char) ('0' + (next - '\uDC25')));
                } else if (next >= '\uDC00' && next <= '\uDC03') {
                    sb.append((char) ('6' + (next - '\uDC00')));
                } else if (next == '\uDC4F' || next == '\uDC4E' || next == '\uDC4D') {
                    sb.append('.');
                } else if (next == '\uDC50') {
                    sb.append('k');
                }
                i++;
            } 
            // New Telos font 0-5 and newer fonts
            else if (c == '\uD817' && i + 1 < plainText.length()) {
                char next = plainText.charAt(i + 1);
                if (next >= '\uDFFA' && next <= '\uDFFF') {
                    sb.append((char) ('0' + (next - '\uDFFA')));
                } else if (next >= '\uDFCB' && next <= '\uDFD4') { // Newest font 0-9
                    sb.append((char) ('0' + (next - '\uDFCB')));
                } else if (next == '\uDFD5') { // Newest font 'k'
                    sb.append('k');
                }
                i++;
            }
        }
        
        String res = sb.toString();
        if (res.length() > 0) {
            try {
                double multiplier = 1.0;
                if (res.endsWith("k")) {
                    multiplier = 1000.0;
                    res = res.substring(0, res.length() - 1);
                } else if (res.endsWith("m")) {
                    multiplier = 1000000.0;
                    res = res.substring(0, res.length() - 1);
                }
                
                res = res.replaceAll("[^0-9.]", "");
                if (res.isEmpty() || res.equals(".")) return 0;
                
                return Double.parseDouble(res) * multiplier;
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    public static void registerDamage(double damage) {
        long now = System.currentTimeMillis();
        if (now - lastHitTime > 5000) {
            recentHits.clear();
            combatStartTime = now;
        }
        if (recentHits.isEmpty()) {
            combatStartTime = now;
        }
        recentHits.add(new DamageHit(damage, now));
        lastHitTime = now;
    }

    public static double getCurrentDPS() {
        long now = System.currentTimeMillis();
        
        // Timeout DPS display if inactive for 5 seconds
        if (now - lastHitTime > 5000) {
            recentHits.clear();
            displayDps = 0;
            return 0;
        }

        // Only update the calculated DPS visually every 250ms to stop flickering
        if (now - lastDpsUpdateTime > 250) {
            Iterator<DamageHit> it = recentHits.iterator();
            while (it.hasNext()) {
                DamageHit hit = it.next();
                if (now - hit.time > 3000) {
                    it.remove();
                }
            }

            if (recentHits.isEmpty()) {
                displayDps = 0;
            } else {
                double sum = 0;
                for (DamageHit hit : recentHits) {
                    sum += hit.amount;
                }

                double combatDuration = (now - combatStartTime) / 1000.0;
                double window = Math.min(3.0, combatDuration);
                if (window < 0.5) window = 0.5; // Prevent dividing by near-zero

                displayDps = sum / window;
            }
            lastDpsUpdateTime = now;
        }

        return displayDps;
    }
}
