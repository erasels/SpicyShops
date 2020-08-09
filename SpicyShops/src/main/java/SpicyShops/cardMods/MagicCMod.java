package SpicyShops.cardMods;

import SpicyShops.SpicyShops;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.DoomAndGloom;
import com.megacrit.cardcrawl.cards.blue.ThunderStrike;
import com.megacrit.cardcrawl.cards.purple.DevaForm;
import com.megacrit.cardcrawl.cards.purple.Halt;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class MagicCMod extends AbstractSpicySaleCMod {
    private static ArrayList<String> excluded = new ArrayList<>(Arrays.asList(ThunderStrike.ID, DevaForm.ID, Halt.ID, DoomAndGloom.ID));
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
        return 2f;
    }

    @Override
    public boolean isApplicable(AbstractCard c) {
        return c.baseMagicNumber > 0 && StringUtils.containsIgnoreCase(c.rawDescription, "!M!") && excluded.stream().noneMatch(str -> str.equals(c.cardID));
    }

    @Override
    public String getTexturePath() {
        return "magic";
    }
}
