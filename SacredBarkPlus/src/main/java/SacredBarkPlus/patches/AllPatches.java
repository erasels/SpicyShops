package SacredBarkPlus.patches;

import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.characters.TheSilent;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.RingOfTheSerpent;
import com.megacrit.cardcrawl.relics.SacredBark;
import com.megacrit.cardcrawl.relics.SnakeRing;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.ui.panels.PotionPopUp;
import com.megacrit.cardcrawl.ui.panels.TopPanel;
import javassist.*;

import java.util.ArrayList;


public class AllPatches {
    @SpirePatch(clz = TheSilent.class, method = "getStartingDeck")
    public static class BigFuckSilent {
        @SpirePostfixPatch
        public static ArrayList<String> patch(ArrayList<String> __result, TheSilent __instance) {
            __result.remove("Defend_G");
            __result.remove("Strike_G");
            return __result;
        }
    }

    @SpirePatch(clz = SnakeRing.class, method = "atBattleStart")
    public static class SnakeRing_FuckOriginalEffect {
        @SpirePrefixPatch
        public static SpireReturn patch(SnakeRing __instance) {
            return SpireReturn.Return(null);
        }
    }

    @SpirePatch(clz = SnakeRing.class, method = "getUpdatedDescription")
    public static class SnakeRing_FuckOriginalDesc {
        @SpirePostfixPatch
        public static String patch(SnakeRing __instance) {
            return "The first time you gain #yBlock each combat, gain #b6 #yBlock.";
        }
    }

    @SpirePatch(clz = SnakeRing.class, method = SpirePatch.CONSTRUCTOR)
    public static class SnakeRing_NewEffect {
        public static void Raw(CtBehavior ctMethodToPatch) throws NotFoundException, CannotCompileException {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
            ClassPool pool = ctClass.getClassPool();
            CtClass ctAbstractRoom = pool.get(AbstractRoom.class.getName());

            CtMethod method = CtNewMethod.make(
                    CtClass.intType, // Return
                    "onPlayerGainedBlock", // Method name
                    new CtClass[]{CtClass.floatType}, //Paramters
                    null, // Exceptions
                    "{" +
                            "if($1 > 0 && !grayscale) { " +
                            "this.flash();" +
                            "addToBot(new com.megacrit.cardcrawl.actions.common.GainBlockAction(com.megacrit.cardcrawl.dungeons.AbstractDungeon.player, com.megacrit.cardcrawl.dungeons.AbstractDungeon.player, 6));" +
                            "this.grayscale = true;" +
                            "}" +
                            "return com.badlogic.gdx.math.MathUtils.floor($1);" +
                     "}",
                    ctClass
            );
            ctClass.addMethod(method);

            CtMethod method2 = CtNewMethod.make(
                    CtClass.voidType, // Return
                    "justEnteredRoom", // Method name
                    new CtClass[]{ctAbstractRoom}, //Paramters
                    null, // Exceptions
                    "{" +
                                "this.grayscale = false;" +
                            "}",
                    ctClass
            );
            ctClass.addMethod(method2);
        }
    }

    @SpirePatch(clz = RingOfTheSerpent.class, method = "onUnequip")
    @SpirePatch(clz = RingOfTheSerpent.class, method = "onEquip")
    public static class SerpentRing_FuckOriginalEffect {
        @SpirePrefixPatch
        public static SpireReturn patch(RingOfTheSerpent __instance) {
            return SpireReturn.Return(null);
        }
    }

    @SpirePatch(clz = RingOfTheSerpent.class, method = "getUpdatedDescription")
    public static class SerpentRing_FuckOriginalDesc {
        @SpirePostfixPatch
        public static String patch(RingOfTheSerpent __instance) {
            return "The first time you gain #yBlock each turn, gain #b3 #yBlock and draw #b1 card.";
        }
    }

    @SpirePatch(clz = RingOfTheSerpent.class, method = "atTurnStart")
    public static class SerpentRing_ReplaceOriginalEffect {
        @SpirePrefixPatch
        public static SpireReturn patch(RingOfTheSerpent __instance) {
            __instance.beginLongPulse();
            return SpireReturn.Return(null);
        }
    }

    @SpirePatch(clz = RingOfTheSerpent.class, method = SpirePatch.CONSTRUCTOR)
    public static class SerpentRing_NewEffect {
        public static void Raw(CtBehavior ctMethodToPatch) throws NotFoundException, CannotCompileException {
            CtClass ctClass = ctMethodToPatch.getDeclaringClass();
            ClassPool pool = ctClass.getClassPool();
            CtClass ctAbstractRoom = pool.get(AbstractRoom.class.getName());

            CtMethod method = CtNewMethod.make(
                    CtClass.intType, // Return
                    "onPlayerGainedBlock", // Method name
                    new CtClass[]{CtClass.floatType}, //Paramters
                    null, // Exceptions
                    "{" +
                            "if($1 > 0 && pulse) { " +
                            "this.flash();" +
                            "addToBot(new com.megacrit.cardcrawl.actions.common.GainBlockAction(com.megacrit.cardcrawl.dungeons.AbstractDungeon.player, com.megacrit.cardcrawl.dungeons.AbstractDungeon.player, 3));" +
                            "com.megacrit.cardcrawl.dungeons.AbstractDungeon.actionManager.addToBottom(new com.megacrit.cardcrawl.actions.common.DrawCardAction(1));" +
                            "this.stopPulse();" +
                            "}" +
                            "return com.badlogic.gdx.math.MathUtils.floor($1);" +
                            "}",
                    ctClass
            );
            ctClass.addMethod(method);
        }
    }

}
