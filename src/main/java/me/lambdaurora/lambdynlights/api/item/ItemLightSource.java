/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights.api.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.lambdaurora.lambdynlights.DynamicLightsReforged;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents an item light source.
 *
 * @author LambdAurora
 * @version 1.3.1
 * @since 1.3.0
 */
public class ItemLightSource
{
    public final ResourceLocation id;
    public final Item item;
    public final int        luminance;
    public final boolean    waterSensitive;

    public ItemLightSource(@NotNull ResourceLocation id, @NotNull Item item, int luminance)
    {
        this(id, item, luminance, false);
    }

    public ItemLightSource(@NotNull ResourceLocation id, @NotNull Item item, int luminance, boolean waterSensitive)
    {
        this.id = id;
        this.item = item;
        this.luminance = luminance;
        this.waterSensitive = waterSensitive;
    }

    /**
     * Gets the luminance of the item.
     *
     * @param stack            The item stack.
     * @param submergedInWater True if submerged in water, else false.
     * @return The luminance value between 0 and 15.
     */
    public int getLuminance(@NotNull ItemStack stack, boolean submergedInWater)
    {
        if (this.waterSensitive && submergedInWater)
            return 0; // Don't emit light with water sensitive items while submerged in water.

        return this.luminance;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemLightSource that = (ItemLightSource) o;
        return luminance == that.luminance &&
                waterSensitive == that.waterSensitive &&
                Objects.equals(id, that.id) &&
                Objects.equals(item, that.item);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, item, luminance, waterSensitive);
    }

    @Override
    public String toString()
    {
        return "ItemLightSource{" +
                "item=" + item +
                ", luminance=" + luminance +
                ", water_sensitive=" + waterSensitive +
                '}';
    }

    public static @NotNull Optional<ItemLightSource> fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json)
    {
        if (!json.has("item") || !json.has("luminance")) {
            DynamicLightsReforged.warn("Failed to parse item light source \"" + id + "\", invalid format: missing required fields.");
            return Optional.empty();
        }

        ResourceLocation affectId = new ResourceLocation(json.get("item").getAsString());
        Item item = Registry.ITEM.get(affectId);

        if (item == Items.AIR)
            return Optional.empty();

        int luminance;
        JsonPrimitive luminanceElement = json.get("luminance").getAsJsonPrimitive();
        if (luminanceElement.isNumber()) {
            luminance = luminanceElement.getAsInt();
        } else if (luminanceElement.isString()) {
            String luminanceStr = luminanceElement.getAsString();
            if (luminanceStr.equals("block")) {
                if (item instanceof BlockItem) {
                    luminance = ((BlockItem) item).getBlock().defaultBlockState().getLightEmission();
                } else {
                    return Optional.empty();
                }
            } else {
                Block block = Registry.BLOCK.get(new ResourceLocation(luminanceStr));
                if (block == Blocks.AIR)
                    return Optional.empty();

                luminance = block.defaultBlockState().getLightEmission();
            }
        } else {
            DynamicLightsReforged.warn("Failed to parse item light source \"" + id + "\", invalid format: \"luminance\" field value isn't string or integer.");
            return Optional.empty();
        }

        boolean waterSensitive = false;
        if (json.has("water_sensitive"))
            waterSensitive = json.get("water_sensitive").getAsBoolean();

        return Optional.of(new ItemLightSource(id, item, luminance, waterSensitive));
    }
}
