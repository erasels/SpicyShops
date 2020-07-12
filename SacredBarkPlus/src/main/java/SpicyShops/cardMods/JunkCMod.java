package SpicyShops.cardMods;

import SpicyShops.SpicyShops;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class JunkCMod extends AbstractSpicySaleCMod{
    public static final String ID = SpicyShops.getModID()+"Junk";

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
    }

    @Override
    public float getPriceMod(AbstractCard c) {
        return 0.1f;
    }

    @Override
    public boolean isApplicable(AbstractCard c) {
        AbstractCard checkCard = c.makeCopy();
        checkCard.upgrade();
        boolean costChanged = c.cost != checkCard.cost;
        return c.rarity != AbstractCard.CardRarity.COMMON && c.price > AbstractDungeon.player.gold && !costChanged;
    }

    @Override
    public String getTexturePath() {
        return "junk";
    }
}
