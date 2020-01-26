package SacredBarkPlus.patches;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.ui.panels.TopPanel;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class Patch {
    @SpirePatch(clz = TopPanel.class, method = "renderHP")
    public static class ManaPanel {
        public static ExprEditor Instrument() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getClassName().equals(SpriteBatch.class.getName()) && m.getMethodName().equals("draw")) {
                        m.replace("{" +
                                "$proceed($1, $2, ICON_Y + 16f *"+ Settings.class.getName() +".scale, $4, $5, $6, $7, $8 *0.75f, $9*0.75f, $10, $11, $12, $13, $14, $15, $16);" +
                                "sb.setColor("+ Color.class.getName()+".ROYAL);" +
                                //"$proceed($1, $2, ICON_Y + 32.0F * "+ Settings.class.getName() +".scale, $4, $5, $6, $7, $8, $9*0.5f, $10, $11, $12, $13, $14, $15, $16);" +
                                "sb.draw("+ ImageMaster.class.getName() +".TP_HP, hpIconX - 32.0F + 32.0F * "+ Settings.class.getName() +".scale, ICON_Y - 48.0F + 32.0F * "+ Settings.class.getName() +".scale, 32.0F, 32.0F, 64.0F, 64.0F, "+ Settings.class.getName() +".scale*0.75f, "+ Settings.class.getName() +".scale *0.75f, 0.0F, 0, 0, 64, 64, false, false);" +
                                "sb.setColor("+ Color.class.getName()+".WHITE);" +
                                "}");
                    } else if(m.getMethodName().equals("renderFontLeftTopAligned")) {
                        m.replace("{" +
                                "$proceed($1, $2, $3, $4, INFO_TEXT_Y + 18f, $6);" +
                                FontHelper.class.getName() +".renderFontLeftTopAligned(sb, "+ FontHelper.class.getName()+".topPanelInfoFont, "+ AbstractDungeon.class.getName()+".player.currentHealth + \"/\" + "+ AbstractDungeon.class.getName()+".player.maxHealth, hpIconX + HP_NUM_OFFSET_X, INFO_TEXT_Y - 14f, "+ Color.class.getName()+".SKY);" +
                                "}");
                    }


                }
            };
    }
    }
}

