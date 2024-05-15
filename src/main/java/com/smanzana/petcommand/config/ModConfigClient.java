package com.smanzana.petcommand.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfigClient {
	
	// Major healthbars
	// Show healthbars at all
	// Healthbar position
	
	ForgeConfigSpec.BooleanValue configShowHealthbars;
	ForgeConfigSpec.BooleanValue configShowBigHealthbars;
	ForgeConfigSpec.IntValue configHealthbarAnchorX;
	ForgeConfigSpec.IntValue configHealthbarAnchorY;
	
	public ModConfigClient(ForgeConfigSpec.Builder builder) {
//		builder.comment("PetCommand options")
//			.push("petcommand");
		{
			configShowHealthbars = builder
					.translation("show.healthbar.desc")
					.define("show.healthbar", true);
			
			configShowBigHealthbars = builder
					.translation("show.bighealthbar.desc")
					.define("show.bighealthbar", true);
			
			configHealthbarAnchorX = builder
					.translation("display.anchorx.desc")
					.defineInRange("display.anchorx", -2, -Integer.MAX_VALUE, Integer.MAX_VALUE);
			
			configHealthbarAnchorY = builder
					.translation("display.anchory.desc")
					.defineInRange("display.anchory", 75, 0, Integer.MAX_VALUE);
		}
//		builder.pop();
	}
}
