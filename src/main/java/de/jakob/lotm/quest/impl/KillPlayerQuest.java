package de.jakob.lotm.quest.impl;

import de.jakob.lotm.potions.BeyonderPotion;
import de.jakob.lotm.potions.PotionItemHandler;
import de.jakob.lotm.quest.Quest;
import de.jakob.lotm.quest.QuestManager;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class KillPlayerQuest extends Quest {

    private final java.util.Map<UUID, UUID> targetByPlayer = new java.util.HashMap<>();

    public KillPlayerQuest(String id, int sequence) {
        super(id, sequence);
    }

    @Override
    public void startQuest(ServerPlayer player) {
        List<ServerPlayer> possibleTargets = player.server.getPlayerList().getPlayers().stream()
                .filter(other -> !other.getUUID().equals(player.getUUID()))
                .filter(other -> BeyonderData.getSequence(other) <= BeyonderData.getSequence(player))
                .toList();

        if (possibleTargets.isEmpty()) {
            QuestManager.discardQuest(player, id);
            player.sendSystemMessage(Component.literal("No valid player target found for this mission."));
            return;
        }

        ServerPlayer target = possibleTargets.get(new Random().nextInt(possibleTargets.size()));
        targetByPlayer.put(player.getUUID(), target.getUUID());
        player.sendSystemMessage(Component.literal("Target selected: " + target.getName().getString()));
    }

    @Override
    protected void onPlayerKillLiving(ServerPlayer player, LivingEntity victim) {
        if (!(victim instanceof Player killedPlayer)) {
            return;
        }

        UUID targetUuid = targetByPlayer.get(player.getUUID());
        if (targetUuid == null || !targetUuid.equals(killedPlayer.getUUID())) {
            return;
        }

        QuestManager.progressQuest(player, id, 1f);
    }

    @Override
    public List<ItemStack> getRewards(ServerPlayer player) {
        List<ItemStack> rewards = new ArrayList<>();
        int currentSequence = BeyonderData.getSequence(player);
        int rewardSequence = Math.min(9, currentSequence + 1);
        BeyonderPotion potion = PotionItemHandler.selectRandomPotionOfSequence(new Random(), rewardSequence);
        if (potion != null) {
            rewards.add(new ItemStack(potion));
        }
        return rewards;
    }

    @Override
    public float getDigestionReward() {
        return .5f;
    }

    @Override
    public MutableComponent getDescription() {
        return Component.translatable("lotm.quest.impl." + id + ".description");
    }
}
