package SpicyShops.cardMods;

import SpicyShops.SpicyShops;
import SpicyShops.util.HelperClass;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.CommonKeywordIconsField;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import com.megacrit.cardcrawl.localization.LocalizedStrings;

public class InnateCMod extends AbstractSpicySaleCMod{
    public static final String ID = SpicyShops.getModID()+"Innate";

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        card.isInnate = true;
    }

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        if(!CommonKeywordIconsField.useIcons.get(card)) {
            return HelperClass.capitalize(GameDictionary.INNATE.NAMES[0]) + LocalizedStrings.PERIOD + " NL " + rawDescription;
        }
        return rawDescription;
    }

    @Override
    public float getPriceMod(AbstractCard c) {
        return 1.5f;
    }

    @Override
    public boolean isApplicable(AbstractCard c) {
        boolean check = !c.isInnate;
        if(check && !c.upgraded) {
            AbstractCard checkCard = c.makeCopy();
            checkCard.upgrade();
            check = !checkCard.isInnate;
        }
        return check;
    }

    @Override
    public String getTexturePath() {
        return "innate";
    }
}
