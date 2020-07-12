package SpicyShops.vfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;

public class ConcentratedPotionEffect extends AbstractGameEffect {
    private float x;
    private float y;
    private float oX;
    private float oY;
    private float vX;
    private float vY;
    private float dur_div2;
    private Hitbox hb;
    private TextureAtlas.AtlasRegion img;

    public ConcentratedPotionEffect(float x, float y) {
        this(null);
        this.x = x;
        this.y = y;
    }

    public ConcentratedPotionEffect(Hitbox hb) {
        this.hb = hb;
        img = ImageMaster.GLOW_SPARK_2;
        duration = MathUtils.random(0.6F, 1F);
        scale = MathUtils.random(0.4F, 0.8F) * Settings.scale;
        dur_div2 = duration / 2.0F;
        color = new Color(0.2F, MathUtils.random(0.8F, 1.0F), 0.3f, 0.0F);
        oX = MathUtils.random(-25.0F, 25.0F) * Settings.scale;
        oY = MathUtils.random(-25.0F, 25.0F) * Settings.scale;
        oX -= img.packedWidth / 2.0F;
        oY -= img.packedHeight / 2.0F;
        vX = MathUtils.random(-5.0F, 5.0F) * Settings.scale;
        vY = MathUtils.random(-7.0F, 7.0F) * Settings.scale;
        renderBehind = MathUtils.randomBoolean(0.2F + this.scale - 0.5F);
        rotation = MathUtils.random(-8.0F, 8.0F);
    }

    public void update() {
        if (duration > dur_div2) {
            color.a = Interpolation.pow3In.apply(0.5F, 0.0F, (duration - dur_div2) / dur_div2);
        } else {
            color.a = Interpolation.pow3In.apply(0.0F, 0.5F, duration / dur_div2);
        }
        oX += vX * Gdx.graphics.getDeltaTime();
        oY += vY * Gdx.graphics.getDeltaTime();
        duration -= Gdx.graphics.getDeltaTime();
        if (duration < 0.0F)
            isDone = true;
    }

    public void render(SpriteBatch sb) {
        sb.setColor(color);
        sb.setBlendFunction(770, 1);
        if (hb != null) {
            sb.draw(img, hb.cX + oX, hb.cY + oY,
                    img.packedWidth / 2.0F,
                    img.packedHeight / 2.0F, img.packedWidth, img.packedHeight,
                    scale * MathUtils.random(0.8F, 1.2F),
                    scale * MathUtils.random(0.8F, 1.2F), rotation);
        } else {
            sb.draw(img, x + oX, y + oY,
                    img.packedWidth / 2.0F,
                    img.packedHeight / 2.0F, img.packedWidth, img.packedHeight,
                    scale * MathUtils.random(0.8F, 1.2F),
                    scale * MathUtils.random(0.8F, 1.2F), rotation);
        }
        sb.setBlendFunction(770, 771);
    }

    public void dispose() {
    }
}

