package SpicyShops.patches.cards;

import SpicyShops.cardMods.FreeButExhaustCMod;
import basemod.helpers.CardModifierManager;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;

public class CardCostModificationPatches {
    @SpirePatch(clz = AbstractCard.class, method = "freeToPlay")
    public static class RecklessAttackPatch {
        @SpirePostfixPatch
        public static boolean patch(boolean __result, AbstractCard __instance) {
            return __result || (CardModifierManager.hasModifier(__instance, FreeButExhaustCMod.ID));
        }
    }
}
