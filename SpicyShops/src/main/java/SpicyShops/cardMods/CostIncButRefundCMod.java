package SpicyShops.cardMods;

import SpicyShops.SpicyShops;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.RefundFields;
import com.evacipated.cardcrawl.mod.stslib.variables.RefundVariable;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import org.apache.commons.lang3.math.NumberUtils;

public class CostIncButRefundCMod extends AbstractSpicySaleCMod {
    public static final String ID = SpicyShops.getModID()+"CostIncButRefund";
    public static String localizedRefundName;

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        card.cost = card.cost + 1;
        card.costForTurn = card.cost;
        card.isCostModified = true;
        card.upgradedCost = true;
        //RefundFields.baseRefund.set(card, );
        RefundVariable.setBaseValue(card, NumberUtils.max(RefundFields.baseRefund.get(card), 0) + 2);
    }

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return rawDescription + " NL " + localizedRefundName + " !stslib:refund!" + SPACE + LocalizedStrings.PERIOD;
    }

    @Override
    public float getPriceMod(AbstractCard c) {
        return 1.55f;
    }

    @Override
    public boolean isApplicable(AbstractCard c) {
        AbstractCard checkCard = c.makeCopy();
        checkCard.upgrade();
        boolean costChanged = c.cost != checkCard.cost;
        return !costChanged && c.cost > 0 && (AbstractDungeon.actNum > 1 || c.cost < 3);
    }

    @Override
    public String getTexturePath() {
        return "refund";
    }
}
