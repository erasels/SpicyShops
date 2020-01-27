package SacredBarkPlus.vfx.combat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.combat.HealNumberEffect;

import java.lang.reflect.Field;

public class GainManaEffect extends AbstractGameEffect {
    private static final float X_JITTER = 120.0F * Settings.scale;
    private static final float Y_JITTER = 120.0F * Settings.scale;
    private static final float OFFSET_Y = -50.0F * Settings.scale;
    private static final float PITCH = 0.5F;

    public GainManaEffect(float x, float y, int amount) {
        int roll = MathUtils.random(0, 2);
        if (roll == 0) {
            CardCrawlGame.sound.playA("HEAL_1", PITCH);
        } else if (roll == 1) {
            CardCrawlGame.sound.playA("HEAL_2",PITCH);
        } else {
            CardCrawlGame.sound.playA("HEAL_3", PITCH);
        }
        HealNumberEffect tmpHN = new HealNumberEffect(x, y, amount);
        try {
            Field hnF = getField(tmpHN.getClass(), "color");
            hnF.setAccessible(true);
            hnF.set(tmpHN, Color.SKY.cpy());
        } catch (Exception e) {
            e.printStackTrace();
        }
        AbstractDungeon.effectsQueue.add(tmpHN);

        for (int i = 0; i < 18; i++)
            AbstractDungeon.effectsQueue.add(new ManaVerticalLineEffect(x +
                    MathUtils.random(-X_JITTER * 1.5F, X_JITTER * 1.5F), y + OFFSET_Y +
                    MathUtils.random(-Y_JITTER, Y_JITTER)));
    }

    public void update() {
        this.isDone = true;
    }

    public void render(SpriteBatch sb) {
    }

    public void dispose() {
    }

    private static Field getField(Class clazz, String fieldName)
            throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class superClass = clazz.getSuperclass();
            if (superClass == null) {
                throw e;
            } else {
                return getField(superClass, fieldName);
            }
        }
    }
}
