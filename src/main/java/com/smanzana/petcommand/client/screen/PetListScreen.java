package com.smanzana.petcommand.client.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL32;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.PetFuncs;
import com.smanzana.petcommand.api.pet.EPetAction;
import com.smanzana.petcommand.api.pet.EPetOrderType;
import com.smanzana.petcommand.api.pet.EPetPlacementMode;
import com.smanzana.petcommand.api.pet.EPetTargetMode;
import com.smanzana.petcommand.api.pet.PetInfo;
import com.smanzana.petcommand.api.pet.PetInfo.PetValue;
import com.smanzana.petcommand.client.icon.PetPlacementModeIcon;
import com.smanzana.petcommand.client.icon.PetTargetModeIcon;
import com.smanzana.petcommand.client.overlay.OverlayRenderer;
import com.smanzana.petcommand.client.widget.PetValueWidget;
import com.smanzana.petcommand.client.widgetdupe.AutoLayoutParentWidget;
import com.smanzana.petcommand.client.widgetdupe.AutoRowWidget;
import com.smanzana.petcommand.client.widgetdupe.ChildButtonWidget;
import com.smanzana.petcommand.client.widgetdupe.LabeledWidget;
import com.smanzana.petcommand.client.widgetdupe.ListWidget;
import com.smanzana.petcommand.client.widgetdupe.ObscurableChildWidget;
import com.smanzana.petcommand.client.widgetdupe.ParentWidget;
import com.smanzana.petcommand.client.widgetdupe.ScrollbarWidget;
import com.smanzana.petcommand.client.widgetdupe.SpacerWidget;
import com.smanzana.petcommand.client.widgetdupe.TextWidget;
import com.smanzana.petcommand.client.widgetdupe.Tooltip;
import com.smanzana.petcommand.network.NetworkHandler;
import com.smanzana.petcommand.network.message.OpenPetGUIMessage;
import com.smanzana.petcommand.network.message.PetCommandMessage;
import com.smanzana.petcommand.pet.PetOrder;
import com.smanzana.petcommand.proxy.ClientProxy;
import com.smanzana.petcommand.util.ColorUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;

/**
 * Screen for viewing all pets and their status, and configuring individual pet details.
 */
public class PetListScreen extends Screen {
	
	protected final Player player;
	protected ListWidget<PetListEntry> petList;
	protected ScrollbarWidget scrollbar;
	protected ListWidget<ObscurableChildWidget> controlList;

	public PetListScreen(Component title, Player player) {
		super(title);
		this.player = player;
		
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}
	
	@Override
	protected void init() {
		super.init();
		
		final int margin = 20;
		final int wideSpace = this.width - (margin * 2);
		
		// first third is control, and then second chunk is list
		final int leftWidth = wideSpace / 3;
		
		final int scrollbarWidth = 10;
		final int listWidth = width - (margin + margin + leftWidth + scrollbarWidth + 1);
		petList = new ListWidget<>(margin + leftWidth, margin,
				listWidth, height - (margin + margin), TextComponent.EMPTY);
		scrollbar = new ScrollbarWidget(petList, width - (margin + scrollbarWidth + 1), margin, scrollbarWidth, height - (margin + margin));
		petList.setScrollbar(scrollbar).setSpacing(2).setAutoSizeChildren().setMargin(1);
		
		this.addRenderableWidget(scrollbar);
		this.addRenderableWidget(petList);
		
		// Get all tamed pets for list
		List<PetListEntry> entries = new ArrayList<>();
		for (LivingEntity ent : PetFuncs.GetTamedEntities(player)) {
			entries.add(new PetListEntry(this, ent, petList.getWidth() - 2)); // -2 for 1 pixel margin on list
		}
		petList.addChildren(entries);
		
		final int extraHSpace = Math.max(0, leftWidth - (68 + 24 + 5));
		
		controlList = new ListWidget<>(margin, margin, leftWidth, height - (margin + margin), TextComponent.EMPTY);
		controlList.setMargin(5);
		controlList.setSpacing(5);
		controlList.setAutoSizeChildren();
		
		AutoRowWidget<ObscurableChildWidget> row = new AutoRowWidget<>(0, 0, leftWidth, 24, TextComponent.EMPTY);
			row.addChild(new TextWidget(this, new TextComponent("Placement: "), 0, 0, 68, 24).centerVertical().centerInBounds().tooltip(Tooltip.create(new TranslatableComponent("petplacement.info"))));
			if (extraHSpace > 5) {
				row.addChild(new SpacerWidget(0, 0, extraHSpace - 5, 1));
			}
			row.addChild(new PlacemodeModeButton(this, 0, 0, 24, 24, this::cyclePlacementMode, this::getPlacementMode).tooltip(this::getPetPlacementTooltip));
		controlList.addChild(row);
		
		row = new AutoRowWidget<>(0, 0, leftWidth, 24, TextComponent.EMPTY);
			row.addChild(new TextWidget(this, new TextComponent("Target Mode: "), 0, 0, 68, 24).centerVertical().centerInBounds().tooltip(Tooltip.create(new TranslatableComponent("pettarget.info"))));
			if (extraHSpace > 5) {
				row.addChild(new SpacerWidget(0, 0, extraHSpace - 5, 1));
			}
			row.addChild(new TargetModeButton(this, 0, 0, 24, 24, this::cycleTargetMode, this::getTargetMode).tooltip(this::getPetTargetTooltip));
		controlList.addChild(row);
		
		controlList.addChild(new ChildButtonWidget(this, 0, 0, 20, 24, new TextComponent("All Stop"), this::stopAll));
		controlList.addChild(new ChildButtonWidget(this, 0, 0, 20, 24, new TextComponent("All Come"), this::callAll));
		controlList.addChild(new ChildButtonWidget(this, 0, 0, 20, 24, new TextComponent("All Stay"), this::stayAll));
		
		this.addRenderableWidget(controlList);
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {

		this.renderBackground(matrixStack);

		final int margin = 20;
		// Set up mask
		{
			RenderSystem.enableDepthTest(); // make sure this is on
			RenderSystem.depthMask(true);
			
			matrixStack.pushPose();
			matrixStack.translate(0, 0, 950); // 950 copied from achv tab vanilla renderer
			
			// Set the max depth up to a really high value to prevent things from rendering under it
			RenderSystem.colorMask(false, false, false, false); // only write to depth
			Screen.fill(matrixStack, 4680, 2260, -4680, -2260, 0xFF000000); // Magic width/height is from vanilla. Is that 8k?
			
			// Slice out the section we want to render on to allow things to render
			matrixStack.translate(0, 0, -950);
			RenderSystem.depthFunc(GL32.GL_GEQUAL); // Only render to spots with a depth already set to higher AKA our giant mask
			Screen.fill(matrixStack, margin, margin, width - (margin), height - (margin), 0xFF000000); // "Render" no color at this depth, effectively setting the max depth drawn back to 0
			RenderSystem.depthFunc(GL32.GL_LEQUAL); // Restore to standard 'only draw on top of things' behavior for rest of render
			RenderSystem.colorMask(true, true, true, true); // write color again
			
			matrixStack.popPose();
		}
		
		// Renders widgets
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		
		// Clear out mask
		{
			//RenderSystem.enableDepthTest(); // make sure this is on
			RenderSystem.depthMask(true);
			matrixStack.pushPose();
			matrixStack.translate(0, 0, 0);
			RenderSystem.colorMask(false, false, false, false); // only write to depth
			RenderSystem.depthFunc(GL32.GL_GEQUAL);
			Screen.fill(matrixStack, 4680, 2260, -4680, -2260, 0xFF000000); // Magic width/height is from vanilla. Is that 8k?
			RenderSystem.colorMask(true, true, true, true);
			RenderSystem.depthFunc(GL32.GL_LEQUAL);
			matrixStack.popPose();
		}
		
		// Tooltips and foregrounds
		for (Widget child : this.renderables) {
			if (child instanceof AbstractWidget widget) {
				widget.renderToolTip(matrixStack, mouseX, mouseY);
			}
		}
	}
	
	@Override
	public void renderBackground(PoseStack matrixStack) {
		super.renderBackground(matrixStack);
		
		final int margin = 20;
		final int wideSpace = this.width - (margin * 2);
		
		// first third is control, and then second chunk is list
		final int leftWidth = wideSpace / 3;
		
		final int scrollbarWidth = 8;
		
		final int startX = margin + leftWidth;
		final int startY = margin;
		final int endX = startX + (width - (margin + margin + leftWidth + scrollbarWidth + 2));
		final int endY = startY + (height - (margin + margin));
		
		Screen.fill(matrixStack, startX, startY, endX, endY, 0xFF202020);
		
		Screen.fill(matrixStack, margin, margin, margin + leftWidth, margin + (height - (margin + margin)), 0xFF404040);
	}
	
	protected EPetPlacementMode getPlacementMode() {
		return PetCommand.GetPetCommandManager().getPlacementMode(player);
	}
	
	protected void cyclePlacementMode(ChildButtonWidget ignored) {
		((ClientProxy) PetCommand.GetProxy()).cyclePlacementMode();
	}
	
	protected EPetTargetMode getTargetMode() {
		return PetCommand.GetPetCommandManager().getTargetMode(player);
	}
	
	protected void cycleTargetMode(ChildButtonWidget ignored) {
		((ClientProxy) PetCommand.GetProxy()).cycleTargetMode();
	}
	
	protected static final List<Component> getPetActionTooltip(EPetAction action) {
		return List.of(action.getName().copy().withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD),
				action.getDescription());
	}
	
	protected static final List<Component> getPetPlacementTooltip(EPetPlacementMode mode) {
		return List.of(mode.getName().copy().withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD),
				mode.getDescription());
	}
	
	protected List<Component> getPetPlacementTooltip() {
		return getPetPlacementTooltip(this.getPlacementMode());
	}
	
	protected static final List<Component> getPetTargetTooltip(EPetTargetMode mode) {
		return List.of(mode.getName().copy().withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD),
				mode.getDescription());
	}
	
	protected List<Component> getPetTargetTooltip() {
		return getPetTargetTooltip(this.getTargetMode());
	}
	
	protected void stopAll(ChildButtonWidget ignored) {
		PetCommand.GetPetCommandManager().commandAllStop(player);
	}
	
	protected void stayAll(ChildButtonWidget ignored) {
		for (LivingEntity ent : PetFuncs.GetTamedEntities(player)) {
			PetCommand.GetPetCommandManager().setPetOrder(player, ent, new PetOrder(EPetOrderType.STAY, null));
		}
	}
	
	protected void callAll(ChildButtonWidget ignored) {
		for (LivingEntity ent : PetFuncs.GetTamedEntities(player)) {
			PetCommand.GetPetCommandManager().setPetOrder(player, ent, new PetOrder(EPetOrderType.MOVE_TO_ME, null));
		}
	}
	
	protected static class PetListEntry extends AutoRowWidget<ObscurableChildWidget> {
		
		protected static final int collapsedHeight = 10;
		protected static final int expandedHeight = collapsedHeight * 2;
		
		public final LivingEntity pet;
		
		// Whether we are currently supposed to be expanded or not
		protected boolean expanded;
		private AutoLayoutParentWidget<? extends AutoRowWidget<ObscurableChildWidget>> parentWidget;
		protected AutoRowWidget<ObscurableChildWidget> firstRow;
		protected AutoRowWidget<ObscurableChildWidget> secondRow;

		public PetListEntry(PetListScreen parent, LivingEntity pet, int width) {
			super(0, 0, width, collapsedHeight, TextComponent.EMPTY);
			this.pet = pet;
			
			this.setAutoSizeChildren();
			this.setSpacing(3);
			
			final PetInfo info = PetInfo.Wrap(pet);
			final int dist = Math.round(pet.distanceTo(parent.player));
			
			final int tagWidth = 6;
			
			this.addChild(new ColorButtonWidget(parent, pet, 0, 0, 6, expandedHeight, TextComponent.EMPTY, (b) -> this.cycleColor(), () -> this.getColor()));
			ListWidget<ObscurableChildWidget> mainList = new ListWidget<>(0, 0, 300 - tagWidth, collapsedHeight, TextComponent.EMPTY);
			this.addChild(mainList);
			
			{
				firstRow = new AutoRowWidget<>(0, 0, width - tagWidth, collapsedHeight, TextComponent.EMPTY);

				final int spacing = 3;
				firstRow.setMargin(0);
				firstRow.setSpacing(spacing);
				firstRow.setAutoSizeChildren();
				
				// This is a sum I am doing manually of the total size of entries.
				final int totalPrecomputeSpace = 40 + 55 + 30 + 45 + collapsedHeight + (spacing * 8);
				final int extraSpace = (width-tagWidth) - totalPrecomputeSpace;
				
				final int extraPer = Math.max(0, extraSpace / 3);
				
				firstRow.addChild(new TextWidget(parent, pet.getDisplayName(), 0, 0, 40, collapsedHeight).truncate().scale(.5f).color(0xFFFFFFFF).centerVertical().centerInBounds());
				if (extraPer > 0)
				firstRow.addChild(new SpacerWidget(0, 0, extraPer, collapsedHeight));
				firstRow.addChild(new LabeledWidget(parent,
						new LabeledWidget.StringLabel("Health: "),
						new LabeledWidget.TextValue(() -> String.format("%4d / %d", (int)info.getCurrentHp(), (int)info.getMaxHp())),
						0, 3, 55, collapsedHeight)
						.scale(.5f));
				if (extraPer > 0)
				firstRow.addChild(new SpacerWidget(0, 0, extraPer, collapsedHeight));
				firstRow.addChild(new LabeledWidget(parent,
						new LabeledWidget.StringLabel("Action: "),
						new LabeledWidget.PetActionValue(() -> PetInfo.Wrap(pet).getPetAction(), 16, 16),
						0, 3, 30, collapsedHeight)
						.tooltip(() -> getPetActionTooltip(PetInfo.Wrap(pet).getPetAction())).scale(.5f));
				if (extraPer > 0)
				firstRow.addChild(new SpacerWidget(0, 0, extraPer, collapsedHeight));
				firstRow.addChild(new TextWidget(parent, new TextComponent(String.format("%5d blocks away", dist)), 0, 0, 45, collapsedHeight).truncate().scale(.5f).color(0xFFFFFFFF).centerVertical().centerInBounds());
				firstRow.addChild(new HideButtonWidget(parent, pet, 0, 0, collapsedHeight, collapsedHeight, (b) -> {
					toggleHidden();
				}, () -> OverlayRenderer.IsHiddenFromHUD(pet)));
			}
			mainList.addChild(firstRow);
			
			// Second row
			{
				List<PetValue> values = info.getPetValues();
				@Nullable PetValue valueToDisplay = OverlayRenderer.GetPetValue(pet);
				
				final int spacing = 3;
				
				secondRow = new AutoRowWidget<>(0, 0, width - tagWidth, collapsedHeight, TextComponent.EMPTY); 
				secondRow.setMargin(0);
				secondRow.setSpacing(spacing);
				secondRow.setAutoSizeChildren();
				
				// This is a sum I am doing manually of the total size of entries.
				final int totalPrecomputeSpace = 40 + 55 + 20 + 20 + 20 + 20 + (spacing * 8);
				final int extraSpace = (width-tagWidth) - totalPrecomputeSpace;
				
				final int extraPer = Math.max(0, extraSpace / 2);
				
				secondRow.addChild(new PetChildButtonWidget(parent, pet, 0, 0, 40, 8, new TextComponent("Open Menu"), (b) -> {
					NetworkHandler.sendToServer(new OpenPetGUIMessage(pet));
				}).checkDistance());
				
				if (extraPer > 0)
				secondRow.addChild(new SpacerWidget(0, 0, extraPer, collapsedHeight));
				
				if (valueToDisplay != null) {
					secondRow.addChild(new PetValueWidget(parent, 0, 3, 46, collapsedHeight, () -> OverlayRenderer.GetPetValue(pet))
							.scale(.5f));
					
					if (values != null && values.size() > 1) {
						secondRow.addChild(new PetChildButtonWidget(parent, pet, 0, 0, 6, collapsedHeight, new TextComponent("o"), (b) -> {
							cycleValue();
						}));
					} else {
						secondRow.addChild(new SpacerWidget(0, 0, 6, collapsedHeight));
					}
				} else {
					secondRow.addChild(new SpacerWidget(0, 0, 55, collapsedHeight));
				}
				
				secondRow.addChild(new SpacerWidget(0, 0, 20, collapsedHeight));
				
				if (extraPer > 0)
				secondRow.addChild(new SpacerWidget(0, 0, extraPer, collapsedHeight));
				
				secondRow.addChild(new PetChildButtonWidget(parent, pet, 0, 0, 20, collapsedHeight, new TextComponent("Stop"), this::stopCommand));
				secondRow.addChild(new PetChildButtonWidget(parent, pet, 0, 0, 20, collapsedHeight, new TextComponent("Call"), this::comeCommand));
				secondRow.addChild(new PetChildButtonWidget(parent, pet, 0, 0, 20, collapsedHeight, new TextComponent("Stay"), this::stayCommand));
			}
			mainList.addChild(secondRow);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T extends ParentWidget<?>> void setParent(T parent) {
			super.setParent(parent);
			if (parent == null) {
				this.parentWidget = null;
			} else if (parent instanceof AutoLayoutParentWidget autoParent) {
				this.parentWidget = autoParent;
			}
		}

		@Override
		public void renderButton(PoseStack matrixStackIn, int p_93677_, int p_93678_, float p_93679_) {
			super.renderButton(matrixStackIn, p_93677_, p_93678_, p_93679_);
			RenderSystem.enableDepthTest();
			fill(matrixStackIn, x, y, x + this.getWidth(), y + this.getHeight(), this.isHoveredOrFocused() ? 0x40808080 : 0x40804040);
		}

		@Override
		public void renderToolTip(PoseStack p_93653_, int p_93654_, int p_93655_) {
			super.renderToolTip(p_93653_, p_93654_, p_93655_);
		}
		
		public void expand() {
			if (!this.expanded) {
				this.expanded = true;
				
				// Update height
				this.setHeight(expandedHeight);
				if (this.parentWidget != null) {
					this.parentWidget.recalculateLayout();
				}
				this.recalculateLayout();
			}
		}
		
		public void collapse() {
			if (this.expanded) {
				this.expanded = false;
				
				//
				this.setHeight(collapsedHeight);
				if (this.parentWidget != null) {
					this.parentWidget.recalculateLayout();
				}
				this.recalculateLayout();
			}
		}
		
		public void toggleExpand() {
			if (this.expanded) {
				this.collapse();
			} else {
				this.expand();
			}
		}
		
		protected void toggleHidden() {
			final UUID id = this.pet.getUUID();
			final boolean current = OverlayRenderer.IsHiddenFromHUD(pet);
			
			// toggle. Optimize unhiding by removing override
			PetCommand.GetClientPetOverrides().setHide(id, current ? null : Boolean.TRUE);
		}
		
		@Override
		public void onClick(double mouseX, double mouseY, int button) {
			super.onClick(mouseX, mouseY, button);
			if (button == 0) {
				this.toggleExpand();
			} else if (button == 1) {
				this.toggleHidden();
			}
		}
		
		@Override
		protected boolean isValidClickButton(int button) {
			return button == 0 || button == 1;
		}
		
		protected void cycleColor() {
			// Ugly but I'd rather store color for now instead of index so that this can be expanded in the future
			final int current = getColor();
			
			// Try to find dye color that matches
			int matchIdx = -1;
			final DyeColor[] values = DyeColor.values();
			for (int i = 0; i < values.length; i++) {
				final DyeColor dye = values[i];
				if (ColorUtil.dyeToARGB(dye) == current) {
					matchIdx = i;
					break;
				}
			}
			
			final DyeColor next;
			if (matchIdx == -1) {
				next = values[0];
			} else {
				next = values[(matchIdx+1) % values.length];
			}
			
			PetCommand.GetClientPetOverrides().setColor(pet.getUUID(), ColorUtil.dyeToARGB(next));
		}
		
		protected int getColor() {
			return OverlayRenderer.GetPetColorRaw(pet);
		}
		
		protected void cycleValue() {
			int current = OverlayRenderer.GetPetValueIndex(pet);
			final PetInfo info = PetInfo.Wrap(pet);
			List<PetValue> values = info.getPetValues();
			
			if (values != null && !values.isEmpty()) {
				int next = (current + 1) % values.size();
				PetCommand.GetClientPetOverrides().setValueIdx(pet.getUUID(), next);
			}
		}
		
		protected void stopCommand(ChildButtonWidget ignored) {
			NetworkHandler.sendToServer(PetCommandMessage.PetStop(pet));
		}
		
		protected void stayCommand(ChildButtonWidget ignored) {
			NetworkHandler.sendToServer(PetCommandMessage.PetOrder(pet, new PetOrder(EPetOrderType.STAY, null)));
		}
		
		protected void comeCommand(ChildButtonWidget ignored) {
			NetworkHandler.sendToServer(PetCommandMessage.PetOrder(pet, new PetOrder(EPetOrderType.MOVE_TO_ME, null)));
		}
		
	}
	
	protected static class PetChildButtonWidget extends ChildButtonWidget {
		
		protected static final List<Component> TOO_FAR = List.of(new TextComponent("Too far away!"));
		
		protected final LivingEntity pet;
		protected boolean checkDistance;

		public PetChildButtonWidget(Screen parent, LivingEntity pet, int x, int y, int width, int height, Component label, OnPress onPress) {
			super(parent, x, y, width, height, label, onPress);
			this.pet = pet;
			this.checkDistance = false;
		}
		
		public PetChildButtonWidget checkDistance() {
			this.checkDistance = true;
			return this;
		}
		
		protected boolean isEnabled() {
			return pet != null && pet.isAlive() && (!checkDistance || pet.distanceTo(PetCommand.GetProxy().getPlayer()) <= 10);
		}
		
		@Override
		public void renderButton(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			this.active = isEnabled();
			super.renderButton(matrixStackIn, mouseX, mouseY, partialTicks);
		}
		
		@Override
		public void renderToolTip(PoseStack matrixStackIn, int mouseX, int mouseY) {
			if (this.isHoveredOrFocused()) {
				if (!this.active) {
					matrixStackIn.pushPose();
					matrixStackIn.translate(0, 0, 100);
					parent.renderComponentTooltip(matrixStackIn, TOO_FAR, mouseX, mouseY);
					matrixStackIn.popPose();
				} else if (this.tooltip != null) {
					Tooltip.RenderTooltip(tooltip, parent, matrixStackIn, mouseX, mouseY);
				}
			}
		}
		
	}
	
	protected static final class ColorButtonWidget extends PetChildButtonWidget {
		
		protected final Supplier<Integer> colorSupplier;

		public ColorButtonWidget(Screen parent, LivingEntity pet, int x, int y, int width, int height, Component label, OnPress onPress, Supplier<Integer> color) {
			super(parent, pet, x, y, width, height, label, onPress);
			this.colorSupplier = color;
		}
		
		@Override
		public void renderButton(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			//super.renderButton(matrixStackIn, mouseX, mouseY, partialTicks);
			Minecraft minecraft = Minecraft.getInstance();
			Font font = minecraft.font;
			final int color = this.colorSupplier.get();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableDepthTest();
			{
//				this.blit(matrixStackIn, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
//				this.blit(matrixStackIn, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
				// gonna just render outside border, and then grainy texture. Will scale if button is too large?
				fill(matrixStackIn, x, y, x + width, y + height, this.isHoveredOrFocused() ? 0xFFCCCCCC : 0xFF000000);
				
				fill(matrixStackIn, x + 1, y + 1, x + width - 2, y + height - 2, color);
				
				// highlights
				fill(matrixStackIn, x + 1, y + 1, x + (width - 1), y + 2, 0x40FFFFFF);
				fill(matrixStackIn, x + 1, y + 1, x + 2, y + (height - 1), 0x40FFFFFF);
				fill(matrixStackIn, x + 1, y + (height - 2), x + (width - 1), y + (height - 1), 0x40000000);
				fill(matrixStackIn, x + (width - 2), y + 1, x + (width - 1), y + (height - 1), 0x40000000);
				
			}
			this.renderBg(matrixStackIn, minecraft, mouseX, mouseY);
			int j = getFGColor();
			
			if (this.getMessage() != null && this.getMessage() != TextComponent.EMPTY)
			{
				// possibly scale down if button is small
				matrixStackIn.pushPose();
				matrixStackIn.translate(this.x + this.width / 2, this.y + this.height / 2, 0);
				if (font.lineHeight > this.height - 4) {
					final float scale = (float)(this.height - 4) / (float) (font.lineHeight);
					matrixStackIn.scale(scale, scale, 1f);
				}
				drawCenteredString(matrixStackIn, font, this.getMessage(), 0, -(font.lineHeight / 2), j | Mth.ceil(this.alpha * 255.0F) << 24);
				matrixStackIn.popPose();
			}
		}
	}
	
	protected static final class HideButtonWidget extends PetChildButtonWidget {
		
		private static final ResourceLocation TEXT = new ResourceLocation(PetCommand.MODID, "textures/gui/misc_widget.png");
		
		private static final int TEX_WIDTH = 64;
		private static final int TEX_HEIGHT = 64;
		
		private static final List<Component> HIDE_TEXT = List.of(new TextComponent("Hide"));
		private static final List<Component> SHOW_TEXT = List.of(new TextComponent("Show"));
			
		protected final Supplier<Boolean> hiddenSupplier;
	
		public HideButtonWidget(Screen parent, LivingEntity pet, int x, int y, int width, int height, OnPress onPress, Supplier<Boolean> hidden) {
			super(parent, pet, x, y, width, height, TextComponent.EMPTY, onPress);
			this.hiddenSupplier = hidden;
			this.tooltip(() -> isPetHidden() ? SHOW_TEXT : HIDE_TEXT);
		}
		
		protected boolean isPetHidden() {
			return this.hiddenSupplier.get();
		}
		
		@Override
		public Component getMessage() {
			return super.getMessage();
		}
		
		@Override
		public void renderButton(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			//super.renderButton(matrixStackIn, mouseX, mouseY, partialTicks);
			final float sat = this.active ? 1f : .3f;
			final int vOffset = 0 + (isPetHidden() ? 32 : 0);
			RenderSystem.setShaderColor(sat, sat, sat, this.alpha);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableDepthTest();
			{
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderTexture(0, TEXT);
				blit(matrixStackIn, x, y, width, width, 32, vOffset, 32, 32, TEX_WIDTH, TEX_HEIGHT);
				
			}
			
		}
	}
	
	protected static class PlacemodeModeButton extends ChildButtonWidget {
		protected final Supplier<EPetPlacementMode> modeSupplier;

		public PlacemodeModeButton(Screen parent, int x, int y, int width, int height, OnPress onPress, Supplier<EPetPlacementMode> modeSupplier) {
			super(parent, x, y, width, height, TextComponent.EMPTY, onPress);
			this.modeSupplier = modeSupplier;
		}
		
		@Override
		public void renderButton(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			final EPetPlacementMode mode = this.modeSupplier.get();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableDepthTest();
			{
				fill(matrixStackIn, x, y, x + width, y + height, this.isHoveredOrFocused() ? 0xFFCCCCCC : 0xFF000000);
				
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
				blit(matrixStackIn, x + 1, y + 1, width - 2, height - 2, 2, 68, (width - 2), Math.min(height - 2, 14), 256, 256);
				
				{
					//fill(matrixStackIn, x + 1, y + 1, x + width - 2, y + height - 2, color);
					final float sat = this.isHoveredOrFocused() ? 1f : .8f;
					PetPlacementModeIcon.get(mode).draw(matrixStackIn, x + 1, y + 1, width - 2, height - 2, sat, sat, sat, 1f);
				}
				
				// highlights
				fill(matrixStackIn, x + 1, y + 1, x + (width - 1), y + 2, 0x40FFFFFF);
				fill(matrixStackIn, x + 1, y + 1, x + 2, y + (height - 1), 0x40FFFFFF);
				fill(matrixStackIn, x + 1, y + (height - 2), x + (width - 1), y + (height - 1), 0x40000000);
				fill(matrixStackIn, x + (width - 2), y + 1, x + (width - 1), y + (height - 1), 0x40000000);
				
			}
		}
	}
	
	protected static class TargetModeButton extends ChildButtonWidget {
		protected final Supplier<EPetTargetMode> modeSupplier;

		public TargetModeButton(Screen parent, int x, int y, int width, int height, OnPress onPress, Supplier<EPetTargetMode> modeSupplier) {
			super(parent, x, y, width, height, TextComponent.EMPTY, onPress);
			this.modeSupplier = modeSupplier;
		}
		
		@Override
		public void renderButton(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
			final EPetTargetMode mode = this.modeSupplier.get();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableDepthTest();
			{
				fill(matrixStackIn, x, y, x + width, y + height, this.isHoveredOrFocused() ? 0xFFCCCCCC : 0xFF000000);
				
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
				blit(matrixStackIn, x + 1, y + 1, width - 2, height - 2, 2, 68, (width - 2), Math.min(height - 2, 14), 256, 256);
				
				{
					//fill(matrixStackIn, x + 1, y + 1, x + width - 2, y + height - 2, color);
					final float sat = this.isHoveredOrFocused() ? 1f : .8f;
					PetTargetModeIcon.get(mode).draw(matrixStackIn, x + 1, y + 1, width - 2, height - 2, sat, sat, sat, 1f);
				}
				
				// highlights
				fill(matrixStackIn, x + 1, y + 1, x + (width - 1), y + 2, 0x40FFFFFF);
				fill(matrixStackIn, x + 1, y + 1, x + 2, y + (height - 1), 0x40FFFFFF);
				fill(matrixStackIn, x + 1, y + (height - 2), x + (width - 1), y + (height - 1), 0x40000000);
				fill(matrixStackIn, x + (width - 2), y + 1, x + (width - 1), y + (height - 1), 0x40000000);
				
			}
		}
	}
}
