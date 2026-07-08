package com.sapo.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.resources.sounds.SoundInstance;

@Mixin(SoundEngine.class)
public class SapoAudioMixin {
    @Inject(method = "play", at = @At("RETURN"), cancellable = true)
    private void play(SoundInstance sound, CallbackInfoReturnable<Void> cir) {
        if (sound == null) return;
        
        try {
            if (com.sapo.Config.aliveOrDeadMode) {
                String soundId = sound.getIdentifier().toString();
                if (soundId.contains("noise:cherub.beam1")) {
                    com.sapo.SapoMod.aliveOrDeadMessage = "CROUCH!";
                    com.sapo.SapoMod.alertTimeRemaining = 20;
                    com.sapo.SapoMod.aliveOrDeadColor = 0x55FF55;
                } else if (soundId.contains("noise:cherub.beam2")) {
                    com.sapo.SapoMod.aliveOrDeadMessage = "JUMP!";
                    com.sapo.SapoMod.alertTimeRemaining = 20;
                    com.sapo.SapoMod.aliveOrDeadColor = 0x5555FF;
                }
            }
            if (com.sapo.Config.devMode) {
                System.out.println("[Sapo Debug] Playing sound: " + sound + " | Volume: " + sound.getVolume() + " | Pitch: " + sound.getPitch());
            }
        } catch (Exception e) {
            System.out.println("[Sapo Debug] Error getting sound: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
