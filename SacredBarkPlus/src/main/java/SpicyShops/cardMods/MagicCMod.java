package SpicyShops.cardMods;

import SpicyShops.SpicyShops;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.cards.AbstractCard;

public class MagicCMod extends AbstractSpicySaleCMod {
    public static final String ID = SpicyShops.getModID() + "Magic";

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public void onInitialApplication(AbstractCard c) {
        AbstractCard checkCard = c.makeCopy();
        checkCard.upgrade();

        boolean invertMagic = c.baseMagicNumber > checkCard.baseMagicNumber;

        float downmult = 0.5f;
        int prevnum = (invertMagic ? MathUtils.floor((float) c.baseMagicNumber * downmult) : MathUtils.ceilPositive(((float) c.baseMagicNumber / downmult))) - c.baseMagicNumber;
        if (!invertMagic && prevnum <= 0) {
            prevnum = 1;
        } else if (invertMagic && prevnum >= 0) {
            prevnum = -1;
        }
        c.baseMagicNumber += prevnum;
        c.magicNumber = c.baseMagicNumber;
        c.upgradedMagicNumber = true;
    }

    @Override
    public float getPriceMod(AbstractCard c) {
        return 1.65f;
    }

    @Override
    public boolean isApplicable(AbstractCard c) {
        return c.baseMagicNumber > 0;
    }

    @Override
    public String getTexturePath() {
        return "magic";
    }
}
