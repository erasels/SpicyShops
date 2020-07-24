package SpicyShops.cardMods;

import SpicyShops.SpicyShops;
import SpicyShops.util.HelperClass;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.localization.LocalizedStrings;

public class ReshuffleCMod extends AbstractSpicySaleCMod{
    public static final String ID = SpicyShops.getModID()+"Reshuffle";

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        card.shuffleBackIntoDrawPile = true;
    }

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        String tmp = SpicyShops.modKeywords.get("Reshuffle").NAMES[0];
        return rawDescription + " NL " + HelperClass.capitalize(tmp, tmp.substring(tmp.indexOf(":") + 1)) + LocalizedStrings.PERIOD;
    }

    @Override
    public float getPriceMod(AbstractCard c) {
        return 1.3f;
    }

    @Override
    public boolean isApplicable(AbstractCard c) {
        return !(c.exhaust || c.type == AbstractCard.CardType.POWER) && c.cost != -2;
    }

    @Override
    public String getTexturePath() {
        return "reshuffle";
    }
}
