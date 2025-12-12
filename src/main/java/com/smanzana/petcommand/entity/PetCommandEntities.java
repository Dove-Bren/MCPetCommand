package com.smanzana.petcommand.entity;

import com.smanzana.petcommand.PetCommand;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = PetCommand.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PetCommandEntities {
	
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, PetCommand.MODID);

	// Values copied from EntityType.IRON_GOLEM
	public static final RegistryObject<EntityType<BoundIronGolemEntity>> BOUND_IRON_GOLEM = REGISTRY.register(BoundIronGolemEntity.ID, () -> EntityType.Builder.<BoundIronGolemEntity>of(BoundIronGolemEntity::new, MobCategory.MISC)
			.sized(1.4F, 2.7F).clientTrackingRange(10)
			.build(""));
	
	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(BOUND_IRON_GOLEM.get(), IronGolem.createAttributes().build());
	}
	
}
