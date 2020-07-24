package SpicyShops.cardMods;

import SpicyShops.SpicyShops;
import SpicyShops.patches.cards.RepeatKeywordPatches;
import SpicyShops.util.HelperClass;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.CommonKeywordIconsField;
import com.evacipated.cardcrawl.mod.stslib.patches.CommonKeywordIconsPatches;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import com.megacrit.cardcrawl.localization.LocalizedStrings;

public class RepeatCMod extends AbstractSpicySaleCMod{
    public static final String ID = SpicyShops.getModID()+"Repeat";
    public static String localizedPurgeName = CommonKeywordIconsPatches.purgeName;

    private boolean removedExhaust = false;

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        RepeatKeywordPatches.RepeatField.repeat.set(card, true);
        if(card.exhaust) {
            card.exhaust = false;
            removedExhaust = true;
        }

        if(card.type != AbstractCard.CardType.POWER) {
            card.purgeOnUse = true;
        }
    }

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        String tmp = rawDescription;
        if(!CommonKeywordIconsField.useIcons.get(card) && card.type != AbstractCard.CardType.POWER) {
            if(removedExhaust) {
                tmp = rawDescription.replace(HelperClass.capitalize(GameDictionary.EXHAUST.NAMES[0]), HelperClass.capitalize(localizedPurgeName));
            } else {
                tmp = rawDescription + " NL " + HelperClass.capitalize(localizedPurgeName) + LocalizedStrings.PERIOD;
            }
        }
        String rK = SpicyShops.modKeywords.get("Repeat").NAMES[0];
        return tmp + " NL " + HelperClass.capitalize(rK, rK.substring(rK.indexOf(":") + 1)) + LocalizedStrings.PERIOD;
    }

    @Override
    public float getPriceMod(AbstractCard c) {
        return 2f + (removedExhaust || c.type == AbstractCard.CardType.POWER?0.5f:0);
    }

    @Override
    public boolean isApplicable(AbstractCard c) {
        return c.cost != -2;
    }

    @Override
    public String getTexturePath() {
        return "repeat";
    }
}
