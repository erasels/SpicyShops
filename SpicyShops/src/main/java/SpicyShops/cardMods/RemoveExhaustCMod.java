package SpicyShops.cardMods;

import SpicyShops.SpicyShops;
import SpicyShops.util.HelperClass;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.CommonKeywordIconsField;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import com.megacrit.cardcrawl.localization.LocalizedStrings;

public class RemoveExhaustCMod extends AbstractSpicySaleCMod{
    public static final String ID = SpicyShops.getModID()+"RemoveExhaust";

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        card.exhaust = false;
    }

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        if(!CommonKeywordIconsField.useIcons.get(card)) {
            return rawDescription.replace("NL " + HelperClass.capitalize(GameDictionary.EXHAUST.NAMES[0]) + LocalizedStrings.PERIOD, "");
        }
        return rawDescription;
    }

    @Override
    public float getPriceMod(AbstractCard c) {
        return 2.5f + (c.rarity == AbstractCard.CardRarity.RARE?0.5f:0);
    }

    @Override
    public boolean isApplicable(AbstractCard c) {
        return c.exhaust && c.cost < AbstractDungeon.player.gold;
    }

    @Override
    public String getTexturePath() {
        return "antiExhaust";
    }
}
