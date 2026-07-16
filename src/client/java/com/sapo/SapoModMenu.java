package com.sapo;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SapoModMenu implements ModMenuApi {

        @Override
        public ConfigScreenFactory<?> getModConfigScreenFactory() {
                return parent -> {
                        ConfigBuilder builder = ConfigBuilder.create()
                                        .setParentScreen(parent)
                                        .setTitle(Component.literal("Sapo Configuration"));

                        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
                        ConfigCategory category = builder.getOrCreateCategory(Component.literal("General"));

                        category.addEntry(
                                        entryBuilder.startBooleanToggle(Component.literal("Jump Active"), Config.active)
                                                        .setDefaultValue(true)
                                                        .setSaveConsumer(newValue -> Config.active = newValue)
                                                        .build());

                        category.addEntry(entryBuilder.startIntField(Component.literal("Min (CPS)"), Config.minCroaks)
                                        .setDefaultValue(8)
                                        .setMin(1)
                                        .setMax(30)
                                        .setSaveConsumer(newValue -> Config.minCroaks = newValue)
                                        .build());

                        category.addEntry(entryBuilder.startIntField(Component.literal("Max (CPS)"), Config.maxCroaks)
                                        .setDefaultValue(14)
                                        .setMin(1)
                                        .setMax(30)
                                        .setSaveConsumer(newValue -> Config.maxCroaks = newValue)
                                        .build());

                        ConfigCategory alerts = builder.getOrCreateCategory(Component.literal("Alerts"));

                        alerts.addEntry(entryBuilder
                                        .startBooleanToggle(Component.literal("Alive or Dead Mode"),
                                                        Config.aliveOrDeadMode)
                                        .setDefaultValue(false)
                                        .setSaveConsumer(newValue -> Config.aliveOrDeadMode = newValue)
                                        .build());

                        List<String> currentTriggerText = Config.triggerText.isEmpty() ? new ArrayList<>() : Arrays.stream(Config.triggerText.split(",")).map(String::trim).collect(Collectors.toList());

                        alerts.addEntry(entryBuilder
                                        .startStrList(Component.literal("Trigger Text(s)"), currentTriggerText)
                                        .setDefaultValue(new ArrayList<>())
                                        .setSaveConsumer(newValue -> Config.triggerText = String.join(",", newValue))
                                        .build());

                        alerts.addEntry(entryBuilder
                                        .startStrField(Component.literal("On-Screen Alert Message"), Config.alertText)
                                        .setDefaultValue("CAUTION!")
                                        .setSaveConsumer(newValue -> Config.alertText = newValue)
                                        .build());

                        alerts.addEntry(entryBuilder
                                        .startColorField(Component.literal("Alert Color"), Config.alertColor)
                                        .setDefaultValue(0xFF5555)
                                        .setSaveConsumer(newValue -> Config.alertColor = newValue)
                                        .build());

                        alerts.addEntry(entryBuilder
                                        .startIntField(Component.literal("Alert Time (Seconds)"), Config.alertTime / 20)
                                        .setDefaultValue(5)
                                        .setMin(1)
                                        .setMax(60)
                                        .setSaveConsumer(newValue -> Config.alertTime = newValue * 20)
                                        .build());

                        ConfigCategory sounds = builder.getOrCreateCategory(Component.literal("Sounds"));

                        List<String> currentSoundTriggers = Config.soundTriggers.isEmpty() ? new ArrayList<>() : Arrays.stream(Config.soundTriggers.split(",")).map(String::trim).collect(Collectors.toList());

                        sounds.addEntry(entryBuilder
                                        .startStrList(Component.literal("Sound Triggers"), currentSoundTriggers)
                                        .setDefaultValue(new ArrayList<>())
                                        .setTooltip(Component.literal("Messages that will trigger the alert sound"))
                                        .setSaveConsumer(newValue -> Config.soundTriggers = String.join(",", newValue))
                                        .build());

                        sounds.addEntry(entryBuilder
                                        .startFloatField(Component.literal("Sound Volume"), Config.soundVolume)
                                        .setDefaultValue(1.0f)
                                        .setMin(0.0f)
                                        .setMax(2.0f)
                                        .setTooltip(Component.literal("Custom sound volume (0.0 to 2.0)"))
                                        .setSaveConsumer(newValue -> Config.soundVolume = newValue)
                                        .build());

                        ConfigCategory dps = builder.getOrCreateCategory(Component.literal("DPS"));

                        dps.addEntry(entryBuilder
                                        .startBooleanToggle(Component.literal("Show DPS HUD"), Config.dpsHudEnabled)
                                        .setDefaultValue(true)
                                        .setSaveConsumer(newValue -> Config.dpsHudEnabled = newValue)
                                        .build());

                        dps.addEntry(entryBuilder
                                        .startBooleanToggle(Component.literal("Hide Damage Numbers"), Config.hideDamageNumbers)
                                        .setDefaultValue(false)
                                        .setTooltip(Component.literal("Hides floating numbers to improve FPS"))
                                        .setSaveConsumer(newValue -> Config.hideDamageNumbers = newValue)
                                        .build());



                        builder.setSavingRunnable(Config::save);

                        return builder.build();
                };
        }
}
