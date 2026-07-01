package com.calcinhaminimalista.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.resources.sounds.SoundInstance;

@Mixin(SoundEngine.class)
public class OuvidoSapoMixin {
    @Inject(method = "play", at = @At("RETURN"), cancellable = true)
    private void play(SoundInstance sound, CallbackInfoReturnable<Void> cir) {
        if (sound == null) return;
        
        try {
            if (com.calcinhaminimalista.Config.modoVivoOuMorto) {
                String soundId = sound.getIdentifier().toString();
                if (soundId.contains("noise:cherub.beam1")) {
                    com.calcinhaminimalista.CalcinhaMinimalista.mensagemVivoMorto = "AGACHE!";
                    com.calcinhaminimalista.CalcinhaMinimalista.alertaTempoRestante = 20;
                    com.calcinhaminimalista.CalcinhaMinimalista.corVivoMorto = 0x55FF55;
                } else if (soundId.contains("noise:cherub.beam2")) {
                    com.calcinhaminimalista.CalcinhaMinimalista.mensagemVivoMorto = "PULE!";
                    com.calcinhaminimalista.CalcinhaMinimalista.alertaTempoRestante = 20;
                    com.calcinhaminimalista.CalcinhaMinimalista.corVivoMorto = 0x5555FF;
                }
            }
            if (com.calcinhaminimalista.Config.modoDev) {
                System.out.println("[Sapo Debug] Tocando som: " + sound + " | Volume: " + sound.getVolume() + " | Pitch: " + sound.getPitch());
            }
        } catch (Exception e) {
            System.out.println("[Sapo Debug] Erro ao pegar som: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
