package SpicyShops.patches;

import SpicyShops.SpicyShops;
import SpicyShops.cardMods.AbstractSpicySaleCMod;
import basemod.helpers.CardModifierManager;
import basemod.patches.com.megacrit.cardcrawl.cards.AbstractCard.CardModifierPatches;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.audio.SoundMaster;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.shop.OnSaleTag;
import com.megacrit.cardcrawl.shop.ShopScreen;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.ArrayList;

public class SpicyCardPatches {
    public static ArrayList<AbstractCard> spicyCards = new ArrayList<>();

    @SpirePatch(clz = ShopScreen.class, method = "init")
    public static class InitCardHook {
        @SpireInsertPatch(locator = Locator.class)
        public static void patch(ShopScreen __instance, ArrayList<AbstractCard> coloredCards, ArrayList<AbstractCard> colorlessCards) {
            spicyCards.clear();
            ArrayList<AbstractCard> allCards = new ArrayList<>();
            allCards.addAll(coloredCards);
            allCards.addAll(colorlessCards);
            float roll = AbstractDungeon.merchantRng.random(1f);
            int spicyAmt = 1 + (roll > 0.49f ? 1 : 0) + (roll > 0.74f ? 1 : 0) + (roll > 0.9f ? 1 : 0);

            for (int i = 0; i < spicyAmt; i++) {
                AbstractCard c = allCards.get(AbstractDungeon.merchantRng.random(0, allCards.size() - 1));
                allCards.remove(c);

                ArrayList<AbstractSpicySaleCMod> applicable = new ArrayList<>();
                for (AbstractSpicySaleCMod cmod: SpicyShops.cardMods) {
                    if(cmod.isApplicable(c)) {
                        applicable.add(cmod);
                    }
                }

                if(applicable.isEmpty()) {
                    continue;
                }
                spicyCards.add(c);
                AbstractSpicySaleCMod mod = applicable.get(AbstractDungeon.merchantRng.random(0, applicable.size() - 1));
                CardModifierManager.addModifier(c, mod);
                c.price *= mod.getPriceMod(c);
            }
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(ShopScreen.class, "initRelics");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<>(), finalMatcher);
            }
        }
    }

    @SpirePatch(clz = ShopScreen.class, method = "purchaseCard")
    public static class PurchaseCardHooks {
        @SpireInsertPatch(locator = Locator.class)
        public static void removeSpicyCard(ShopScreen __instance, AbstractCard card) {
            spicyCards.remove(card);
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(SoundMaster.class, "play");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<>(), finalMatcher);
            }
        }
    }

    @SpirePatch(clz = ShopScreen.class, method = "render")
    public static class RenderSpicyCardTags {
        @SpireInsertPatch(locator = Locator.class)
        public static void patch(ShopScreen __instance, SpriteBatch sb, OnSaleTag ___saleTag) {
            for (AbstractCard c : spicyCards) {
                boolean isSale = SpicyShops.hasReplay || ___saleTag.card == c;
                AbstractSpicySaleCMod mod = (AbstractSpicySaleCMod) CardModifierPatches.CardModifierFields.cardModifiers.get(c).stream().filter(cmod -> cmod instanceof AbstractSpicySaleCMod).findAny().get();
                sb.setColor(Color.WHITE);
                sb.draw(SpicyShops.tagTextures.get(mod.getTexturePath()), c.current_x + ((isSale?-20f:30f) * Settings.scale) + (c.drawScale - 0.75F) * 60.0F * Settings.scale, c.current_y + 60.0F * Settings.scale + (c.drawScale - 0.75F) * 90.0F * Settings.scale, 128.0F * Settings.scale * c.drawScale, 128.0F * Settings.scale * c.drawScale);
                sb.setBlendFunction(770, 1);
                sb.setColor(new Color(1.0F, 1.0F, 1.0F, (MathUtils.cosDeg((float) (System.currentTimeMillis() / 5L % 360L)) + 1.25F) / 3.0F));
                sb.draw(SpicyShops.tagTextures.get(mod.getTexturePath()), c.current_x + ((isSale?-20f:30f) * Settings.scale) + (c.drawScale - 0.75F) * 60.0F * Settings.scale, c.current_y + 60.0F * Settings.scale + (c.drawScale - 0.75F) * 90.0F * Settings.scale, 128.0F * Settings.scale * c.drawScale, 128.0F * Settings.scale * c.drawScale);
                sb.setBlendFunction(770, 771);
                sb.setColor(Color.WHITE);
            }
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(ShopScreen.class, "renderRelics");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<>(), finalMatcher);
            }
        }
    }
}
