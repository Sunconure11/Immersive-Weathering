package com.ordana.immersive_weathering.mixin.forge;

import com.ordana.immersive_weathering.reg.ModTags;
import com.ordana.immersive_weathering.utils.WeatheringHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;


@Mixin(FireBlock.class)
public abstract class FireMixin extends BaseFireBlock{

    @Unique
    private BlockState bs;

    public FireMixin(Properties settings, float damage) {
        super(settings, damage);
    }


    @Inject(method = "tryCatchFire",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/world/level/Level.removeBlock (Lnet/minecraft/core/BlockPos;Z)Z",
                    shift = At.Shift.AFTER))
    private void afterRemoveBlock(Level pLevel, BlockPos pPos, int pChance, Random pRandom, int pAge, Direction face, CallbackInfo ci) {
        WeatheringHelper.onFireBurnBlock(pLevel, pPos, bs);
    }

    @Inject(method = "tryCatchFire",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/world/level/Level.removeBlock (Lnet/minecraft/core/BlockPos;Z)Z"))
    private void beforeRemoveBlock(Level pLevel, BlockPos pPos, int pChance, Random pRandom, int pAge, Direction face, CallbackInfo ci) {
        bs = pLevel.getBlockState(pPos);
    }



    //expired fire turns into soot
    @Inject(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z",
                    ordinal = 3,
                    shift = At.Shift.AFTER))
    private void removeBlock(BlockState state, ServerLevel serverLevel, BlockPos pos, Random random, CallbackInfo ci) {
        WeatheringHelper.onFireExpired(serverLevel, pos, state);
    }


    //fire can replace soot
    @Inject(method = "getFireOdds",
            at = @At(value = "HEAD"), cancellable = true)
    private void canFireReplace(LevelReader reader, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (reader.getBlockState(pos).is(ModTags.FIRE_REPLACEABLE)) cir.setReturnValue(0);
    }


}