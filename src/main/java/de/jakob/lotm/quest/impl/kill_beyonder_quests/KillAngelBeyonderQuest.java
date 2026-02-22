package de.jakob.lotm.quest.impl.kill_beyonder_quests;

import de.jakob.lotm.potions.BeyonderPotion;
import de.jakob.lotm.potions.PotionItemHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KillAngelBeyonderQuest extends KillBeyonderQuest {

    public KillAngelBeyonderQuest(String id, int sequence) {
        super(id, sequence, "", 2);
    }

    @Override
    public List<ItemStack> getRewards(ServerPlayer player) {
        ArrayList<ItemStack> rewards = new ArrayList<>();
        BeyonderPotion potion = PotionItemHandler.selectRandomPotionOfSequence(new Random(), 2);
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
        // TODO: Fill quest description text.
        return Component.literal("");
    }
}
