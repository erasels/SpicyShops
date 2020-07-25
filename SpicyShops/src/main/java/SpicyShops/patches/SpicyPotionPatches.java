package SpicyShops.patches;

import SpicyShops.SpicyShops;
import SpicyShops.vfx.ConcentratedPotionEffect;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.mod.hubris.relics.EmptyBottle;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.potions.*;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.ui.panels.PotionPopUp;
import com.megacrit.cardcrawl.ui.panels.TopPanel;
import javassist.CtBehavior;

import java.util.ArrayList;
import java.util.Arrays;

import static SpicyShops.SpicyShops.hasHubris;
import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.merchantRng;

public class SpicyPotionPatches {
    //Shop system
    @SpirePatch(clz = ShopScreen.class, method = "initPotions")
    public static class PotionModifierGen {
        @SpirePostfixPatch
        public static void patch(ShopScreen __instance, ArrayList<StorePotion> ___potions) {
            for (StorePotion p : ___potions) {
                float roll = merchantRng.random(1.0f);
                if (roll > 0.75f) {
                    if (roll < 0.9f && canBeConcentrated(p.potion)) {
                        p.price *= 1.5f;
                        PotionUseField.isConcentrated.set(p.potion, true);
                        p.potion.initializeData();
                    } else {
                        p.price *= 2f;
                        PotionUseField.isBig.set(p.potion, true);
                    }
                }
            }
        }

        private static boolean canBeConcentrated(AbstractPotion p) {
            return !(new ArrayList<>(Arrays.asList(Ambrosia.POTION_ID, BlessingOfTheForge.POTION_ID, Elixir.POTION_ID, EntropicBrew.POTION_ID, GamblersBrew.POTION_ID, SmokeBomb.POTION_ID, StancePotion.POTION_ID, AttackPotion.POTION_ID, PowerPotion.POTION_ID, SkillPotion.POTION_ID)).contains(p.ID));

        }
    }

    //Potion modifier system
    public static final int USES = 2;

    @SpirePatch(clz = AbstractPotion.class, method = SpirePatch.CLASS)
    public static class PotionUseField {
        public static SpireField<Boolean> isBig = new SpireField<>(() -> false);
        public static SpireField<Integer> useCount = new SpireField<>(() -> USES);

        public static SpireField<Boolean> isConcentrated = new SpireField<>(() -> false);
    }

    //Render stuff if the potion has a modifier
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
                } else if (PotionUseField.isConcentrated.get(p)) {
                    if (p.isObtained) {
                        FontHelper.renderFontRightTopAligned(sb, FontHelper.topPanelAmountFont,
                                "+",
                                p.posX - 15.0f * Settings.scale, p.posY - 14.0f * Settings.scale,
                                Color.FOREST);
                    }
                }
            }
        }
    }

    @SpirePatch(clz = AbstractPotion.class, method = "shopRender")
    public static class RenderModifierTips {
        private static float sparkleTimer = 0.5F;

        @SpirePrefixPatch
        public static void addTip(AbstractPotion __instance, SpriteBatch sb) {
            if (PotionUseField.isBig.get(__instance)) {
                PowerTip pt = new PowerTip(bigPotion[0], bigPotion[1]);

                if (__instance.tips.stream().noneMatch(tip -> pt.header.equals(tip.header))) {
                    __instance.tips.add(pt);
                }
            } else if (PotionUseField.isConcentrated.get(__instance)) {
                PowerTip pt = new PowerTip(concPotion[0], concPotion[1]);

                if (__instance.tips.stream().noneMatch(tip -> pt.header.equals(tip.header))) {
                    __instance.tips.add(pt);
                }

                sparkleTimer -= Gdx.graphics.getDeltaTime();
                if (!Settings.DISABLE_EFFECTS && sparkleTimer < 0.0F) {
                    AbstractDungeon.topLevelEffectsQueue.add(new ConcentratedPotionEffect(__instance.hb));
                    sparkleTimer = MathUtils.random(0.4F, 0.05F);
                }
            }
        }

        //Increase scale of potion in shop to show it's special
        @SpireInsertPatch(locator = Locator.class)
        public static void incScale(AbstractPotion __instance, SpriteBatch sb) {
            if (PotionUseField.isBig.get(__instance)) {
                __instance.scale *= 1.5f;
            }
        }

        @SpirePostfixPatch
        public static void undoIncrease(AbstractPotion __instance, SpriteBatch sb) {
            if (PotionUseField.isBig.get(__instance)) {
                __instance.scale /= 1.5f;
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

    //Concentrated potions
    private static String[] concPotion = CardCrawlGame.languagePack.getUIString(SpicyShops.makeID("ConcentratedPotion")).TEXT;

    @SpirePatch(clz = AbstractPotion.class, method = "getPotency", paramtypez = {})
    public static class IncreasePotency {
        @SpireInsertPatch(locator = Locator.class, localvars = {"potency"})
        public static void patch(AbstractPotion __instance, @ByRef int[] potency) {
            if (PotionUseField.isConcentrated.get(__instance)) {
                potency[0] *= 1.5f;
            }
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractPlayer.class, "hasRelic");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    //Big potions
    private static String[] bigPotion = CardCrawlGame.languagePack.getUIString(SpicyShops.makeID("BigPotion")).TEXT;

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
