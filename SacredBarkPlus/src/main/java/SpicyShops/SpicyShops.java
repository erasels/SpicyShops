package SpicyShops;

import basemod.BaseMod;
import basemod.interfaces.*;


import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;

@SpireInitializer
public class SpicyShops implements
        PostInitializeSubscriber,
        EditStringsSubscriber{
    public static void initialize() {
        BaseMod.subscribe(new SpicyShops());
    }

    @Override
    public void receivePostInitialize() {

    }

    @Override
    public void receiveEditStrings() {

    }

    public static String makeUIPath(String resourcePath) {
        return getModID() + "Resources/img/ui/" + resourcePath;
    }

    public static String getModID() {
        return "spicyShop";
    }
}