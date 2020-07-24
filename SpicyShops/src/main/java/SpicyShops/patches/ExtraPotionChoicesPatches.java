package SpicyShops.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.ArrayList;

public class ExtraPotionChoicesPatches {
    @SpirePatch(clz = ShopScreen.class, method = "initPotions")
    public static class CreateAdditionalRelics {
        public static boolean reset = false;

        @SpirePrefixPatch
        public static void resetVars(ShopScreen __instance) {
            reset = false;
        }

        @SpireInsertPatch(locator = StorePotionLocator.class, localvars = {"i"})
        public static void modifyCounter(ShopScreen __instance, @ByRef int[] i) {
            if (reset) {
                i[0]++;
            }
        }

        @SpireInsertPatch(locator = EndLoopLocator.class, localvars = {"i"})
        public static void resetLoop(ShopScreen __instance, @ByRef int[] i) {
            if (i[0] == 2 && !reset) {
                reset = true;
                //for loop increments i after so it has to be set to -1 to reset to 0
                i[0] = 1;
            }
        }

        private static class EndLoopLocator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(ArrayList.class, "add");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<>(), finalMatcher);
            }
        }

        private static class StorePotionLocator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.NewExprMatcher(StorePotion.class);
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<>(), finalMatcher);
            }
        }
    }

    @SpirePatch(clz = StorePotion.class, method = "update")
    public static class AdjustPotionsPositions {
        @SpireInsertPatch(locator = Locator.class)
        public static void patch(StorePotion __instance, float rugY, int ___slot) {
            if (___slot > 0) {
                __instance.potion.posX -= 50f * ___slot * Settings.scale;
            }

            __instance.potion.posY -= 60f * Settings.scale;
            //Down-adjusted the lowest y position to account for the relic price text
            float diff = (ExtraRelicChoicePatches.lowestYPos - 128f * Settings.scale) - __instance.potion.posY;
            if (diff < 0) {
                //Add the difference since a negative value will decrease/lower the y position
                __instance.potion.posY += diff;
            }
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(Hitbox.class, "move");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<>(), finalMatcher);
            }
        }
    }
}
