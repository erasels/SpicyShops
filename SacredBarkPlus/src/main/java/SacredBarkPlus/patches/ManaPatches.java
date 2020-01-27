package SacredBarkPlus.patches;

import SacredBarkPlus.actions.common.ChangeManaAction;
import SacredBarkPlus.util.ManaHelper;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.red.Strike_Red;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.characters.Ironclad;
import com.megacrit.cardcrawl.core.EnergyManager;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.screens.CharSelectInfo;
import com.megacrit.cardcrawl.vfx.campfire.CampfireSleepEffect;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.ArrayList;

public class ManaPatches {
    @SpirePatch(clz = AbstractPlayer.class, method = SpirePatch.CLASS)
    public static class ManaField {
        public static SpireField<Integer> mp = new SpireField<>(() -> -1);
        public static SpireField<Integer> maxMP = new SpireField<>(() -> -1);
    }

    @SpirePatch(clz = AbstractPlayer.class, method = "initializeClass")
    public static class InitMana {
        @SpirePostfixPatch
        public static void patch(AbstractPlayer __instance, String imgUrl, String shoulder2ImgUrl, String shouldImgUrl, String corpseImgUrl, CharSelectInfo info, float hb_x, float hb_y, float hb_w, float hb_h, EnergyManager energy) {
            ManaField.mp.set(__instance, ManaField.maxMP.get(__instance));
        }
    }

    @SpirePatch(clz = CampfireSleepEffect.class, method = "update")
    public static class RestoreManaOnRest {
        @SpireInsertPatch(locator = Locator.class)
        public static void patch(CampfireSleepEffect __instance) {
            if (ManaHelper.getMP() < ManaHelper.getMaxMP()) {
                ManaHelper.addMP(ManaHelper.getMaxMP() - ManaHelper.getMP());
            }
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractPlayer.class, "heal");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<Matcher>(), finalMatcher);
            }
        }
    }

    //TODO: Remove this!
    @SpirePatch(clz = Ironclad.class, method = SpirePatch.CONSTRUCTOR)
    public static class MagicIC {
        @SpirePrefixPatch
        public static void patch(Ironclad __instance) {
            ManaField.maxMP.set(__instance, 30);
        }
    }

    @SpirePatch(clz = Strike_Red.class, method = "use")
    public static class ManaTestingStuff {
        @SpirePrefixPatch
        public static void patch(Strike_Red __instance, AbstractPlayer p, AbstractMonster m) {
            AbstractDungeon.actionManager.addToBottom(new ChangeManaAction(3));
        }
    }
}
