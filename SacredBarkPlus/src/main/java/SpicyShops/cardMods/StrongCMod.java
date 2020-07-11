package SpicyShops.cardMods;

import SpicyShops.SpicyShops;
import com.megacrit.cardcrawl.cards.AbstractCard;
import org.apache.commons.lang3.math.NumberUtils;

public class StrongCMod extends AbstractSpicySaleCMod{
    public static final String ID = SpicyShops.getModID()+"Strong";

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        card.baseDamage += 3 * NumberUtils.max(card.cost, 0);
    }

    @Override
    public float getPriceMod(AbstractCard c) {
        return 1.5f;
    }

    @Override
    public boolean isApplicable(AbstractCard c) {
        return c.baseDamage > -1;
    }

    @Override
    public String getTexturePath() {
        return "strong";
    }
}
