package SacredBarkPlus;

import SacredBarkPlus.patches.PotionLogicPatches;
import basemod.BaseMod;
import basemod.abstracts.CustomSavable;
import basemod.interfaces.*;


import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.potions.*;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.SacredBark;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpireInitializer
public class SacredBarkPlus implements
        PostInitializeSubscriber {
    public static void initialize() {
        BaseMod.subscribe(new SacredBarkPlus());
    }

    @Override
    public void receivePostInitialize() {

    }

}