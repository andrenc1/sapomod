package com.calcinhaminimalista;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SapoPuzzle {

    public static List<BlockPos> solucaoAtiva = new ArrayList<>();

    public static void escanearEResolver(Minecraft client) {
        solucaoAtiva.clear();
        if (client.level == null || client.player == null) return;

        char[][] grid = new char[10][10];
        
        // LÊ O CHÃO SÓLIDO (Y=43) PARA IGNORAR O MINECRAFT:LIGHT DO HYPIXEL
        BlockPos topLeft = new BlockPos(-346, 43, 179); 

        client.player.sendSystemMessage(Component.literal("§e[Sapo] Escaneando o tabuleiro 10x10..."));
        System.out.println("[Sapo Debug] Iniciando varredura no piso sólido: " + topLeft.toShortString());

        boolean achouParede = false;

        for (int r = 0; r < 10; r++) { 
            for (int c = 0; c < 10; c++) { 
                BlockPos currentPos = topLeft.offset(c, 0, r);
                BlockState state = client.level.getBlockState(currentPos);
                String blockName = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();

                char cell = '.'; 

                if (blockName.contains("black") || blockName.contains("coal")) {
                    cell = 'X';
                    achouParede = true;
                }

                // O scanner de entidades continua pegando a camada de cima (Y=44)
                AABB box = new AABB(currentPos).inflate(0.2, 1.5, 0.2).move(0, 1, 0); 
                List<Entity> entities = client.level.getEntities(client.player, box);

                for (Entity e : entities) {
                    if (e instanceof Display.ItemDisplay itemDisplay) {
                        String data = itemDisplay.getItemStack().getComponents().toString();
                        if (data.contains("number0")) cell = '0';
                        else if (data.contains("number1")) cell = '1';
                        else if (data.contains("number2")) cell = '2';
                        else if (data.contains("number3")) cell = '3';
                        else if (data.contains("number4")) cell = '4';
                    }
                }
                grid[r][c] = cell;
            }
        }

        System.out.println("[Sapo Debug] Tabuleiro lido com sucesso (Matriz 10x10):");
        for (int i = 0; i < 10; i++) {
            System.out.println("[Sapo Debug] " + new String(grid[i]));
        }

        if (!achouParede) {
            System.out.println("[Sapo Debug] AVISO CRÍTICO: Nenhuma parede preta detectada! O Y=43 pode estar errado.");
        }

        System.out.println("[Sapo Debug] Iniciando o algoritmo Solver (Backtracking)...");
        long tempoInicio = System.currentTimeMillis();
        
        Set<String> bulbs = new HashSet<>();
        boolean resolvido = solve(grid, bulbs, 0, 0);
        
        long tempoFim = System.currentTimeMillis();

        if (resolvido) {
            System.out.println("[Sapo Debug] SUCESSO! Resolvido em " + (tempoFim - tempoInicio) + "ms.");
            client.player.sendSystemMessage(Component.literal("§a[Sapo] Puzzle resolvido! Siga as partículas verdes."));
            for (String b : bulbs) {
                String[] parts = b.split(",");
                int br = Integer.parseInt(parts[0]);
                int bc = Integer.parseInt(parts[1]);
                solucaoAtiva.add(topLeft.offset(bc, 0, br));
            }
        } else {
            System.out.println("[Sapo Debug] FALHA! O algoritmo não encontrou nenhuma solução possível.");
            client.player.sendSystemMessage(Component.literal("§c[Sapo] Falha ao resolver. Verifique o console."));
        }
    }

    private static boolean solve(char[][] grid, Set<String> bulbs, int r, int c) {
        if (c == 10) { r++; c = 0; }
        if (r == 10) return isSolved(grid, bulbs);

        if (grid[r][c] != '.') return solve(grid, bulbs, r, c + 1);

        if (canPlace(grid, bulbs, r, c)) {
            String pos = r + "," + c;
            bulbs.add(pos);
            if (isValidClues(grid, bulbs)) {
                if (solve(grid, bulbs, r, c + 1)) return true;
            }
            bulbs.remove(pos); // Backtrack
        }

        return solve(grid, bulbs, r, c + 1);
    }

    private static boolean canPlace(char[][] grid, Set<String> bulbs, int r, int c) {
        for (int i = r - 1; i >= 0; i--) {
            if (grid[i][c] != '.') break;
            if (bulbs.contains(i + "," + c)) return false;
        }
        for (int i = r + 1; i < 10; i++) {
            if (grid[i][c] != '.') break;
            if (bulbs.contains(i + "," + c)) return false;
        }
        for (int i = c - 1; i >= 0; i--) {
            if (grid[r][i] != '.') break;
            if (bulbs.contains(r + "," + i)) return false;
        }
        for (int i = c + 1; i < 10; i++) {
            if (grid[r][i] != '.') break;
            if (bulbs.contains(r + "," + i)) return false;
        }
        return true;
    }

    private static boolean isValidClues(char[][] grid, Set<String> bulbs) {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                if (grid[r][c] >= '0' && grid[r][c] <= '4') {
                    int req = grid[r][c] - '0';
                    int adj = 0;
                    if (bulbs.contains((r - 1) + "," + c)) adj++;
                    if (bulbs.contains((r + 1) + "," + c)) adj++;
                    if (bulbs.contains(r + "," + (c - 1))) adj++;
                    if (bulbs.contains(r + "," + (c + 1))) adj++;
                    if (adj > req) return false;
                }
            }
        }
        return true;
    }

    private static boolean isSolved(char[][] grid, Set<String> bulbs) {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                if (grid[r][c] >= '0' && grid[r][c] <= '4') {
                    int req = grid[r][c] - '0';
                    int adj = 0;
                    if (bulbs.contains((r - 1) + "," + c)) adj++;
                    if (bulbs.contains((r + 1) + "," + c)) adj++;
                    if (bulbs.contains(r + "," + (c - 1))) adj++;
                    if (bulbs.contains(r + "," + (c + 1))) adj++;
                    if (adj != req) return false;
                }
            }
        }

        Set<String> lit = new HashSet<>();
        for (String b : bulbs) {
            String[] parts = b.split(",");
            int br = Integer.parseInt(parts[0]);
            int bc = Integer.parseInt(parts[1]);
            lit.add(br + "," + bc);
            for (int i = br - 1; i >= 0 && grid[i][bc] == '.'; i--) lit.add(i + "," + bc);
            for (int i = br + 1; i < 10 && grid[i][bc] == '.'; i++) lit.add(i + "," + bc);
            for (int i = bc - 1; i >= 0 && grid[br][i] == '.'; i--) lit.add(br + "," + i);
            for (int i = bc + 1; i < 10 && grid[br][i] == '.'; i++) lit.add(br + "," + i);
        }

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                if (grid[r][c] == '.' && !lit.contains(r + "," + c)) return false;
            }
        }
        return true;
    }
}
