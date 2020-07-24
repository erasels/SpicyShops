package SpicyShops.patches.cards;

import SpicyShops.cardMods.CostIncButRefundCMod;
import SpicyShops.util.HelperClass;
import basemod.BaseMod;
import com.evacipated.cardcrawl.mod.stslib.Keyword;
import com.evacipated.cardcrawl.mod.stslib.StSLib;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.ArrayList;

@SpirePatch(clz = StSLib.class, method = "loadLangKeywords")
public class StealStSLibKeywordPatches {
    @SpireInsertPatch(locator = Locator.class, localvars = {"keyword"})
    public static void patch(StSLib __instance, String language, Keyword keyword) {
        if(keyword.NAMES.length > 1 && keyword.NAMES[1].equalsIgnoreCase("refund") || keyword.NAMES[0].equalsIgnoreCase("refund")) {
            CostIncButRefundCMod.localizedRefundName = HelperClass.capitalize(keyword.NAMES[0]);
        }
    }

    private static class Locator extends SpireInsertLocator {
        public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(BaseMod.class, "addKeyword");
            return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<>(), finalMatcher);
        }
    }
}
