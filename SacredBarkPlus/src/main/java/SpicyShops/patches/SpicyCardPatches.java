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
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.shop.ShopScreen;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.ArrayList;

public class SpicyCardPatches {
    private static final int MAX_SPICY_CARDS = 3;

    public static ArrayList<AbstractCard> spicyCards = new ArrayList<>();

    @SpirePatch(clz = ShopScreen.class, method = "init")
    public static class InitCardHook {
        @SpireInsertPatch(locator = Locator.class)
        public static void patch(ShopScreen __instance, ArrayList<AbstractCard> coloredCards, ArrayList<AbstractCard> colorlessCards) {
            spicyCards.clear();
            ArrayList<AbstractCard> allCards = new ArrayList<>();
            allCards.addAll(coloredCards);
            allCards.addAll(colorlessCards);
            int spicyAmt = 1 + (AbstractDungeon.merchantRng.randomBoolean(0.4f) ? 1 : 0) + (AbstractDungeon.merchantRng.randomBoolean(0.25f) ? 1 : 0);

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

    @SpirePatch(clz = ShopScreen.class, method = "render")
    public static class RenderSpicyCardTags {
        @SpireInsertPatch(locator = Locator.class)
        public static void patch(ShopScreen __instance, SpriteBatch sb) {
            for (AbstractCard c : spicyCards) {
                AbstractSpicySaleCMod mod = (AbstractSpicySaleCMod) CardModifierPatches.CardModifierFields.cardModifiers.get(c).stream().filter(cmod -> cmod instanceof AbstractSpicySaleCMod).findAny().get();
                sb.setColor(Color.WHITE);
                sb.draw(SpicyShops.tagTextures.get(mod.getTexturePath()), c.current_x - 20f * Settings.scale + (c.drawScale - 0.75F) * 60.0F * Settings.scale, c.current_y + 60.0F * Settings.scale + (c.drawScale - 0.75F) * 90.0F * Settings.scale, 128.0F * Settings.scale * c.drawScale, 128.0F * Settings.scale * c.drawScale);
                sb.setBlendFunction(770, 1);
                sb.setColor(new Color(1.0F, 1.0F, 1.0F, (MathUtils.cosDeg((float) (System.currentTimeMillis() / 5L % 360L)) + 1.25F) / 3.0F));
                sb.draw(SpicyShops.tagTextures.get(mod.getTexturePath()), c.current_x - 20f * Settings.scale + (c.drawScale - 0.75F) * 60.0F * Settings.scale, c.current_y + 60.0F * Settings.scale + (c.drawScale - 0.75F) * 90.0F * Settings.scale, 128.0F * Settings.scale * c.drawScale, 128.0F * Settings.scale * c.drawScale);
                sb.setBlendFunction(770, 771);
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
