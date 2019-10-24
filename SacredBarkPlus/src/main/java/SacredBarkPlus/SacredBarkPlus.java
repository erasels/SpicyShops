package SacredBarkPlus;

import SacredBarkPlus.patches.PotionLogicPatches;
import SacredBarkPlus.patches.PotionUsePatches;
import basemod.BaseMod;
import basemod.abstracts.CustomSavable;
import basemod.interfaces.*;

import com.evacipated.cardcrawl.mod.hubris.patches.potions.AbstractPotion.PotionUseCountField;
import com.evacipated.cardcrawl.mod.hubris.relics.EmptyBottle;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.potions.*;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.SacredBark;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpireInitializer
public class SacredBarkPlus implements
        PostInitializeSubscriber, PostUpdateSubscriber, RelicGetSubscriber {
    public static final Logger logger = LogManager.getLogger(SacredBarkPlus.class.getName());
    public static final int USES = 2;
    public static final boolean hasHubris;

    static {
        hasHubris = Loader.isModLoaded("hubris");
        if (hasHubris) {
            logger.info("Detected hubris for compatibility.");
        }
    }

    public static ArrayList<String> affectedPotions = new ArrayList<>(Arrays.asList(Ambrosia.POTION_ID, BlessingOfTheForge.POTION_ID, Elixir.POTION_ID, EntropicBrew.POTION_ID, GamblersBrew.POTION_ID, SmokeBomb.POTION_ID, StancePotion.POTION_ID));

    public static void initialize() {
        BaseMod.subscribe(new SacredBarkPlus());
    }

    @Override
    public void receivePostInitialize() {
        logger.info("Sacred Bark Plus is active.");

        BaseMod.addSaveField("SBPPotionUseCountField", new CustomSavable<List<Integer>>() {
            @Override
            public List<Integer> onSave() {
                return AbstractDungeon.player.potions.stream().map(p -> PotionUsePatches.PotionUseField.useCount.get(p)).collect(Collectors.toCollection(ArrayList::new));
            }

            @Override
            public void onLoad(List<Integer> l) {
                int c = 0;
                if (l != null && !l.isEmpty()) {
                    for (AbstractPotion p : AbstractDungeon.player.potions) {
                        PotionUsePatches.PotionUseField.useCount.set(p, l.get(NumberUtils.min(c++, AbstractDungeon.player.potions.size() - 1)));
                    }
                }
            }
        });
    }

    @Override
    public void receivePostUpdate() {
        if (PotionLogicPatches.potion != null) {
            AbstractRelic sb = AbstractDungeon.player.getRelic(SacredBark.ID);
            if (sb != null) {
                sb.flash();
                AbstractDungeon.player.obtainPotion(PotionLogicPatches.potion.slot, PotionLogicPatches.potion);
            }
            PotionLogicPatches.potion = null;
        }
    }

    @Override
    public void receiveRelicGet(AbstractRelic r) {
        if (!CardCrawlGame.loadingSave) {
            if (hasHubris && ((r.relicId.equals(EmptyBottle.ID) && AbstractDungeon.player.hasRelic(SacredBark.ID)) || (r.relicId.equals(SacredBark.ID) && AbstractDungeon.player.hasRelic(EmptyBottle.ID)))) {
                AbstractDungeon.player.potions.stream()
                        .filter(p -> affectedPotions.contains(p.ID))
                        .filter(p -> PotionUsePatches.PotionUseField.useCount.get(p) > 1)
                        .forEach(p -> PotionUseCountField.useCount.set(p, PotionUseCountField.useCount.get(p) + 1));
            }
        }
    }
}