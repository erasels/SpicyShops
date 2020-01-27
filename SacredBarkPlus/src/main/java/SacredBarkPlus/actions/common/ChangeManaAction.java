package SacredBarkPlus.actions.common;

import SacredBarkPlus.util.ManaHelper;
import SacredBarkPlus.vfx.combat.GainManaEffect;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

public class ChangeManaAction extends AbstractGameAction {
    private boolean showEffect;

    public ChangeManaAction(int amt, boolean showEffect, float dur) {
        amount = amt;
        startDuration = duration = dur;
        this.showEffect = showEffect;
    }

    public ChangeManaAction(int amt, boolean showEffect) {
        this(amt, showEffect, Settings.ACTION_DUR_FAST);
    }

    public ChangeManaAction(int amt) {
        this(amt, (AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT), Settings.ACTION_DUR_FAST);
    }

    @Override
    public void update() {
        if(startDuration == duration) {
            if(amount > 0) {
                if(showEffect) {
                    AbstractPlayer p = AbstractDungeon.player;
                    AbstractDungeon.effectsQueue.add(new GainManaEffect(p.hb.cX - p.animX, p.hb.cY, amount));
                }
                ManaHelper.addMP(amount);
            } else {
                ManaHelper.loseMP(amount);
            }
        }
        tickDuration();
    }
}
