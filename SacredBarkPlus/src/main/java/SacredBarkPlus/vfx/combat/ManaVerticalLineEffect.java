package SacredBarkPlus.vfx.combat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.vfx.combat.HealVerticalLineEffect;

public class ManaVerticalLineEffect extends HealVerticalLineEffect {
    public ManaVerticalLineEffect(float x, float y) {
        super(x, y);
        if (MathUtils.randomBoolean()) {
            this.color = Color.SKY.cpy();
        } else {
            this.color = Color.ROYAL.cpy();
        }
    }
}
