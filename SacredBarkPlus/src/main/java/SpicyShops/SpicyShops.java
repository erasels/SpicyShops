package SpicyShops;

import SpicyShops.cardMods.AbstractSpicySaleCMod;
import SpicyShops.util.TextureLoader;
import basemod.AutoAdd;
import basemod.BaseMod;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;

import java.util.ArrayList;
import java.util.HashMap;

@SpireInitializer
public class SpicyShops implements
        PostInitializeSubscriber,
        EditStringsSubscriber{
    public static ArrayList<AbstractSpicySaleCMod> cardMods = new ArrayList<>();
    public static HashMap<String, Texture> tagTextures = new HashMap<>();

    public static void initialize() {
        BaseMod.subscribe(new SpicyShops());
    }

    @Override
    public void receivePostInitialize() {
        new AutoAdd(getModID())
        .packageFilter("SpicyShops.cardMods")
        .any(AbstractSpicySaleCMod.class, (info, cmod) -> {
            cardMods.add(cmod);
            tagTextures.put(cmod.getTexturePath(), TextureLoader.getTexture(makeUIPath(cmod.getTexturePath())+ ".png"));
        });
    }

    @Override
    public void receiveEditStrings() {

    }

    public static String makeUIPath(String resourcePath) {
        return getModID() + "Resources/img/ui/" + resourcePath;
    }

    public static String getModID() {
        return "spicyShops";
    }
}