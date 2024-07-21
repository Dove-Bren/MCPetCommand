package com.smanzana.petcommand.entity;

import com.smanzana.petcommand.PetCommand;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.IronGolem;
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
		registry.register(EntityType.Builder.<BoundIronGolemEntity>of(BoundIronGolemEntity::new, MobCategory.MISC)
				.sized(1.4F, 2.7F).clientTrackingRange(10)
			.build("").setRegistryName(BoundIronGolemEntity.ID));
	}
	
	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(BOUND_IRON_GOLEM, IronGolem.createAttributes().build());
	}
	
}
