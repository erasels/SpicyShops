package SacredBarkPlus;

import basemod.BaseMod;
import basemod.interfaces.*;


import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;

@SpireInitializer
public class BetterSilentStart implements
        PostInitializeSubscriber {
    public static void initialize() {
        BaseMod.subscribe(new BetterSilentStart());
    }

    @Override
    public void receivePostInitialize() {

    }

}