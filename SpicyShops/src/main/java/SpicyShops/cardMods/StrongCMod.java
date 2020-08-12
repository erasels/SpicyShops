package SpicyShops.cardMods;

import SpicyShops.SpicyShops;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.Blizzard;
import com.megacrit.cardcrawl.cards.colorless.MindBlast;
import com.megacrit.cardcrawl.cards.purple.Halt;
import com.megacrit.cardcrawl.cards.red.BodySlam;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class StrongCMod extends AbstractSpicySaleCMod{
    private static ArrayList<String> excluded = new ArrayList<>(Arrays.asList(Blizzard.ID, MindBlast.ID, BodySlam.ID));
    public static final String ID = SpicyShops.getModID()+"Strong";

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        card.baseDamage += 3 * NumberUtils.max(card.cost, 1);
    }

    @Override
    public float getPriceMod(AbstractCard c) {
        return 1.5f;
    }

    @Override
    public boolean isApplicable(AbstractCard c) {
        return c.baseDamage > -1 && excluded.stream().noneMatch(str -> str.equals(c.cardID));
    }

    @Override
    public String getTexturePath() {
        return "strong";
    }
}
