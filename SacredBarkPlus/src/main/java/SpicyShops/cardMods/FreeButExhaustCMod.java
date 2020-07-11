package SpicyShops.cardMods;

import SpicyShops.SpicyShops;
import SpicyShops.util.HelperClass;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.CommonKeywordIconsField;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import com.megacrit.cardcrawl.localization.LocalizedStrings;

public class FreeButExhaustCMod extends AbstractSpicySaleCMod {
    public static final String ID = SpicyShops.getModID()+"FreeButExhaust";

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        card.exhaust = true;
    }

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return rawDescription + (!CommonKeywordIconsField.useIcons.get(card)?" NL " + HelperClass.capitalize(GameDictionary.EXHAUST.NAMES[0] + LocalizedStrings.PERIOD):"");
    }

    @Override
    public float getPriceMod(AbstractCard c) {
        return 1.1f;
    }

    @Override
    public boolean isApplicable(AbstractCard c) {
        return c.type != AbstractCard.CardType.POWER && c.cost > 1 && !c.exhaust;
    }
}
