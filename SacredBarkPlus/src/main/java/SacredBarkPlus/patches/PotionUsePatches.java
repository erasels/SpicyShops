package SacredBarkPlus.patches;

import SacredBarkPlus.SacredBarkPlus;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.hubris.relics.EmptyBottle;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.relics.SacredBark;
import com.megacrit.cardcrawl.ui.panels.TopPanel;

import static SacredBarkPlus.SacredBarkPlus.affectedPotions;
import static SacredBarkPlus.SacredBarkPlus.hasHubris;

public class PotionUsePatches {
    @SpirePatch(clz = AbstractPotion.class, method = SpirePatch.CLASS)
    public static class PotionUseField {
        public static SpireField<Integer> useCount = new SpireField<>(() -> SacredBarkPlus.USES);
    }

    //Render the number if the potion is affected, the player has sacred bark nad hubris/empty bottle isn't present
    @SpirePatch(clz = TopPanel.class, method = "renderPotions")
    public static class Render {
        public static void Postfix(TopPanel __instance, SpriteBatch sb) {
            if(!(hasHubris && AbstractDungeon.player.hasRelic(EmptyBottle.ID)) && AbstractDungeon.player.hasRelic(SacredBark.ID)) {
                for (AbstractPotion p : AbstractDungeon.player.potions) {
                    if (p.isObtained && affectedPotions.contains(p.ID)) {
                        FontHelper.renderFontRightTopAligned(sb, FontHelper.topPanelAmountFont,
                                String.valueOf(PotionUseField.useCount.get(p)),
                                p.posX + 20.0f * Settings.scale, p.posY - 14.0f * Settings.scale,
                                Settings.CREAM_COLOR);
                    }
                }
            }
        }
    }


}