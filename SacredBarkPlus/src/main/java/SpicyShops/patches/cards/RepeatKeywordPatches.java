package SpicyShops.patches.cards;

import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class RepeatKeywordPatches {
    @SpirePatch(clz = AbstractCard.class, method = SpirePatch.CLASS)
    public static class RepeatField {
        public static SpireField<Boolean> repeat = new SpireField<>(() -> false);
    }

    @SpirePatch(clz= AbstractPlayer.class, method="useCard")
    public static class MultiUse {
        @SpireInstrumentPatch
        public static ExprEditor repeatEffect() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getClassName().equals(AbstractCard.class.getName()) && m.getMethodName().equals("use")) {
                        m.replace("{" +
                                "if(" + MultiUse.class.getName() + ".getRepeat(c)) {" +
                                "c.use($$);" +
                                "}" +
                                "$proceed($$);" +
                                "}");
                    }
                }
            };
        }

        public static boolean getRepeat(AbstractCard c) {
            return RepeatField.repeat.get(c);
        }
    }

    @SpirePatch(clz = AbstractCard.class, method = "makeStatEquivalentCopy")
    public static class KeepField {
        @SpirePostfixPatch
        public static AbstractCard carryOverRepeats(AbstractCard __result, AbstractCard __instance) {
            RepeatField.repeat.set(__result, MultiUse.getRepeat(__instance));
            return __result;
        }
    }
}
