package SpicyShops.cardMods;

import SpicyShops.SpicyShops;
import SpicyShops.util.HelperClass;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.CommonKeywordIconsField;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import org.apache.commons.lang3.math.NumberUtils;

public class RetainCMod extends AbstractSpicySaleCMod{
    public static final String ID = SpicyShops.getModID()+"Retain";

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        card.selfRetain = true;
    }

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        if(!CommonKeywordIconsField.useIcons.get(card)) {
            if (card.isInnate) {
                int nlIndex = NumberUtils.max(rawDescription.indexOf("NL"), 0);
                //Is innate in the bottom half of the card? Then don't care about trying to slot retain under it
                if(rawDescription.indexOf(GameDictionary.INNATE.NAMES[0]) > (rawDescription.length()/2f) || nlIndex == 0) {
                    return HelperClass.capitalize(GameDictionary.RETAIN.NAMES[0]) + SPACE + LocalizedStrings.PERIOD + " NL " + rawDescription;
                } else {
                    String beforeNL = rawDescription.substring(0, nlIndex);
                    return beforeNL + "NL " + HelperClass.capitalize(GameDictionary.RETAIN.NAMES[0]) + SPACE + LocalizedStrings.PERIOD + " " + rawDescription.substring(nlIndex);
                }
            } else {
                return HelperClass.capitalize(GameDictionary.RETAIN.NAMES[0]) + SPACE + LocalizedStrings.PERIOD + " NL " + rawDescription;
            }
        }
        return rawDescription;
    }

    @Override
    public float getPriceMod(AbstractCard c) {
        return 1.4f;
    }

    @Override
    public boolean isApplicable(AbstractCard c) {
        boolean check = !(c.retain || c.selfRetain || c.isEthereal) && c.cost != -2;
        if(check && !c.upgraded) {
            AbstractCard checkCard = c.makeCopy();
            checkCard.upgrade();
            check = !(c.retain || c.selfRetain || c.isEthereal) && c.cost != -2;
        }
        return check;
    }

    @Override
    public String getTexturePath() {
        return "retain";
    }
}
