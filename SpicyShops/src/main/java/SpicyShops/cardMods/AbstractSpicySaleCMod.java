package SpicyShops.cardMods;

import basemod.abstracts.AbstractCardModifier;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;

public abstract class AbstractSpicySaleCMod extends AbstractCardModifier {
    public static String SPACE;

    public abstract float getPriceMod(AbstractCard c);

    public abstract boolean isApplicable(AbstractCard c);

    public String getTexturePath() {
        return "default";
    }

    @Override
    public AbstractCardModifier makeCopy() {
        try {
            return this.getClass().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void receivePostInit() {
        SPACE = Settings.lineBreakViaCharacter? " " : "";
    }
}
