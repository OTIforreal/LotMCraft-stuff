package de.jakob.lotm.quest.impl;

import de.jakob.lotm.quest.Quest;
import de.jakob.lotm.quest.QuestManager;
import de.jakob.lotm.potions.BeyonderPotion;
import de.jakob.lotm.potions.PotionItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.*;

public class FindStructureQuest extends Quest {

    private static final List<String> STRUCTURE_IDS = List.of(
            "minecraft:stronghold",
            "minecraft:jungle_temple",
            "minecraft:woodland_mansion",
            "minecraft:trial_chambers",
            "minecraft:desert_pyramid",
            "minecraft:monument"
    );

    private final int requiredAmount;
    private final Map<UUID, Set<String>> foundStructures = new HashMap<>();

    public FindStructureQuest(String id, int sequence, int requiredAmount) {
        super(id, sequence);
        this.requiredAmount = requiredAmount;
    }

    @Override
    public void tick(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Set<String> found = foundStructures.computeIfAbsent(player.getUUID(), ignored -> new HashSet<>());

        for (String structureId : STRUCTURE_IDS) {
            if (found.contains(structureId)) {
                continue;
            }

            BlockPos structurePos = findNearestStructure(level, player.blockPosition(), structureId);
            if (structurePos == null) {
                continue;
            }

            if (structurePos.distSqr(player.blockPosition()) <= 160 * 160) {
                found.add(structureId);
                if (found.size() >= requiredAmount) {
                    QuestManager.progressQuest(player, id, 1f);
                    return;
                }
                QuestManager.progressQuest(player, id, 1f / requiredAmount);
                return;
            }
        }
    }

    @Override
    public List<ItemStack> getRewards(ServerPlayer player) {
        List<ItemStack> rewards = new ArrayList<>();
        int seq = new Random().nextBoolean() ? 7 : 8;
        BeyonderPotion potion = PotionItemHandler.selectRandomPotionOfSequence(new Random(), seq);
        if (potion != null) {
            rewards.add(new ItemStack(potion));
        }
        return rewards;
    }

    @Override
    public float getDigestionReward() {
        return .3f;
    }

    @Override
    public MutableComponent getDescription() {
        return Component.translatable("lotm.quest.impl." + id + ".description", requiredAmount);
    }

    private BlockPos findNearestStructure(ServerLevel level, BlockPos origin, String structureId) {
        ResourceLocation structureKey = ResourceLocation.tryParse(structureId);
        if (structureKey == null) {
            return null;
        }

        var structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Optional<Holder.Reference<Structure>> structureHolder = structureRegistry.getHolder(structureKey);
        if (structureHolder.isEmpty()) {
            return null;
        }

        var result = level.getChunkSource().getGenerator().findNearestMapStructure(
                level,
                HolderSet.direct(structureHolder.get()),
                origin,
                120,
                false
        );
        return result == null ? null : result.getFirst();
    }
}
