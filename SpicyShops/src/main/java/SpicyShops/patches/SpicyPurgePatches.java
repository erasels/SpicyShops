package SpicyShops.patches;

import SpicyShops.SpicyShops;
import SpicyShops.util.TextureLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.shop.ShopScreen;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

import java.util.ArrayList;

public class SpicyPurgePatches {
    public static boolean additionalPurge = false;
    private static final float DOUBLE_PURGE_CHANCE = 0.33f;

    @SpirePatch(clz = ShopScreen.class, method = "init")
    public static class ResetPurgeAvailability {
        @SpirePostfixPatch
        public static void patch(ShopScreen __instance, ArrayList<AbstractCard> coloredCards, ArrayList<AbstractCard> colorlessCards) {
            if(__instance.purgeAvailable) {
                additionalPurge = AbstractDungeon.merchantRng.randomBoolean(DOUBLE_PURGE_CHANCE);
            }
        }
    }

    @SpirePatch(clz = ShopScreen.class, method = "updatePurge")
    public static class ResetPurge {
        @SpireInstrumentPatch
        public static ExprEditor insertAfterAlternative() {
            return new ExprEditor() {
                @Override
                public void edit(FieldAccess m) throws CannotCompileException {
                    if (m.getClassName().equals(ShopScreen.class.getName()) && m.getFieldName().equals("purgeAvailable")) {
                            m.replace("{" +
                                    "$_ = $proceed($$);" +
                                    "if(" + SpicyPurgePatches.class.getName() + ".additionalPurge) {" +
                                    "purgeAvailable = true;" +
                                     SpicyPurgePatches.class.getName() + ".additionalPurge = false;" +
                                    "}" +
                                    "}");
                    }
                }
            };
        }
    }

    @SpirePatch(clz = ShopScreen.class, method = "renderPurge")
    public static class RenderAdditionalPurge {
        public static final Texture addPurgeText = TextureLoader.getTexture(SpicyShops.makeUIPath("AdditionalPurge.png"));

        @SpirePostfixPatch
        public static void patch(ShopScreen __instance, SpriteBatch sb, float ___purgeCardX, float ___purgeCardY, float ___purgeCardScale) {
            if(additionalPurge) {
                sb.setColor(Color.WHITE);
                sb.draw(addPurgeText,
                        ___purgeCardX + (40f * Settings.scale),
                        ___purgeCardY + (40f * Settings.scale),
                        addPurgeText.getWidth() * ___purgeCardScale,
                        addPurgeText.getHeight() * ___purgeCardScale
                );
                sb.setBlendFunction(770, 1);
                sb.setColor(new Color(1.0F, 1.0F, 1.0F, (MathUtils.cosDeg((float) (System.currentTimeMillis() / 5L % 360L)) + 1.25F) / 3.0F));
                sb.draw(addPurgeText,
                        ___purgeCardX + (40f * Settings.scale),
                        ___purgeCardY + (40f * Settings.scale),
                        addPurgeText.getWidth() *  ___purgeCardScale,
                        addPurgeText.getHeight() * ___purgeCardScale
                );
                sb.setBlendFunction(770, 771);
                sb.setColor(Color.WHITE);
            }
        }
    }
}
