package com.calcinhaminimalista;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Config {
    // Variáveis do mod
    public static boolean ativo = true;
    public static int minCroac = 8;
    public static int maxCroac = 14;
    public static boolean modoDev = false;
    public static String textoGatilho = "";
    public static String textoAlerta = "CUIDADO!";
    public static int alertaX = 100;
    public static int alertaY = 50;
    public static float alertaEscala = 2.0f;
    public static int alertaTempo = 100;
    public static int alertaCor = 0xFF5555;
    public static boolean modoVivoOuMorto = false;
    public static int vivoMortoX = 50;
    public static int vivoMortoY = 70;
    public static float vivoMortoEscala = 2.0f;
    public static String somGatilhos = "";
    public static float somVolume = 1.0f;

    // Ferramentas para ler/escrever o JSON
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File PASTA_CONFIG = new File(FabricLoader.getInstance().getConfigDir().toFile(), "calcinhaminimalista");
    private static final File ARQUIVO = new File(PASTA_CONFIG, "sapo_config.json");

    // Método para carregar ao abrir o jogo
    public static void carregar() {
        if (ARQUIVO.exists()) {
            try (FileReader leitor = new FileReader(ARQUIVO)) {
                SapoDados dados = GSON.fromJson(leitor, SapoDados.class);
                ativo = dados.ativo;
                minCroac = dados.minCroac;
                maxCroac = dados.maxCroac;
                modoDev = dados.modoDev;
                if (dados.textoGatilho != null) textoGatilho = dados.textoGatilho;
                if (dados.textoAlerta != null) textoAlerta = dados.textoAlerta;
                alertaX = dados.alertaX != 0 ? dados.alertaX : 100;
                alertaY = dados.alertaY != 0 ? dados.alertaY : 50;
                alertaEscala = dados.alertaEscala != 0.0f ? dados.alertaEscala : 2.0f;
                alertaTempo = dados.alertaTempo != 0 ? dados.alertaTempo : 100;
                alertaCor = dados.alertaCor != 0 ? dados.alertaCor : 0xFF5555;
                modoVivoOuMorto = dados.modoVivoOuMorto;
                vivoMortoX = dados.vivoMortoX != 0 ? dados.vivoMortoX : 50;
                vivoMortoY = dados.vivoMortoY != 0 ? dados.vivoMortoY : 70;
                vivoMortoEscala = dados.vivoMortoEscala != 0.0f ? dados.vivoMortoEscala : 2.0f;
                if (dados.somGatilhos != null) somGatilhos = dados.somGatilhos;
                somVolume = dados.somVolume != 0.0f ? dados.somVolume : 1.0f;
            } catch (IOException e) {
                System.out.println("Erro ao carregar as configurações do Sapo.");
            }
        } else {
            salvar(); // Cria o arquivo padrão se não existir
        }
        extrairSomPadrao();
    }

    private static void extrairSomPadrao() {
        if (!PASTA_CONFIG.exists()) {
            PASTA_CONFIG.mkdirs();
        }
        File arquivoSom = new File(PASTA_CONFIG, "sapo_alerta.wav");
        if (!arquivoSom.exists()) {
            try (InputStream in = Config.class.getResourceAsStream("/sapo_alerta.wav")) {
                if (in != null) {
                    Files.copy(in, arquivoSom.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("[Sapo] Arquivo sapo_alerta.wav padrão extraído com sucesso.");
                } else {
                    System.out.println("[Sapo] Aviso: sapo_alerta.wav não encontrado dentro do mod (resources).");
                }
            } catch (IOException e) {
                System.out.println("[Sapo] Erro ao extrair sapo_alerta.wav padrão: " + e.getMessage());
            }
        }
    }

    // Método para salvar ao fechar o menu
    public static void salvar() {
        if (!PASTA_CONFIG.exists()) {
            PASTA_CONFIG.mkdirs();
        }
        try (FileWriter escritor = new FileWriter(ARQUIVO)) {
            SapoDados dados = new SapoDados(ativo, minCroac, maxCroac, modoDev, textoGatilho, textoAlerta, alertaX, alertaY, alertaEscala, alertaTempo, alertaCor, modoVivoOuMorto, vivoMortoX, vivoMortoY, vivoMortoEscala, somGatilhos, somVolume);
            GSON.toJson(dados, escritor);
        } catch (IOException e) {
            System.out.println("Erro ao salvar as configurações do Sapo.");
        }
    }

    // Classe auxiliar interna apenas para formatar o JSON
    private static class SapoDados {
        boolean ativo;
        int minCroac;
        int maxCroac;
        boolean modoDev;
        String textoGatilho;
        String textoAlerta;
        int alertaX;
        int alertaY;
        float alertaEscala;
        int alertaTempo;
        int alertaCor;
        boolean modoVivoOuMorto;
        int vivoMortoX;
        int vivoMortoY;
        float vivoMortoEscala;
        String somGatilhos;
        float somVolume;

        SapoDados(boolean ativo, int minCroac, int maxCroac, boolean modoDev, String textoGatilho, String textoAlerta, int alertaX, int alertaY, float alertaEscala, int alertaTempo, int alertaCor, boolean modoVivoOuMorto, int vivoMortoX, int vivoMortoY, float vivoMortoEscala, String somGatilhos, float somVolume) {
            this.ativo = ativo;
            this.minCroac = minCroac;
            this.maxCroac = maxCroac;
            this.modoDev = modoDev;
            this.textoGatilho = textoGatilho;
            this.textoAlerta = textoAlerta;
            this.alertaX = alertaX;
            this.alertaY = alertaY;
            this.alertaEscala = alertaEscala;
            this.alertaTempo = alertaTempo;
            this.alertaCor = alertaCor;
            this.modoVivoOuMorto = modoVivoOuMorto;
            this.vivoMortoX = vivoMortoX;
            this.vivoMortoY = vivoMortoY;
            this.vivoMortoEscala = vivoMortoEscala;
            this.somGatilhos = somGatilhos;
            this.somVolume = somVolume;
        }
    }
}
