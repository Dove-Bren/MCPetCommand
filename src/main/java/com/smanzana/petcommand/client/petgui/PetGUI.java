package com.smanzana.petcommand.client.petgui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.client.container.IPetContainer;
import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;
import com.smanzana.petcommand.api.client.petgui.PetGUIRenderHelper;
import com.smanzana.petcommand.api.client.petgui.sheet.PetStatSheet;
import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.api.entity.IRerollablePet;
import com.smanzana.petcommand.api.pet.PetInfo;
import com.smanzana.petcommand.api.pet.PetInfo.PetValue;
import com.smanzana.petcommand.api.pet.PetInfo.ValueFlavor;
import com.smanzana.petcommand.client.container.AutoGuiContainer;
import com.smanzana.petcommand.client.container.PetCommandContainers;
import com.smanzana.petcommand.network.NetworkHandler;
import com.smanzana.petcommand.network.message.PetGUIControlMessage;
import com.smanzana.petcommand.network.message.PetGUISyncMessage;
import com.smanzana.petcommand.sound.PetCommandSounds;
import com.smanzana.petcommand.util.ArrayUtil;
import com.smanzana.petcommand.util.ColorUtil;
import com.smanzana.petcommand.util.ContainerUtil;
import com.smanzana.petcommand.util.ContainerUtil.IPackedContainerProvider;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A nice wrapped up pet gui.
 * 
 * Doesn't do a lot on its own. Instead, things can build it up using pet sheets
 * 
 * @author Skyler
 */
public class PetGUI {
	
	// Need a static registry of open containers for dispatching messages on the server
	private static Map<Integer, PetContainer<?>> containers = new HashMap<>();
	
	private static int lastKey = 0;
	
	private static int takeNextKey() {
		return lastKey++;
	}
	
	private static void register(PetContainer<?> container, int key) {
		if (container.player.level.isClientSide) {
			throw new IllegalArgumentException("Can't register on the client!");
		}
		
		containers.put(key, container);
	}
	
	private static void revoke(int id) {
		containers.remove(id);
	}
	
	public static void updateServerContainer(int id, CompoundTag nbt) {
		PetContainer<?> container = containers.get(id);
		if (container != null) {
			container.handle(nbt);
		}
	}
	
	private static PetContainer<?> clientContainer = null;
	
	public static void updateClientContainer(CompoundTag nbt) {
		if (clientContainer != null) {
			clientContainer.handle(nbt);
		}
	}

	public static class PetContainer<T extends LivingEntity> extends AbstractContainerMenu implements IPetContainer<T> {
		
		public static final String ID = "pet_container";

		private Player player;
		
		private T pet;
		
		private LivingEntity livingPet;
		
		private int currentSheet;
		
		protected List<IPetGUISheet<T>> sheetsAllInternal;
		
		protected final int id;
		
		private int guiOffsetX;
		private int guiOffsetY;
		
		@SafeVarargs
		public PetContainer(int windowId, int netID, T pet, Player player, IPetGUISheet<T> ... sheets) {
			super(PetCommandContainers.PetGui, windowId);
			this.pet = pet;
			this.livingPet= (LivingEntity) pet;
			this.player = player;
			this.currentSheet = 0;
			this.sheetsAllInternal = Lists.newArrayList(sheets);
			this.id = netID;
			
			if (livingPet.level.isClientSide()) {
				PetGUI.clientContainer = this;
			}

			this.setSheet(0);
		}
		
		public static final PetContainer<?> FromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf buffer) {
			// Just gonna let this crash if it fails instead of making a 'dummy pet'
			final UUID petID = buffer.readUUID();
			final int containerID = buffer.readVarInt();
			final int numSheets = buffer.readVarInt();
			
			LivingEntity pet = null;
			Entity foundEnt = PetCommand.GetEntityByUUID(PetCommand.GetProxy().getPlayer().level, petID);
			
			if (foundEnt == null || !(foundEnt instanceof LivingEntity)) {
				return null; // crash
			}
			
			pet = (LivingEntity) foundEnt;
			
			final Player player = PetCommand.GetProxy().getPlayer();
			final IPetGUISheet<?>[] sheets = GetSheetsFor(pet, player);

			if (numSheets != sheets.length) {
				PetCommand.LOGGER.error("Sheet count differs on client and server for " + pet);
				return null;
			}
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			PetContainer<?> container = new PetContainer(windowId, containerID, pet, player, sheets); 
					//pet.getGUIContainer(PetCommand.GetProxy().getPlayer(), windowId, containerID);
			
			return container;
		}
		
		protected static <T extends LivingEntity> IPetGUISheet<T>[] GetSheetsFor(T pet, Player player) {
			if (pet instanceof IEntityPet p) {
				@SuppressWarnings("unchecked")
				final IPetGUISheet<T>[] sheets = (IPetGUISheet<T>[]) p.getContainerSheets(player);
				return sheets;
			} else {
				// default sheets for non-pet
				return ArrayUtil.MakeArray(
					new PetStatSheet<>(pet)
				);
			}
		}
		
		public static <T extends LivingEntity> IPackedContainerProvider Make(T pet, Player player) {
			final IPetGUISheet<T>[] sheets = GetSheetsFor(pet, player);
			final int key = takeNextKey();
			
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, playerIn) -> {
				final PetContainer<T> container = new PetContainer<T>(windowId, key, pet, playerIn, sheets);
				PetGUI.register(container, key);
				return container;
			}, (buffer) -> {
				buffer.writeUUID(pet.getUUID());
				buffer.writeVarInt(key);
				buffer.writeVarInt(sheets.length);
			});
		}
		
		@Override
		public AbstractContainerMenu getContainer() {
			return this;
		}
		
		@Override
		public T getPet() {
			return this.pet;
		}
		
//		public void overrideID(int id) {
//			if (!this.player.world.isRemote) {
//				throw new IllegalArgumentException("Can't reset id on the server!");
//			}
////			revoke(id);
////			registerAt(this, id);
//			this.id = id;
//		}
		
		public void setGUIOffets(int x, int y) {
			this.guiOffsetX = x;
			this.guiOffsetY = y;
		}
		
		@Override
		public boolean stillValid(Player playerIn) {
			if (pet == null) {
				// Pet hasn't been synced yet
				return false;
			}
			if (pet instanceof IEntityPet p) {
				return playerIn.equals(p.getOwner());
			} else {
				return pet.isAlive();
			}
		}

		// Caution: This assumes only one player has these open!
		@Override
		public void removed(Player playerIn) {
			if (this.getCurrentSheet() != null) {
				this.getCurrentSheet().hideSheet(pet, player, this);
			}
			revoke(this.id);
		}
		
		@Override
		public @Nonnull ItemStack quickMoveStack(Player playerIn, int fromSlot) {
			return ItemStack.EMPTY;
		}
		
		/**
		 * Returns a list of sheets this container has.
		 * This is a collection that's filtered down to what should be shown
		 * @return
		 */
		protected List<IPetGUISheet<T>> getSheets() {
			final PetContainer<T> container = this;
			return sheetsAllInternal.parallelStream().filter((sheet) -> {
				return sheet.shouldShow(container.pet, container);
			}).collect(Collectors.toList());
		}
		
		@Override
		public IPetGUISheet<T> getCurrentSheet() {
			return getSheets().get(currentSheet);
		}
		
		@Override
		public void setSheet(int index) {
			if (this.currentSheet < this.getSheetCount()) {
				// If we changed the number of sheets, we may have an invalid one to close. So just don't close it.
				this.getCurrentSheet().hideSheet(pet, player, this);
			}
			this.currentSheet = Math.min(Math.max(0, index), getSheets().size() - 1);
			this.getCurrentSheet().showSheet(pet, player, this, GUI_SHEET_WIDTH, GUI_SHEET_HEIGHT, guiOffsetX, guiOffsetY);
		}
		
		@Override
		public int getSheetIndex() {
			return this.currentSheet;
		}
		
		@Override
		public void clearSlots() {
			this.slots.clear();
			// this.inventoryItemStacks.clear(); // uh oh? TODO does not having this cause pr oblems?
		}
		
		@Override
		public void dropContainerInventory(Container inv) {
			this.clearContainer(player, inv);
		}
		
		@Override
		public void addSheetSlot(Slot slot) {
			this.addSlot(slot);
		}
		
		public int getContainerID() {
			return this.id;
		}
		
		@Override
		public int getSheetCount() {
			return this.getSheets().size();
		}
		
		protected boolean supportsReroll() {
			return pet != null && pet instanceof IRerollablePet;
		}
		
		// Handle a message sent from the client.
		// Could be a button click to change sheets, some other control message,
		// or a message for updating a sheet's contents.
		protected void handle(CompoundTag nbt) {
			PetContainerMessageType type = NetworkHelper.GetType(nbt);
			
			if (type == null) {
				return;
			}
			
			switch (type) {
			case SET_SHEET:
				int index = NetworkHelper.GetSendSheetIndex(nbt);
				this.setSheet(index);
				break;
			case SHEET_DATA:
				this.getCurrentSheet().handleMessage(NetworkHelper.GetSendSheetData(nbt));
				break;
			case REROLL:
				if (pet != null
					&& supportsReroll()
					&& pet instanceof IEntityPet p
					&& p.getOwner() instanceof Player
					&& ((Player) p.getOwner()).isCreative()) {
					// Reset container sheet. The client will send this as well later.
					this.setSheet(0);
					((IRerollablePet) pet).rerollStats();
				}
				break;
			}
		}
		
		@Override
		public void sendSheetMessageToServer(CompoundTag data) {
			NetworkHelper.ClientSendSheetData(id, data);
		}
		
		@Override
		public void sendSheetMessageToClient(CompoundTag data) {
			NetworkHelper.ServerSendSheetData((ServerPlayer) this.player, data);
		}
		
	}
	
	public static int GUI_SHEET_WIDTH = 246;
	public static int GUI_SHEET_HEIGHT = 191;
	
	
	@OnlyIn(Dist.CLIENT)
	public static class PetGUIContainer<T extends LivingEntity> extends AutoGuiContainer<PetContainer<T>> {
		
		public static final ResourceLocation TEXT = new ResourceLocation(PetCommand.MODID + ":textures/gui/container/tamed_pet_gui.png");

		public static int GUI_TEX_WIDTH = 256;
		public static int GUI_TEX_HEIGHT = 256;
		public static int GUI_TEX_CELL_HOFFSET = 0;
		public static int GUI_TEX_CELL_VOFFSET = 202;
		
		private static int GUI_LENGTH_PREVIEW = 48;
		private static int GUI_INFO_HOFFSET = 12;
		private static int GUI_INFO_VOFFSET = GUI_LENGTH_PREVIEW + 10;
		private static int GUI_SHEET_NHOFFSET = 10;
		private static int GUI_SHEET_MARGIN = 5;
		private static int GUI_SHEET_BUTTON_WIDTH = 50;
		private static int GUI_SHEET_BUTTON_HEIGHT = 20;
		private static int GUI_SHEET_BUTTON_VOFFSET = 5;
		private static int GUI_SHEET_VOFFSET = GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT + GUI_SHEET_BUTTON_VOFFSET;
		
		public static class PetGUIRenderHelperImpl extends PetGUIRenderHelper {
			
			public static final void Register() {
				new PetGUIRenderHelperImpl();
			}
			
			private PetGUIRenderHelperImpl() {
				PetGUIRenderHelper.ProvideImpl(this);
			}

			protected void drawSlotRaw(PoseStack matrixStackIn, int width, int height, int x, int y) {
				Screen.blit(matrixStackIn, x, y,
						GUI_TEX_CELL_HOFFSET, GUI_TEX_CELL_VOFFSET, 
						width, height,
						GUI_TEX_WIDTH, GUI_TEX_HEIGHT);
			}

			@Override
			protected void drawSingleSlot(PoseStack matrixStackIn, int width, int height) {
				RenderSystem.setShaderTexture(0, PetGUI.PetGUIContainer.TEXT);
				this.drawSlotRaw(matrixStackIn, width, height, 0, 0);
			}

			@Override
			protected void drawSlots(PoseStack matrixStackIn, int width, int height, int count, int columns) {
				RenderSystem.setShaderTexture(0, PetGUI.PetGUIContainer.TEXT);
				
				for (int i = 0; i < count; i++) {
					final int x = width * (i % columns);
					final int y = height * (i / columns);
					this.drawSlotRaw(matrixStackIn, width, height, x, y);
				}
			}
		}
		
		//private static int GUI_OPEN_ANIM_TIME = 20 * 1;
		
		private PetContainer<T> container;
		
		//private int openTicks;
		
		public PetGUIContainer(PetContainer<T> container, Inventory playerInv, Component name) {
			super(container, playerInv, name);
			this.container = container;
			//this.openTicks = 0;
		}
		
		@Override
		public void init() {
			this.imageWidth = this.width;
			this.imageHeight = this.height;
			super.init();
			
			final int GUI_SHEET_HOFFSET = this.width - (GUI_SHEET_WIDTH + GUI_SHEET_NHOFFSET);
			this.container.setGUIOffets(GUI_SHEET_HOFFSET, GUI_SHEET_VOFFSET);
			this.container.setSheet(0);
		}
		
//		@Override
//		public void tick() {
//			super.updateScreen();
//			
//			this.openTicks++;
//		}

		@Override
		protected void renderBg(PoseStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			final int GUI_SHEET_HOFFSET = this.width - (GUI_SHEET_WIDTH + GUI_SHEET_NHOFFSET);
			final int GUI_SHEET_BUTTON_HOFFSET = GUI_SHEET_HOFFSET;
			
			if (this.container.pet == null) {
				drawCenteredString(matrixStackIn, font, "Waiting for server...", this.width / 2, this.height / 2, 0XFFAAAAAA);
				return;
			}
			
			// Draw top-left preview
			{
				GuiComponent.fill(matrixStackIn, 0, 0, GUI_LENGTH_PREVIEW, GUI_LENGTH_PREVIEW, 0xFF283D2A);
				
				int xPosition = GUI_LENGTH_PREVIEW / 2;
				int yPosition = GUI_LENGTH_PREVIEW / 2;
				//RenderHelper.turnOff(); disable world lighting?
				InventoryScreen.renderEntityInInventory(
						xPosition,
						(int) (GUI_LENGTH_PREVIEW * .75f),
						(int) (GUI_LENGTH_PREVIEW * .2),
						(float) (xPosition) - mouseX,
						(float) (-yPosition) - mouseY,
						(LivingEntity) container.pet);
			}
			
			// Move everything forward ahead of the drawn entity
			// Can't just move entity back cause there's a GRAY plane drawn at just below 0 Z
			matrixStackIn.pushPose();
			matrixStackIn.translate(0, 0, 51);
			
			// Black background (not overlapping preview)
			{
				GuiComponent.fill(matrixStackIn, 0, GUI_LENGTH_PREVIEW, width, height, 0xFF000000);
				GuiComponent.fill(matrixStackIn, GUI_LENGTH_PREVIEW, 0, width, GUI_LENGTH_PREVIEW, 0xFF000000);
			}
			
			// Draw stats and stuff
			{
				final int w = (GUI_SHEET_HOFFSET - GUI_SHEET_MARGIN) - (GUI_INFO_HOFFSET * 2);
				int x = GUI_INFO_HOFFSET;
				int y = GUI_INFO_VOFFSET;
				//final int w = 125;
				final int h = 14;
				final int centerX = GUI_SHEET_HOFFSET / 2;
				
				final PetInfo info = PetInfo.Wrap(container.pet);
				
				// Health
				{
					drawCenteredString(matrixStackIn, this.font, ChatFormatting.BOLD + "Health", centerX, y, 0xFFFFFFFF);
					y += font.lineHeight + 5;
					GuiComponent.fill(matrixStackIn, x, y, x + w, y + h, 0xFFD0D0D0);
					GuiComponent.fill(matrixStackIn, x + 1, y + 1, x + w - 1, y + h - 1, 0xFF201010);
					
					int prog = (int) ((float) (w - 2) * (info.getCurrentHp() / info.getMaxHp()));
					GuiComponent.fill(matrixStackIn, x + 1, y + 1, x + 1 + prog, y + h - 1, 0xFFA02020);
					
					drawCenteredString(matrixStackIn, font,
							String.format("%d / %d", (int) info.getCurrentHp(), (int) info.getMaxHp()),
							centerX,
							y + (h / 2) - (font.lineHeight / 2),
							0xFFC0C0C0);
					
					y += h + 10;
				}
				
				List<PetValue> values = info.getPetValues();
				if (values != null) {
					for (int i = 0; i < Math.min(4, values.size()); i++) {
						final PetValue value = values.get(i);
						
						if (value.label() == null) {
							continue;
						}
						
						final ValueFlavor flavor = value.flavor();
						final int color = ColorUtil.colorToARGB(flavor.colorR(1f), flavor.colorG(1f), flavor.colorB(1f), flavor.colorA(1f));
						
						drawCenteredString(matrixStackIn, this.font, value.label().copy().withStyle(ChatFormatting.BOLD), centerX, y, 0xFFFFFFFF);
						y += font.lineHeight + 5;
						GuiComponent.fill(matrixStackIn, x, y, x + w, y + h, 0xFFD0D0D0);
						GuiComponent.fill(matrixStackIn, x + 1, y + 1, x + w - 1, y + h - 1, 0xFF201010);
						
						int prog = (int) ((float) (w - 2) * (value.current() / value.max()));
						GuiComponent.fill(matrixStackIn, x + 1, y + 1, x + 1 + prog, y + h - 1, color);
						
						drawCenteredString(matrixStackIn, font,
								String.format("%d / %d", (int) value.current(), (int) value.max()),
								centerX,
								y + (h / 2) - (font.lineHeight / 2),
								0xFFC0C0C0);
						
						y += h + 10;
					}
				}
			}
			
			if (container.getSheets().size() > 0) {
				int x = GUI_SHEET_BUTTON_HOFFSET;
				
				for (IPetGUISheet<T> sheet : container.getSheets()) {
					GuiComponent.fill(matrixStackIn, x, GUI_SHEET_BUTTON_VOFFSET, x + GUI_SHEET_BUTTON_WIDTH, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT, 0xFFFFFFFF);
					GuiComponent.fill(matrixStackIn, x + 1, GUI_SHEET_BUTTON_VOFFSET + 1, x + GUI_SHEET_BUTTON_WIDTH - 1, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT - 1, 0xFF202020);
					
					if (sheet == container.getCurrentSheet()) {
						GuiComponent.fill(matrixStackIn, x, GUI_SHEET_BUTTON_VOFFSET, x + GUI_SHEET_BUTTON_WIDTH, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT, 0x40FFFFFF);
					}
					
					if (mouseX >= x && mouseX <= x + GUI_SHEET_BUTTON_WIDTH && mouseY >= GUI_SHEET_BUTTON_VOFFSET && mouseY <= GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT) {
						GuiComponent.fill(matrixStackIn, x, GUI_SHEET_BUTTON_VOFFSET, x + GUI_SHEET_BUTTON_WIDTH, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT, 0x40FFFFFF);
					}
					
					String text = sheet.getButtonText();
					int strLen = font.width(text);
					int strHeight = font.lineHeight;
					font.draw(matrixStackIn, text, x + (GUI_SHEET_BUTTON_WIDTH / 2) - (strLen / 2), GUI_SHEET_BUTTON_VOFFSET + (GUI_SHEET_BUTTON_HEIGHT / 2) - (strHeight / 2), 0xFFFFFFFF);
					x += GUI_SHEET_BUTTON_WIDTH;
				}
				
				if (container.supportsReroll() && PetCommand.GetProxy().getPlayer().isCreative()) {
					GuiComponent.fill(matrixStackIn, x, GUI_SHEET_BUTTON_VOFFSET, x + GUI_SHEET_BUTTON_WIDTH, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT, 0xFFFFDDFF);
					GuiComponent.fill(matrixStackIn, x + 1, GUI_SHEET_BUTTON_VOFFSET + 1, x + GUI_SHEET_BUTTON_WIDTH - 1, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT - 1, 0xFF702070);
					
					if (mouseX >= x && mouseX <= x + GUI_SHEET_BUTTON_WIDTH && mouseY >= GUI_SHEET_BUTTON_VOFFSET && mouseY <= GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT) {
						GuiComponent.fill(matrixStackIn, x, GUI_SHEET_BUTTON_VOFFSET, x + GUI_SHEET_BUTTON_WIDTH, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT, 0x40FFFFFF);
					}
					
					String text = "Reroll";
					int strLen = font.width(text);
					int strHeight = font.lineHeight;
					font.draw(matrixStackIn, text, x + (GUI_SHEET_BUTTON_WIDTH / 2) - (strLen / 2), GUI_SHEET_BUTTON_VOFFSET + (GUI_SHEET_BUTTON_HEIGHT / 2) - (strHeight / 2), 0xFFFFFFFF);
					x += GUI_SHEET_BUTTON_WIDTH;
				}
			}
			
			RenderSystem.setShaderTexture(0, TEXT);
			
			// Draw sheet
			IPetGUISheet<T> sheet = container.getCurrentSheet();
			if (sheet != null) {
				matrixStackIn.pushPose();
				matrixStackIn.translate(GUI_SHEET_HOFFSET, GUI_SHEET_VOFFSET, 0);
				
				RenderSystem.enableBlend();
				RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
				blit(matrixStackIn, -GUI_SHEET_MARGIN, -GUI_SHEET_MARGIN, 0, 0, GUI_SHEET_WIDTH + (GUI_SHEET_MARGIN * 2), GUI_SHEET_HEIGHT + (GUI_SHEET_MARGIN * 2), GUI_TEX_WIDTH, GUI_TEX_HEIGHT);
				
				sheet.draw(matrixStackIn, Minecraft.getInstance(), partialTicks, GUI_SHEET_WIDTH,
						GUI_SHEET_HEIGHT, mouseX - GUI_SHEET_HOFFSET, mouseY - GUI_SHEET_VOFFSET);
				matrixStackIn.popPose();
			}
			
			matrixStackIn.popPose();
		}
		
		@Override
		protected void renderLabels(PoseStack matrixStackIn, int mouseX, int mouseY) {
			//super.drawGuiContainerForegroundLayer(matrixStackIn, mouseX, mouseY);
			
			final int GUI_SHEET_HOFFSET = this.width - (GUI_SHEET_WIDTH + GUI_SHEET_NHOFFSET);
			
			IPetGUISheet<T> sheet = container.getCurrentSheet();
			if (sheet != null) {
				matrixStackIn.pushPose();
				matrixStackIn.translate(GUI_SHEET_HOFFSET, GUI_SHEET_VOFFSET, 0);
				
				RenderSystem.enableBlend();
				
				sheet.overlay(matrixStackIn, Minecraft.getInstance(), 0f, GUI_SHEET_WIDTH,
						GUI_SHEET_HEIGHT, mouseX - GUI_SHEET_HOFFSET, mouseY - GUI_SHEET_VOFFSET);
				matrixStackIn.popPose();
			}
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
			
			if (!container.stillValid(PetCommand.GetProxy().getPlayer())) {
				return false;
			}
			
			// Only allow custom clicking s tuff if there isn't an item being held
			if (menu.getCarried().isEmpty()) {
			
				final int GUI_SHEET_HOFFSET = this.width - (GUI_SHEET_WIDTH + GUI_SHEET_NHOFFSET);
				final int GUI_SHEET_BUTTON_HOFFSET = GUI_SHEET_HOFFSET;
				
				// Sheet button?
				if (mouseX >= GUI_SHEET_BUTTON_HOFFSET && mouseY >= GUI_SHEET_BUTTON_VOFFSET
						&& mouseY <= GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT) {
					int buttonIdx = (int) ((mouseX - GUI_SHEET_BUTTON_HOFFSET) / GUI_SHEET_BUTTON_WIDTH);
					if (buttonIdx < container.getSheets().size()) {
						// Clicked a button!
						PetCommandSounds.UI_TICK.play(PetCommand.GetProxy().getPlayer());
						this.container.setSheet(buttonIdx);
						NetworkHelper.ClientSendSheet(container.id, buttonIdx);
						return true;
					} else if (container.supportsReroll()
							&& buttonIdx == container.getSheets().size()
							&& PetCommand.GetProxy().getPlayer().isCreative()) {
						NetworkHelper.ClientSendReroll(container.id);
						// Reset sheet index in case reroll removed a tab
						this.container.setSheet(0);
						NetworkHelper.ClientSendSheet(container.id, this.container.currentSheet);
						return true;
					}
				}
				
				// Clicking on the sheet?
				if (mouseX >= GUI_SHEET_HOFFSET && mouseX <= GUI_SHEET_HOFFSET + GUI_SHEET_WIDTH
						&& mouseY >= GUI_SHEET_VOFFSET && mouseY <= GUI_SHEET_VOFFSET + GUI_SHEET_HEIGHT) {
					IPetGUISheet<T> sheet = container.getCurrentSheet();
					if (sheet != null) {
						if (sheet.mouseClicked(mouseX - GUI_SHEET_HOFFSET, mouseY - GUI_SHEET_VOFFSET, mouseButton)) {
							return true; // else let super handle
						}
					}
					
				}
			}
			
			return super.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}
	
	private static enum PetContainerMessageType {
		
		SET_SHEET("_SETSHEET"),
		
		SHEET_DATA("_SHEET_DATA"),
		
		REROLL("_REROLL");
		
		private final String nbtKey;
		
		private PetContainerMessageType(String key) {
			this.nbtKey = key;
		}
		
		public String getKey() {
			return this.nbtKey;
		}
	}
	
	private static final String NBT_TYPE = "TYPE";
	private static final String NBT_INDEX = "INDEX";
	private static final String NBT_USERDATA = "DATA";
	
	private static final class NetworkHelper {
		
		private static void clientSendInternal(int id, CompoundTag nbt) {
			PetGUIControlMessage message = new PetGUIControlMessage(id, nbt);
			
			NetworkHandler.sendToServer(message);
		}
		
		private static void serverSendInternal(ServerPlayer player, CompoundTag nbt) {
			PetGUISyncMessage message = new PetGUISyncMessage(nbt);
			
			NetworkHandler.sendTo(message, player);
		}
		
		private static CompoundTag base(PetContainerMessageType type) {
			CompoundTag nbt = new CompoundTag();
			nbt.putString(NBT_TYPE, type.getKey());
			return nbt;
		}
		
		public static void ClientSendSheet(int id, int sheet) {
			CompoundTag nbt = base(PetContainerMessageType.SET_SHEET);
			nbt.putInt(NBT_INDEX, sheet);
			
			clientSendInternal(id, nbt);
		}
		
		public static void ClientSendSheetData(int id, CompoundTag data) {
			CompoundTag nbt = base(PetContainerMessageType.SHEET_DATA);
			nbt.put(NBT_USERDATA, data);
			
			clientSendInternal(id, nbt);
		}
		
		public static void ClientSendReroll(int id) {
			CompoundTag nbt = base(PetContainerMessageType.REROLL);
			
			clientSendInternal(id, nbt);
		}
		
		public static void ServerSendSheetData(ServerPlayer player, CompoundTag data) {
			CompoundTag nbt = base(PetContainerMessageType.SHEET_DATA);
			nbt.put(NBT_USERDATA, data);
			
			serverSendInternal(player, nbt);
		}
		
		
		public static PetContainerMessageType GetType(CompoundTag nbt) {
			String str = nbt.getString(NBT_TYPE);
			if (str == null || str.isEmpty()) {
				return null;
			}
			
			for (PetContainerMessageType type : PetContainerMessageType.values()) {
				if (type.getKey().equalsIgnoreCase(str)) {
					return type;
				}
			}
			
			return null;
		}
		
		public static int GetSendSheetIndex(CompoundTag nbt) {
			return nbt.getInt(NBT_INDEX);
		}
		
		public static CompoundTag GetSendSheetData(CompoundTag nbt) {
			return nbt.getCompound(NBT_USERDATA);
		}
		
	}
	
}
