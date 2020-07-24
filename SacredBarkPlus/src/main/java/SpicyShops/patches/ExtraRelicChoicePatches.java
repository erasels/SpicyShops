package SpicyShops.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StoreRelic;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.ArrayList;

public class ExtraRelicChoicePatches {

    @SpirePatch(clz = ShopScreen.class, method = "initRelics")
    public static class CreateAdditionalRelics {
        public static boolean reset = false;

        @SpirePrefixPatch
        public static void resetVars(ShopScreen __instance) {
            reset = false;
            lowestYPos = Integer.MAX_VALUE;
        }

        @SpireInsertPatch(locator = StoreRelicLocator.class, localvars = {"i"})
        public static void modifyCounter(ShopScreen __instance, @ByRef int[] i) {
            if(reset) {
                i[0] += 3;
            }
        }

        @SpireInsertPatch(locator = EndLoopLocator.class, localvars = {"i"})
        public static void resetLoop(ShopScreen __instance, @ByRef int[] i) {
            if(i[0] >= 2) {
                if(i[0] == 2 && !reset) {
                    reset = true;
                    //for loop increments i after so it has to be set to -1 to reset to 0
                    i[0] = -1;
                } else {
                    i[0] -= 3;
                }
            }
        }

        private static class EndLoopLocator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(ArrayList.class, "add");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<>(), finalMatcher);
            }
        }

        private static class StoreRelicLocator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.NewExprMatcher(StoreRelic.class);
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<>(), finalMatcher);
            }
        }
    }

    public static float lowestYPos;
    @SpirePatch(clz = StoreRelic.class, method = "update")
    public static class AdjustRelicPositions {
        @SpireInsertPatch(locator = Locator.class)
        public static void patch(StoreRelic __instance, float rugY, int ___slot) {
            //Calculate relic row through integer division which discards the rest and check with modulo whether it's the last relic of the row and adjust accordingly
            int relicRow = ((___slot +1) / 3) - ((___slot +1)%3 > 0? 0:1);
            __instance.relic.currentX = 1000.0F * Settings.scale + 150.0F * (___slot - (3*relicRow)) * Settings.scale;
            __instance.relic.currentY = rugY + (418f - (128f * relicRow)) * Settings.scale;
            lowestYPos = Math.min(__instance.relic.currentY, lowestYPos);
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(Hitbox.class, "move");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<>(), finalMatcher);
            }
        }
    }
}
