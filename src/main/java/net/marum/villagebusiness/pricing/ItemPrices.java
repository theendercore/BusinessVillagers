package net.marum.villagebusiness.pricing;

import net.marum.villagebusiness.VillageBusiness;
import net.marum.villagebusiness.config.VillageBusinessConfig;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.marum.villagebusiness.VillageBusiness.SERVER;

public class ItemPrices {
    public static AtomicBoolean isValid = new AtomicBoolean(true);
    public static Map<Item, ItemPrice> priceList;

    static final int NUGGET = 1;
    static final int EMERALD = 9;
    static final int BLOCK = 81;

    public static Map<Item, ItemPrice> getPrices() {
        if (priceList == null || !isValid.get()) {
            priceList = createPriceList();
        }

        return priceList;
    }


    public static Map<Item, ItemPrice> createPriceList() {
        Map<Item, ItemPrice> output = new HashMap<>();

        for (Map.Entry<ResourceLocation, VillageBusinessConfig.PriceInfo> entry : VillageBusiness.CONFIG.itemPrices.entrySet()) {
            var item = BuiltInRegistries.ITEM.get(entry.getKey());
            if (item == Items.AIR) continue;
            output.put(item, new ItemPrice(item, entry.getValue()));

        }

        getAllRecipes(output);

        return output;
    }

    // Procedurally generate missing craftable item's prices
    private static void getAllRecipes(Map<Item, ItemPrice> map) {
        if (SERVER == null) return;
        RegistryAccess registryManager = SERVER.registryAccess();

        List<String> itemsWithoutPrice = new ArrayList<>();
        for (ResourceLocation id : BuiltInRegistries.ITEM.keySet()) {
            itemsWithoutPrice.add(id.toString());
        }

        Map<Item, ItemPrice> primeProducts = new HashMap<>();
        for (Map.Entry<Item, ItemPrice> entry : map.entrySet()) {
            primeProducts.put(entry.getKey(), entry.getValue());
            String id = BuiltInRegistries.ITEM.getKey(entry.getKey()).toString();
            itemsWithoutPrice.remove(id);
        }

        RecipeManager recipeManager = SERVER.getRecipeManager();

        boolean stillNeedRecipes = true;
        int safety = 0;

        while (stillNeedRecipes && safety < 5) {
            stillNeedRecipes = false;
            for (RecipeHolder<?> recipe : recipeManager.getRecipes()) {
                ItemStack output = recipe.value().getResultItem(registryManager);
                if (map.containsKey(output.getItem())) {
                    continue;
                }

                Item outputItem = output.getItem();

                if (primeProducts.containsKey(outputItem)) {
                    // Skip prime product
                    continue;
                }

                int outputCount = output.getCount();
                float totalIngredientCost = 0f;
                int minIngredientSaleChance = 100;
                int minIngredientRequestChance = 100;
                int maxIngredientCooldown = 0;
                boolean allIngredientsHavePrices = true;
                for (Ingredient ingredient : recipe.value().getIngredients()) {
                    boolean ingredientHasPrice = false;
                    float cheapestIngredient = 99999999;
                    if (!ingredient.isEmpty()) {
                        for (ItemStack stack : ingredient.getItems()) {
                            if (map.containsKey(stack.getItem())) {
                                ItemPrice itemPrice = map.get(stack.getItem());
                                float ingredientCost = 1.0f * itemPrice.getPrice(1) / itemPrice.getSellAmount(1);
                                if (ingredientCost != 0) {
                                    ingredientHasPrice = true;
                                    if (ingredientCost < cheapestIngredient) {
                                        cheapestIngredient = ingredientCost;
                                    }
                                    if (itemPrice.getSaleChance(1) < minIngredientSaleChance) {
                                        minIngredientSaleChance = itemPrice.getSaleChance(1);
                                    }
                                    if (itemPrice.getRequestChance() < minIngredientRequestChance) {
                                        minIngredientRequestChance = itemPrice.getRequestChance();
                                    }
                                    if (itemPrice.getCooldown(1) > maxIngredientCooldown) {
                                        maxIngredientCooldown = itemPrice.getCooldown(1);
                                    }
                                }
                            }
                        }
                        if (!ingredientHasPrice) {
                            allIngredientsHavePrices = false;
                            stillNeedRecipes = true;
                            break;
                        }
                        totalIngredientCost += cheapestIngredient;
                    }
                }
                if (allIngredientsHavePrices) {
                    totalIngredientCost /= outputCount;
                    int sellAmount = 1;
                    if (totalIngredientCost > 0) {
                        while (totalIngredientCost < 1) {
                            sellAmount *= 2;
                            totalIngredientCost *= 2;
                        }
                        if (!map.containsKey(outputItem) || (Math.round(totalIngredientCost) < map.get(outputItem).getPrice(1) / map.get(outputItem).getSellAmount(1))) {
                            map.put(outputItem, new ItemPrice(outputItem, Math.round(totalIngredientCost), sellAmount, minIngredientSaleChance, minIngredientRequestChance, maxIngredientCooldown));
                            String id = BuiltInRegistries.ITEM.getKey(outputItem).toString();
                            itemsWithoutPrice.remove(id);
                        }
                    }
                }
            }
            safety += 1;
        }
    }
}
