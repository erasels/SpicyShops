package SpicyShops.patches;

import SpicyShops.SpicyShops;
import SpicyShops.util.HelperClass;
import SpicyShops.vfx.ConcentratedPotionEffect;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StoreRelic;
import com.megacrit.cardcrawl.vfx.FastCardObtainEffect;
import javassist.CtBehavior;

import java.util.ArrayList;

public class SpicyRelicPatches {
    private static final float CURSE_DISCOUNT_CHANCE = 0.25f;
    private static final float CURSE_DISCOUNT = 0.4f;

    private static String[] cruseTrade = CardCrawlGame.languagePack.getUIString(SpicyShops.makeID("CurseRelicTrade")).TEXT;

    @SpirePatch(clz = StoreRelic.class, method = SpirePatch.CLASS)
    public static class ShopRelicFields {
        public static SpireField<AbstractCard> bonusCard = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = ShopScreen.class, method = "init")
    public static class InitHook {
        //Hook in after discounts are applied
        @SpirePostfixPatch
        public static void patch(ShopScreen __instance, ArrayList<AbstractCard> coloredCards, ArrayList<AbstractCard> colorlessCards, ArrayList<StoreRelic> ___relics) {
            if(AbstractDungeon.merchantRng.randomBoolean(CURSE_DISCOUNT_CHANCE)) {
                StoreRelic rel = HelperClass.getRandomItem(___relics, AbstractDungeon.merchantRng);
                if(rel != null) {
                    ShopRelicFields.bonusCard.set(rel, CardLibrary.getCard(HelperClass.getRandomItem(SpicyShops.vanillaCurses, AbstractDungeon.merchantRng)).makeCopy());
                    SpicyShops.logger.info(rel.relic.name + " has a curse (" + ShopRelicFields.bonusCard.get(rel).name + ") attached to it for a discount. Original cost: " + rel.price);
                    rel.price *= CURSE_DISCOUNT;

                    rel.relic.tips.add(new PowerTip(cruseTrade[0], cruseTrade[1]));
                }
            }
        }
    }

    @SpirePatch(clz = StoreRelic.class, method = "render")
    public static class RenderRelicSpecials {
        private static float sparkleTimer = 0.75F;

        @SpirePostfixPatch
        public static void patch(StoreRelic __instance, SpriteBatch sb) {
            AbstractCard c = ShopRelicFields.bonusCard.get(__instance);
            if(c != null) {
                if(c.type == AbstractCard.CardType.CURSE) {
                    c.drawScale = __instance.relic.scale - 0.8f;
                    //Draw card right of the relic if unhovered and left of it, if it is hovered, so that it doesn't overlap with the powertips
                    c.current_x = __instance.relic.hb.x + (((__instance.relic.hb.width + (!__instance.relic.hb.hovered?(25f * Settings.scale):0)) * (__instance.relic.hb.hovered?-1f:1f)) * __instance.relic.scale);
                    c.current_y = __instance.relic.hb.y + ((__instance.relic.hb.height/2f)*__instance.relic.scale);
                    c.render(sb);

                    sparkleTimer -= Gdx.graphics.getDeltaTime();
                    if (!Settings.DISABLE_EFFECTS && sparkleTimer < 0.0F) {
                        Color col = Color.WHITE;
                        switch(MathUtils.random(0, 2)) {
                            case 0:
                                col = Color.FIREBRICK;
                                break;
                            case 1:
                                col = Color.PURPLE;
                                break;
                            case 2:
                                col = Color.DARK_GRAY;
                        }
                        AbstractDungeon.topLevelEffectsQueue.add(new ConcentratedPotionEffect(__instance.relic.hb, col));
                        sparkleTimer = MathUtils.random(0.45F, 0.65F);
                    }
                }
            }
        }
    }

    @SpirePatch(clz = StoreRelic.class, method = "purchaseRelic")
    public static class BonusCardLogicOnRelicBuy {
        @SpireInsertPatch(locator = Locator.class)
        public static void patch(StoreRelic __instance) {
            AbstractCard c = ShopRelicFields.bonusCard.get(__instance);
            if(c != null) {
                if(c.type == AbstractCard.CardType.CURSE) {
                    AbstractDungeon.topLevelEffects.add(new FastCardObtainEffect(c, c.current_x, c.current_y));
                    ShopRelicFields.bonusCard.set(__instance, null);
                    __instance.relic.tips.removeIf(pt -> pt.header.equalsIgnoreCase(cruseTrade[0]));
                }
            }
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractRelic.class, "flash");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }
}