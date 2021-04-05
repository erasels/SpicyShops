package SpicyShops;

import SpicyShops.cardMods.AbstractSpicySaleCMod;
import SpicyShops.patches.SpicyPotionPatches;
import SpicyShops.util.HelperClass;
import SpicyShops.util.TextureLoader;
import basemod.*;
import basemod.abstracts.CustomSavable;
import basemod.interfaces.*;
import basemod.patches.whatmod.WhatMod;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.mod.hubris.patches.potions.AbstractPotion.PotionUseCountField;
import com.evacipated.cardcrawl.mod.hubris.relics.EmptyBottle;
import com.evacipated.cardcrawl.mod.stslib.Keyword;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.Gson;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.curses.AscendersBane;
import com.megacrit.cardcrawl.cards.curses.CurseOfTheBell;
import com.megacrit.cardcrawl.cards.curses.Necronomicurse;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@SpireInitializer
public class SpicyShops implements
        PostInitializeSubscriber,
        EditStringsSubscriber,
        PostUpdateSubscriber,
        RelicGetSubscriber,
        EditKeywordsSubscriber {
    private static SpireConfig modConfig = null;
    public static ArrayList<AbstractSpicySaleCMod> cardMods = new ArrayList<>();
    public static HashMap<String, Boolean> enabledCMods = new HashMap<>();
    public static HashMap<String, Texture> tagTextures = new HashMap<>();
    public static ArrayList<String> vanillaCurses = new ArrayList<>();
    public static HashMap<String, Keyword> modKeywords = new HashMap<>();
    public static final boolean hasHubris;
    public static final boolean hasReplay;

    static {
        hasHubris = Loader.isModLoaded("hubris");
        hasReplay = Loader.isModLoaded("ReplayTheSpireMod");
    }

    public static final Logger logger = LogManager.getLogger(SpicyShops.class.getName());

    public static void initialize() {
        BaseMod.subscribe(new SpicyShops());
    }

    private ModPanel settingsPanel;
    private float xPos = 350f, yPos = 750f;

    @Override
    public void receivePostInitialize() {
        settingsPanel = new ModPanel();

        new AutoAdd(getModID())
                .packageFilter("SpicyShops.cardMods")
                .any(AbstractSpicySaleCMod.class, (info, cmod) -> {
                    cardMods.add(cmod);
                    enabledCMods.put(cmod.id(), true);
                    tagTextures.put(cmod.getTexturePath(), TextureLoader.getTexture(makeUIPath(cmod.getTexturePath()) + ".png"));
                });

        //Populate defaults, load saved values and overwrite hashmap with saved values
        try {
            Properties defaults = new Properties();
            for (Map.Entry<String, Boolean> e : enabledCMods.entrySet()) {
                defaults.put(e.getKey(), Boolean.toString(true));
            }
            modConfig = new SpireConfig("MintySpire", "Config", defaults);
            for (Map.Entry<String, Boolean> e : enabledCMods.entrySet()) {
                e.setValue(modConfig.getBool(e.getKey()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(makeID("Config"));
        //UI Strings is cached and new changes don't get loaded for some reason
        Map<String, String> TEXT = new HashMap<>();
        if (uiStrings != null) {
            TEXT = uiStrings.TEXT_DICT;
        } else {
            logger.info("uiStrings were cached and had to be hardcoded. No localization for the config menu.");
            TEXT.put("CMODS", "Allow the following card modifiers to spawn in shops:");
        }

        settingsPanel.addUIElement(new ModLabel(TEXT.get("CMODS"), xPos + 5f, yPos, Settings.CREAM_COLOR, settingsPanel, click -> {
        }));
        yPos -= 50f;

        //Create buttons for the CardMods
        for (Map.Entry<String, Boolean> e : enabledCMods.entrySet()) {
            String name = e.getKey();
            name = name.replace(SpicyShops.getModID(), "");
            settingsPanel.addUIElement(new ModLabeledToggleButton(name, xPos, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, modConfig.getBool(e.getKey()), settingsPanel, l -> {
                    },
                            button ->
                            {
                                if (modConfig != null) {
                                    modConfig.setBool(e.getKey(), button.enabled);
                                    e.setValue(button.enabled);
                                    saveConfig();
                                }
                            })
            );

            yPos -= 50f;
        }

        AbstractSpicySaleCMod.receivePostInit();

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
            if (WhatMod.findModID(c.getClass()) == null && !(c instanceof AscendersBane) && !(c instanceof CurseOfTheBell) && !(c instanceof Necronomicurse)) {
                vanillaCurses.add(c.cardID);
            }
        }

        BaseMod.registerModBadge(TextureLoader.getTexture(makeImgPath("modBadge.png")), "Spicy Shops", "erasels", "A mod, boyo.", settingsPanel);
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
        BaseMod.loadCustomStringsFile(UIStrings.class, getModID() + "Resources/loc/" + locPath() + "/uiStrings.json");
    }

    @Override
    public void receiveEditKeywords() {
        Gson gson = new Gson();
        String json = Gdx.files.internal(getModID() + "Resources/loc/" + locPath() + "/keywordStrings.json").readString(String.valueOf(StandardCharsets.UTF_8));
        com.evacipated.cardcrawl.mod.stslib.Keyword[] keywords = gson.fromJson(json, com.evacipated.cardcrawl.mod.stslib.Keyword[].class);

        if (keywords != null) {
            for (Keyword keyword : keywords) {
                BaseMod.addKeyword(getModID().toLowerCase(), keyword.PROPER_NAME, keyword.NAMES, keyword.DESCRIPTION);
                modKeywords.put((keyword.NAMES.length > 1 ? keyword.NAMES[1] : keyword.NAMES[0]), keyword);
            }
        }
    }

    private static String locPath() {
        if (Settings.language == Settings.GameLanguage.ZHS) {
            return "zhs";
        } else if (Settings.language == Settings.GameLanguage.FRA) {
            return "fra";
        } else {
            return "eng";
        }
    }

    public static String makeImgPath(String resourcePath) {
        return getModID() + "Resources/img/" + resourcePath;
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

    private void saveConfig() {
        try {
            modConfig.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}