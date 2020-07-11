package SpicyShops.cardMods;

import SpicyShops.SpicyShops;
import SpicyShops.util.HelperClass;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.CommonKeywordIconsField;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import com.megacrit.cardcrawl.localization.LocalizedStrings;

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
        if(card.isInnate && !CommonKeywordIconsField.useIcons.get(card)) {
            String beforeNL = rawDescription.substring(0, rawDescription.indexOf("NL"));
            return beforeNL + "NL " + HelperClass.capitalize(GameDictionary.RETAIN.NAMES[0]) + LocalizedStrings.PERIOD + " " + rawDescription.substring(rawDescription.indexOf("NL") + 1);
        } else {
            return HelperClass.capitalize(GameDictionary.RETAIN.NAMES[0]) + LocalizedStrings.PERIOD + " NL " + rawDescription;
        }
    }

    @Override
    public float getPriceMod(AbstractCard c) {
        return 1.5f;
    }

    @Override
    public boolean isApplicable(AbstractCard c) {
        return !(c.retain && c.selfRetain);
    }

    @Override
    public String getTexturePath() {
        return "retain";
    }
}
