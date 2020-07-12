package SpicyShops;

import SpicyShops.cardMods.AbstractSpicySaleCMod;
import SpicyShops.patches.SpicyPotionPatches;
import SpicyShops.util.TextureLoader;
import basemod.AutoAdd;
import basemod.BaseMod;
import basemod.abstracts.CustomSavable;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.PostUpdateSubscriber;
import basemod.interfaces.RelicGetSubscriber;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.mod.hubris.patches.potions.AbstractPotion.PotionUseCountField;
import com.evacipated.cardcrawl.mod.hubris.relics.EmptyBottle;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@SpireInitializer
public class SpicyShops implements
        PostInitializeSubscriber,
        EditStringsSubscriber,
        PostUpdateSubscriber,
        RelicGetSubscriber {
    public static ArrayList<AbstractSpicySaleCMod> cardMods = new ArrayList<>();
    public static HashMap<String, Texture> tagTextures = new HashMap<>();
    public static final boolean hasHubris;

    static {
        hasHubris = Loader.isModLoaded("hubris");
    }

    public static void initialize() {
        BaseMod.subscribe(new SpicyShops());
    }

    @Override
    public void receivePostInitialize() {
        new AutoAdd(getModID())
                .packageFilter("SpicyShops.cardMods")
                .any(AbstractSpicySaleCMod.class, (info, cmod) -> {
                    cardMods.add(cmod);
                    tagTextures.put(cmod.getTexturePath(), TextureLoader.getTexture(makeUIPath(cmod.getTexturePath()) + ".png"));
                });

        BaseMod.addSaveField("SSBigPotion", new CustomSavable<List<Integer>>() {
            @Override
            public List<Integer> onSave() {
                //Sets the useCount to a negative if the potion shouldn't be affected
                return AbstractDungeon.player.potions.stream().map(p -> SpicyPotionPatches.PotionUseField.useCount.get(p) * (SpicyPotionPatches.PotionUseField.isBig.get(p)?1:-1)).collect(Collectors.toCollection(ArrayList::new));
            }

            @Override
            public void onLoad(List<Integer> l) {
                int c = 0;
                if (l != null && !l.isEmpty()) {
                    for (AbstractPotion p : AbstractDungeon.player.potions) {
                        SpicyPotionPatches.PotionUseField.useCount.set(p, l.get(NumberUtils.min(c, AbstractDungeon.player.potions.size() - 1)));
                        SpicyPotionPatches.PotionUseField.isBig.set(p, l.get(NumberUtils.min(c++, AbstractDungeon.player.potions.size() - 1)) > 0);
                    }
                }
            }
        });

        BaseMod.addSaveField("SSConcPotion", new CustomSavable<List<Boolean>>() {
            @Override
            public List<Boolean> onSave() {
                return AbstractDungeon.player.potions.stream().map(p -> SpicyPotionPatches.PotionUseField.isConcentrated.get(p)).collect(Collectors.toCollection(ArrayList::new));
            }

            @Override
            public void onLoad(List<Boolean> l) {
                int c = 0;
                if (l != null && !l.isEmpty()) {
                    for (AbstractPotion p : AbstractDungeon.player.potions) {
                        SpicyPotionPatches.PotionUseField.isConcentrated.set(p, l.get(NumberUtils.min(c++, AbstractDungeon.player.potions.size() - 1)));
                        p.initializeData();
                    }
                }
            }
        });
    }

    @Override
    public void receivePostUpdate() {
        if (SpicyPotionPatches.potion != null) {
            AbstractDungeon.player.obtainPotion(SpicyPotionPatches.potion.slot, SpicyPotionPatches.potion);
            SpicyPotionPatches.potion = null;
        }
    }

    @Override
    public void receiveRelicGet(AbstractRelic r) {
        if (!CardCrawlGame.loadingSave) {
            if (hasHubris && r.relicId.equals(EmptyBottle.ID)) {
                AbstractDungeon.player.potions.stream()
                        .filter(p -> SpicyPotionPatches.PotionUseField.isBig.get(p) && SpicyPotionPatches.PotionUseField.useCount.get(p) > 1)
                        .forEach(p -> PotionUseCountField.useCount.set(p, PotionUseCountField.useCount.get(p) + 1));
            }
        }
    }

    @Override
    public void receiveEditStrings() {
        BaseMod.loadCustomStringsFile(UIStrings.class, getModID() + "Resources/loc/eng/uiStrings.json");
    }

    public static String makeUIPath(String resourcePath) {
        return getModID() + "Resources/img/ui/" + resourcePath;
    }

    public static String getModID() {
        return "spicyShops";
    }

    public static String makeID(String input) {
        return getModID() + ":" + input;
    }
}