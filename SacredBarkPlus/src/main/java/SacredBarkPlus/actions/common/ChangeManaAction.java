package SacredBarkPlus.actions.common;

import SacredBarkPlus.util.ManaHelper;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.core.Settings;

public class ChangeManaAction extends AbstractGameAction {
    public ChangeManaAction(int amt, float dur) {
        amount = amt;
        startDuration = duration = dur;
    }

    public ChangeManaAction(int amt) {
        this(amt, Settings.ACTION_DUR_FAST);
    }

    @Override
    public void update() {
        if(startDuration == duration) {
            if(amount > 0) {
                ManaHelper.addMP(amount);
            } else {
                ManaHelper.loseMP(amount);
            }
        }
        tickDuration();
    }
}
