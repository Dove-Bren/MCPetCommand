package com.smanzana.petcommand.entity;

import com.smanzana.petcommand.PetCommand;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = PetCommand.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(PetCommand.MODID)
public class PetCommandEntities {

	@ObjectHolder(BoundIronGolemEntity.ID) public static EntityType<BoundIronGolemEntity> BOUND_IRON_GOLEM;
	
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<EntityType<?>> event) {
		final IForgeRegistry<EntityType<?>> registry = event.getRegistry();
		
		// Values copied from EntityType.IRON_GOLEM
		registry.register(EntityType.Builder.<BoundIronGolemEntity>of(BoundIronGolemEntity::new, EntityClassification.MISC)
				.sized(1.4F, 2.7F).clientTrackingRange(10)
			.build("").setRegistryName(BoundIronGolemEntity.ID));
	}
	
	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(BOUND_IRON_GOLEM, IronGolemEntity.createAttributes().build());
	}
	
}
