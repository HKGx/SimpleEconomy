package com.hkgx.simple_economy;

import com.hkgx.simple_economy.components.BalanceComponent;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.util.Identifier;

public final class EconomyComponents implements EntityComponentInitializer {
    public static final ComponentKey<BalanceComponent> BALANCE = ComponentRegistryV3.INSTANCE
            .getOrCreate(new Identifier("simple_economy:balance"), BalanceComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(BALANCE, player -> new BalanceComponent(), RespawnCopyStrategy.ALWAYS_COPY);
    }

}
