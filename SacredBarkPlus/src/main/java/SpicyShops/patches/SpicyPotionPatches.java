package SpicyShops.patches;

import SpicyShops.SpicyShops;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.hubris.relics.EmptyBottle;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.ui.panels.PotionPopUp;
import com.megacrit.cardcrawl.ui.panels.TopPanel;
import javassist.CtBehavior;

import java.util.ArrayList;

import static SpicyShops.SpicyShops.hasHubris;

public class SpicyPotionPatches {
    //Shop system
    @SpirePatch(clz = ShopScreen.class, method = "initPotions")
    public static class BigPotionGen {
        @SpirePostfixPatch
        public static void patch(ShopScreen __instance, ArrayList<StorePotion> ___potions) {
            for(StorePotion p : ___potions) {
                if(AbstractDungeon.merchantRng.randomBoolean(0.2f)) {
                    p.price *= 1.8f;
                    PotionUseField.isBig.set(p.potion, true);
                }
            }
        }
    }

    //Big potion system
    public static final int USES = 2;

    @SpirePatch(clz = AbstractPotion.class, method = SpirePatch.CLASS)
    public static class PotionUseField {
        public static SpireField<Boolean> isBig = new SpireField<>(() -> false);
        public static SpireField<Integer> useCount = new SpireField<>(() -> USES);
    }

    //Render the number if the potion is affected, the player has sacred bark nad hubris/empty bottle isn't present
    @SpirePatch(clz = TopPanel.class, method = "renderPotions")
    public static class Render {
        public static void Postfix(TopPanel __instance, SpriteBatch sb) {
            boolean emptyBottleCheck = !(hasHubris && AbstractDungeon.player.hasRelic(EmptyBottle.ID));
            for (AbstractPotion p : AbstractDungeon.player.potions) {
                if (emptyBottleCheck && PotionUseField.isBig.get(p)) {
                    if (p.isObtained) {
                        FontHelper.renderFontRightTopAligned(sb, FontHelper.topPanelAmountFont,
                                String.valueOf(PotionUseField.useCount.get(p)),
                                p.posX + 20.0f * Settings.scale, p.posY - 14.0f * Settings.scale,
                                Settings.CREAM_COLOR);
                    }
                }
            }
        }
    }

    private static String[] bigPotion = CardCrawlGame.languagePack.getUIString(SpicyShops.makeID("BigPotion")).TEXT;

    @SpirePatch(clz = AbstractPotion.class, method = "shopRender")
    public static class BIGRenderPotion {
        @SpirePrefixPatch
        public static void addTip(AbstractPotion __instance, SpriteBatch sb) {
            if(PotionUseField.isBig.get(__instance)) {
                PowerTip pt = new PowerTip(bigPotion[0], bigPotion[1]);

                if(__instance.tips.stream().noneMatch(tip -> pt.header.equals(tip.header))) {
                    __instance.tips.add(pt);
                }
            }
        }

        @SpireInsertPatch(locator = Locator.class)
        public static void incScale(AbstractPotion __instance, SpriteBatch sb) {
            if(PotionUseField.isBig.get(__instance)) {
                __instance.scale *= 1.5f;
            }
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractPotion.class, "renderOutline");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    public static AbstractPotion potion;

    private static void Do(AbstractPotion pot) {
        if (!(hasHubris && AbstractDungeon.player.hasRelic(EmptyBottle.ID)) && PotionUseField.isBig.get(pot)) {
            int useCount = PotionUseField.useCount.get(pot);
            PotionUseField.useCount.set(pot, --useCount);

            if (useCount > 0) {
                potion = pot;
            }
        }
    }

    @SpirePatch(clz = AbstractPlayer.class, method = "damage")
    public static class FairyPotion {
        @SpireInsertPatch(locator = Locator.class, localvars = {"p"})
        public static void Insert(AbstractPlayer __instance, DamageInfo info, AbstractPotion potion) {
            Do(potion);
        }
    }

    @SpirePatch(clz = PotionPopUp.class, method = "updateInput")
    @SpirePatch(clz = PotionPopUp.class, method = "updateTargetMode")
    public static class NormalPotions {
        @SpireInsertPatch(locator = Locator.class, localvars = {"potion"})
        public static void Insert(PotionPopUp __instance, AbstractPotion potion) {
            Do(potion);
        }
    }

    private static class Locator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(TopPanel.class, "destroyPotion");
            return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
        }
    }
}
