package com.smanzana.petcommand.client.widgetdupe;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.network.chat.Component;

@FunctionalInterface
public interface ITooltip extends Supplier<List<Component>> {
	
}
