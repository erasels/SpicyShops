package SpicyShops.patches;

import SpicyShops.SpicyShops;
import SpicyShops.util.HelperClass;
import SpicyShops.util.TextureLoader;
import SpicyShops.vfx.ConcentratedPotionEffect;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.screens.SingleRelicViewPopup;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StoreRelic;
import com.megacrit.cardcrawl.vfx.FastCardObtainEffect;
import javassist.CtBehavior;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class SpicyRelicPatches {
    public enum Modifiers {
        NONE, POTION_SACRIFICE, UNIDENTIFIABLE
    }
    private static final float RELIC_MODIFIER_CHANCE = 0.33f;
    private static final float CURSE_DISCOUNT = 0.4f; //60%
    private static final int POT_DISCOUNT = 200;
    private static final float UNID_DISCOUNT = 0.5f;

    private static ArrayList<PowerTip> storedPTs;

    private static String[] cruseTrade = CardCrawlGame.languagePack.getUIString(SpicyShops.makeID("CurseRelicTrade")).TEXT;
    private static String[] potTrade = CardCrawlGame.languagePack.getUIString(SpicyShops.makeID("PotionSacrifice")).TEXT;
    private static String[] unidTrade = CardCrawlGame.languagePack.getUIString(SpicyShops.makeID("Unidentifiable")).TEXT;

    private static Texture unidRelic = TextureLoader.getTexture(SpicyShops.makeUIPath("unidRelic.png"));
    private static Texture sacPot = TextureLoader.getTexture(SpicyShops.makeUIPath("sacrificePotion.png"));

    @SpirePatch(clz = StoreRelic.class, method = SpirePatch.CLASS)
    public static class ShopRelicFields {
        public static SpireField<AbstractCard> bonusCard = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = AbstractRelic.class, method = SpirePatch.CLASS)
    public static class SpicyRelicFields {
        public static SpireField<Modifiers> modifier = new SpireField<>(() -> Modifiers.NONE);
    }

    @SpirePatch(clz = ShopScreen.class, method = "init")
    public static class InitHook {
        //Hook in after discounts are applied
        @SpirePostfixPatch
        public static void patch(ShopScreen __instance, ArrayList<AbstractCard> coloredCards, ArrayList<AbstractCard> colorlessCards, ArrayList<StoreRelic> ___relics) {
            storedPTs = new ArrayList<>();
            ArrayList<StoreRelic> unmodified = new ArrayList<>(___relics);
            StoreRelic rel;

            //Bonus curse
            if(AbstractDungeon.merchantRng.randomBoolean(RELIC_MODIFIER_CHANCE)) {
                rel = HelperClass.getRandomItem(unmodified, AbstractDungeon.merchantRng);
                if(rel != null) {
                    unmodified.remove(rel);
                    ShopRelicFields.bonusCard.set(rel, CardLibrary.getCard(HelperClass.getRandomItem(SpicyShops.vanillaCurses, AbstractDungeon.merchantRng)).makeCopy());

                    SpicyShops.logger.info(rel.relic.name + " has a curse (" + ShopRelicFields.bonusCard.get(rel).name + ") attached to it for a discount. Original cost: " + rel.price);
                    rel.price *= CURSE_DISCOUNT;
                    rel.relic.tips.add(new PowerTip(cruseTrade[0], cruseTrade[1] + " NL ( " + FontHelper.colorString(ShopRelicFields.bonusCard.get(rel).name, "r") + " )"));
                }
            }

            //Unidentifiable relic
            if(AbstractDungeon.merchantRng.randomBoolean(RELIC_MODIFIER_CHANCE)) {
                rel = HelperClass.getRandomItem(unmodified, AbstractDungeon.merchantRng);
                if(rel != null) {
                    unmodified.remove(rel);
                    SpicyRelicFields.modifier.set(rel.relic, Modifiers.UNIDENTIFIABLE);

                    SpicyShops.logger.info("Nice try ;) is unidentifiable for a discount. Original cost: " + rel.price);
                    rel.price *= UNID_DISCOUNT;
                    storedPTs.addAll(rel.relic.tips);
                    rel.relic.tips.clear();
                    rel.relic.tips.add(new PowerTip(unidTrade[0], unidTrade[1]));
                }
            }

            //Potion slot offering
            ArrayList<StoreRelic> rares = unmodified.stream().filter(r -> r.relic.tier == AbstractRelic.RelicTier.RARE).collect(Collectors.toCollection(ArrayList::new));
            if(!rares.isEmpty() && AbstractDungeon.merchantRng.randomBoolean(RELIC_MODIFIER_CHANCE)) {
                rel = HelperClass.getRandomItem(rares, AbstractDungeon.merchantRng);
                if(rel != null) {
                    unmodified.remove(rel);
                    SpicyRelicFields.modifier.set(rel.relic, Modifiers.POTION_SACRIFICE);

                    int tmp = POT_DISCOUNT + (AbstractDungeon.ascensionLevel >= 16? 0:20);
                    SpicyShops.logger.info(rel.relic.name + " will lose you a potion slot for a "+tmp+" gold discount. Original cost: " + rel.price);
                    rel.price = NumberUtils.max(0, rel.price - tmp);
                    rel.relic.tips.add(new PowerTip(potTrade[0], potTrade[1]));
                }
            }
        }
    }

    @SpirePatch(clz = StoreRelic.class, method = "render")
    public static class RenderRelicSpecials {
        private static float curseTimer = 0.75F;
        private static float potTimer = 0.25F;
        private static Color transluscentCol = Color.WHITE.cpy();

        @SpirePostfixPatch
        public static void patch(StoreRelic __instance, SpriteBatch sb) {
            AbstractCard c = ShopRelicFields.bonusCard.get(__instance);

            //Potion Sacrifice
            if(SpicyRelicFields.modifier.get(__instance.relic) == Modifiers.POTION_SACRIFICE) {
                transluscentCol.a = NumberUtils.min((MathUtils.cosDeg((float) (System.currentTimeMillis() / 5L % 360L)) + 1.25F) / 3F, 0.66f);
                sb.setColor(transluscentCol);
                sb.draw(sacPot, __instance.relic.hb.cX, __instance.relic.hb.y - 25f * Settings.scale);
                sb.setColor(Color.WHITE);
                potTimer -= Gdx.graphics.getDeltaTime();
                if (!Settings.DISABLE_EFFECTS && potTimer < 0.0F) {
                    Color col = MathUtils.randomBoolean()?Color.YELLOW : Color.BROWN;

                    AbstractDungeon.topLevelEffectsQueue.add(new ConcentratedPotionEffect(__instance.relic.hb, col));
                    potTimer = MathUtils.random(0.2F, 0.4F);
                }
            }

            //Unidentifiable relic
            else if(SpicyRelicFields.modifier.get(__instance.relic) == Modifiers.UNIDENTIFIABLE) {
                sb.draw(unidRelic,
                        __instance.relic.currentX - 64.0F,
                        __instance.relic.currentY - 64.0F,
                        64.0F,
                        64.0F,
                        128.0F,
                        128.0F,
                        __instance.relic.scale,
                        __instance.relic.scale,
                        0,
                        0,
                        0,
                        128,
                        128,
                        false,
                        false);
            }

            //Cruse discount
            else if(c != null) {
                if(c.type == AbstractCard.CardType.CURSE) {
                    c.drawScale = __instance.relic.scale / 5f;
                    //Draw card right of the relic if unhovered and left of it, if it is hovered, so that it doesn't overlap with the powertips
                    c.current_x = __instance.relic.hb.x + (((__instance.relic.hb.width + (!__instance.relic.hb.hovered?(25f * Settings.scale):0)) * (__instance.relic.hb.hovered?-1f:1f)) * __instance.relic.scale);
                    c.current_y = __instance.relic.hb.y + ((__instance.relic.hb.height/2f)*__instance.relic.scale);
                    c.render(sb);

                    curseTimer -= Gdx.graphics.getDeltaTime();
                    if (!Settings.DISABLE_EFFECTS && curseTimer < 0.0F) {
                        Color col = MathUtils.randomBoolean()?Color.FIREBRICK : Color.PURPLE;

                        AbstractDungeon.topLevelEffectsQueue.add(new ConcentratedPotionEffect(__instance.relic.hb, col));
                        curseTimer = MathUtils.random(0.4F, 0.65F);
                    }
                }
            }
        }
    }

    @SpirePatch(clz = StoreRelic.class, method = "purchaseRelic")
    public static class RelicBuyLogic {
        @SpireInsertPatch(locator = Locator.class)
        public static void patch(StoreRelic __instance) {
            //Curse discount
            AbstractCard c = ShopRelicFields.bonusCard.get(__instance);
            if(c != null) {
                if(c.type == AbstractCard.CardType.CURSE) {
                    AbstractDungeon.topLevelEffects.add(new FastCardObtainEffect(c, c.current_x, c.current_y));
                    ShopRelicFields.bonusCard.set(__instance, null);
                    __instance.relic.tips.removeIf(pt -> pt.header.equalsIgnoreCase(cruseTrade[0]));
                }
            }

            switch(SpicyRelicFields.modifier.get(__instance.relic)) {
                case POTION_SACRIFICE:
                    __instance.relic.tips.removeIf(pt -> pt.header.equalsIgnoreCase(potTrade[0]));
                    AbstractDungeon.player.potions.remove(AbstractDungeon.player.potions.size() -1);
                    AbstractDungeon.player.potionSlots = NumberUtils.max(0, AbstractDungeon.player.potionSlots - 1);
                    break;
                case UNIDENTIFIABLE:
                    __instance.relic.tips.clear();
                    __instance.relic.tips.addAll(storedPTs);
                    storedPTs.clear();
                    break;

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

    //Sacrifice Potion Slots
    @SpirePatch(clz = CardCrawlGame.class, method = "loadPlayerSave")
    public static class LetMeHaveNoPotionSlots {
        @SpireInsertPatch(locator = Locator.class)
        public static void patch(CardCrawlGame __instance, AbstractPlayer p) {
            p.potionSlots = CardCrawlGame.saveFile.potion_slots;
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(ArrayList.class, "clear");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    //Unidentified relic
    @SpirePatch(clz = StoreRelic.class, method = "update")
    public static class DisallowPopup {
        @SpireInsertPatch(locator = Locator.class)
        public static SpireReturn<Void> patch(StoreRelic __instance, float rug) {
            if(SpicyRelicFields.modifier.get(__instance.relic) == Modifiers.UNIDENTIFIABLE) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(SingleRelicViewPopup.class, "open");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }
}