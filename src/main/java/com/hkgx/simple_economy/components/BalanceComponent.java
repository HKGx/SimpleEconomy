package com.hkgx.simple_economy.components;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.nbt.NbtCompound;

public class BalanceComponent implements ComponentV3 {

    private int value = 0;

    public int getBalance() {
        return value;
    }

    public int deposit(int amount) {
        value += amount;
        return value;
    }

    public int withdraw(int amount) {
        value -= amount;
        return value;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.value = tag.getInt("value");

    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt("value", this.value);
    }

}
