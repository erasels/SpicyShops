package SpicyShops;

import SpicyShops.cardMods.AbstractSpicySaleCMod;
import SpicyShops.patches.SpicyPotionPatches;
import SpicyShops.patches.SpicyRelicPatches;
import SpicyShops.util.TextureLoader;
import basemod.AutoAdd;
import basemod.BaseMod;
import basemod.abstracts.CustomSavable;
import basemod.interfaces.*;
import basemod.patches.whatmod.WhatMod;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.mod.hubris.patches.potions.AbstractPotion.PotionUseCountField;
import com.evacipated.cardcrawl.mod.hubris.relics.EmptyBottle;
import com.evacipated.cardcrawl.mod.stslib.Keyword;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.Gson;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@SpireInitializer
public class SpicyShops implements
        PostInitializeSubscriber,
        EditStringsSubscriber,
        PostUpdateSubscriber,
        RelicGetSubscriber,
        EditKeywordsSubscriber {
    public static ArrayList<AbstractSpicySaleCMod> cardMods = new ArrayList<>();
    public static HashMap<String, Texture> tagTextures = new HashMap<>();
    public static ArrayList<String> vanillaCurses = new ArrayList<>();
    public static HashMap<String, Keyword> modKeywords = new HashMap<>();
    public static final boolean hasHubris;

    static {
        hasHubris = Loader.isModLoaded("hubris");
    }

    public static final Logger logger = LogManager.getLogger(SpicyShops.class.getName());

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

        BaseMod.addSaveField("SSPotionSacrifice", new CustomSavable<Integer>() {
            @Override
            public Integer onSave() {
                return Math.toIntExact(AbstractDungeon.player.relics.stream().filter(p -> SpicyRelicPatches.SpicyRelicFields.modifier.get(p) != SpicyRelicPatches.Modifiers.NONE).count());
            }

            @Override
            public void onLoad(Integer i) {
                AbstractDungeon.player.potionSlots = NumberUtils.max(0, AbstractDungeon.player.potionSlots - i);
            }
        });

        BaseMod.addSaveField("SSBigPotion", new CustomSavable<List<Integer>>() {
            @Override
            public List<Integer> onSave() {
                return AbstractDungeon.player.potions.stream().map(p -> {
                    if (SpicyPotionPatches.PotionUseField.isBig.get(p)) {
                        return SpicyPotionPatches.PotionUseField.useCount.get(p);
                    }
                    return 0;
                }).collect(Collectors.toCollection(ArrayList::new));
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

        for (AbstractCard c : CardLibrary.getCardList(CardLibrary.LibraryType.CURSE)) {
            //Remove modded curses
            if (WhatMod.findModID(c.getClass()) == null) {
                vanillaCurses.add(c.cardID);
            }
        }
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

    @Override
    public void receiveEditKeywords() {
        Gson gson = new Gson();
        String json = Gdx.files.internal(getModID() + "Resources/loc/eng/keywordStrings.json").readString(String.valueOf(StandardCharsets.UTF_8));
        com.evacipated.cardcrawl.mod.stslib.Keyword[] keywords = gson.fromJson(json, com.evacipated.cardcrawl.mod.stslib.Keyword[].class);

        if (keywords != null) {
            for (Keyword keyword : keywords) {
                BaseMod.addKeyword(getModID().toLowerCase(), keyword.PROPER_NAME, keyword.NAMES, keyword.DESCRIPTION);
                modKeywords.put(keyword.PROPER_NAME, keyword);
            }
        }
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