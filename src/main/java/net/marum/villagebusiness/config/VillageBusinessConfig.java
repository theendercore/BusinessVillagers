package net.marum.villagebusiness.config;

import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.util.Walkable;
import me.fzzyhmstrs.fzzy_config.validation.collection.ValidatedMap;
import me.fzzyhmstrs.fzzy_config.validation.minecraft.ValidatedIdentifier;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedAny;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import net.minecraft.resources.ResourceLocation;

import static net.marum.villagebusiness.VillageBusiness.*;
import static net.marum.villagebusiness.pricing.ItemPrices.isValid;

public class VillageBusinessConfig extends Config {
    public VillageBusinessConfig() {
        super(id(MOD_ID));
    }

    public ValidatedDouble requestTimeMultiplier = new ValidatedDouble(2.0);
    public ValidatedDouble requestPriceMultiplier = new ValidatedDouble(2.0);
    public ValidatedDouble requestCooldownMultiplier = new ValidatedDouble(2.0);

    public ValidatedMap<ResourceLocation, PriceInfo> itemPrices = makePrices();

    private static ValidatedMap<ResourceLocation, PriceInfo> makePrices() {
        var map = (new ValidatedMap.Builder<ResourceLocation, PriceInfo>())
                .keyHandler(new ValidatedIdentifier())
                .valueHandler(new ValidatedAny<>(new PriceInfo()))
                .defaults(CfgDefaults.MAP)
                .build();
        map.listenToEntry(list -> isValid.set(false));
        return map;
    }


    public static class PriceInfo implements Walkable {
        public int sellAmount = 0;
        public int price = 0;
        public int saleChance = 0;
        public int requestChance = 0;
        public int cooldown = 0;

        public PriceInfo() {
        }

        public PriceInfo(int price, int sellAmount, int saleChance, int requestChance, int cooldown) {
            this.sellAmount = sellAmount;
            this.price = price;
            this.saleChance = saleChance;
            this.requestChance = requestChance;
            this.cooldown = cooldown;
        }
    }

}
