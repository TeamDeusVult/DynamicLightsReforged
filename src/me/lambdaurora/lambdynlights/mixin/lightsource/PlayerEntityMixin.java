/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambDynamicLights.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdynlights.mixin.lightsource;

import me.lambdaurora.lambdynlights.DynamicLightSource;
import me.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements DynamicLightSource
{
    @Shadow
    public abstract boolean isSpectator();

    private int   lambdynlights_luminance;
    private Level lambdynlights_lastWorld;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world)
    {
        super(entityType, world);
    }

    @Override
    public void dynamicLightTick()
    {
        if (this.isOnFire() || this.isGlowing()) {
            this.lambdynlights_luminance = 15;
        } else {
            int luminance = 0;
            BlockPos eyePos = new BlockPos(this.getX(), this.getEyeY(), this.getZ());
            boolean submergedInFluid = !this.level.getFluidState(eyePos).isEmpty();
            for (ItemStack equipped : this.getAllSlots()) {
                if (!equipped.isEmpty())
                    luminance = Math.max(luminance, LambDynLights.getLuminanceFromItemStack(equipped, submergedInFluid));
            }

            this.lambdynlights_luminance = luminance;
        }

        if (this.isSpectator())
            this.lambdynlights_luminance = 0;

        if (this.lambdynlights_lastWorld != this.getCommandSenderWorld()) {
            this.lambdynlights_lastWorld = this.getCommandSenderWorld();
            this.lambdynlights_luminance = 0;
        }
    }

    @Override
    public int getLuminance()
    {
        return this.lambdynlights_luminance;
    }
}
