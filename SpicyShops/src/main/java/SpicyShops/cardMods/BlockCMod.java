package SpicyShops.cardMods;

import SpicyShops.SpicyShops;
import com.megacrit.cardcrawl.cards.AbstractCard;
import org.apache.commons.lang3.math.NumberUtils;

public class BlockCMod extends AbstractSpicySaleCMod{
    public static final String ID = SpicyShops.getModID()+"Block";

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        card.baseBlock += 2 * NumberUtils.max(card.cost, 1);
    }

    @Override
    public float getPriceMod(AbstractCard c) {
        return 1.35f;
    }

    @Override
    public boolean isApplicable(AbstractCard c) {
        return c.baseBlock > -1;
    }

    @Override
    public String getTexturePath() {
        return "block";
    }
}
