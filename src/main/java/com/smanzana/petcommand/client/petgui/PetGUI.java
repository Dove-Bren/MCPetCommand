package com.smanzana.petcommand.client.petgui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.petcommand.PetCommand;
import com.smanzana.petcommand.api.client.container.IPetContainer;
import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;
import com.smanzana.petcommand.api.client.petgui.PetGUIRenderHelper;
import com.smanzana.petcommand.api.client.petgui.PetGUIStatAdapter;
import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.api.entity.IRerollablePet;
import com.smanzana.petcommand.client.container.AutoGuiContainer;
import com.smanzana.petcommand.client.container.PetCommandContainers;
import com.smanzana.petcommand.network.NetworkHandler;
import com.smanzana.petcommand.network.message.PetGUIControlMessage;
import com.smanzana.petcommand.network.message.PetGUISyncMessage;
import com.smanzana.petcommand.sound.PetCommandSounds;
import com.smanzana.petcommand.util.ContainerUtil;
import com.smanzana.petcommand.util.ContainerUtil.IPackedContainerProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
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
		if (container.player.world.isRemote) {
			throw new IllegalArgumentException("Can't register on the client!");
		}
		
		containers.put(key, container);
	}
	
	private static void revoke(int id) {
		containers.remove(id);
	}
	
	public static void updateServerContainer(int id, CompoundNBT nbt) {
		PetContainer<?> container = containers.get(id);
		if (container != null) {
			container.handle(nbt);
		}
	}
	
	private static PetContainer<?> clientContainer = null;
	
	public static void updateClientContainer(CompoundNBT nbt) {
		if (clientContainer != null) {
			clientContainer.handle(nbt);
		}
	}

	public static class PetContainer<T extends IEntityPet> extends Container implements IPetContainer<T> {
		
		public static final String ID = "pet_container";

		private PlayerEntity player;
		
		private T pet;
		
		private LivingEntity livingPet;
		
		private int currentSheet;
		
		protected List<IPetGUISheet<T>> sheetsAllInternal;
		
		protected final int id;
		
		private int guiOffsetX;
		private int guiOffsetY;
		
		@SafeVarargs
		public PetContainer(int windowId, int netID, T pet, PlayerEntity player, IPetGUISheet<T> ... sheets) {
			super(PetCommandContainers.PetGui, windowId);
			this.pet = pet;
			this.livingPet= (LivingEntity) pet;
			this.player = player;
			this.currentSheet = 0;
			this.sheetsAllInternal = Lists.newArrayList(sheets);
			this.id = netID;
			
			if (livingPet.world.isRemote()) {
				PetGUI.clientContainer = this;
			}

			this.setSheet(0);
		}
		
		public static final PetContainer<?> FromNetwork(int windowId, PlayerInventory playerInv, PacketBuffer buffer) {
			// Just gonna let this crash if it fails instead of making a 'dummy pet'
			final UUID petID = buffer.readUniqueId();
			final int containerID = buffer.readVarInt();
			final int numSheets = buffer.readVarInt();
			
			IEntityPet pet = null;
			Entity foundEnt = PetCommand.GetEntityByUUID(PetCommand.GetProxy().getPlayer().world, petID);
			
			if (foundEnt == null || !(foundEnt instanceof IEntityPet)) {
				return null; // crash
			}
			
			pet = (IEntityPet) foundEnt;
			
			final PlayerEntity player = PetCommand.GetProxy().getPlayer();
			final IPetGUISheet<?>[] sheets = pet.getContainerSheets(player);

			if (numSheets != sheets.length) {
				PetCommand.LOGGER.error("Sheet count differs on client and server for " + pet);
				return null;
			}
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			PetContainer<?> container = new PetContainer(windowId, containerID, pet, player, sheets); 
					//pet.getGUIContainer(PetCommand.GetProxy().getPlayer(), windowId, containerID);
			
			return container;
		}
		
		public static <T extends IEntityPet> IPackedContainerProvider Make(T pet, PlayerEntity player) {
			@SuppressWarnings("unchecked")
			final IPetGUISheet<T>[] sheets = (IPetGUISheet<T>[]) pet.getContainerSheets(player);
			final int key = takeNextKey();
			
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, playerIn) -> {
				final PetContainer<T> container = new PetContainer<T>(windowId, key, pet, playerIn, sheets);
				PetGUI.register(container, key);
				return container;
			}, (buffer) -> {
				buffer.writeUniqueId(pet.getPetID());
				buffer.writeVarInt(key);
				buffer.writeVarInt(sheets.length);
			});
		}
		
		@Override
		public Container getContainer() {
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
		public boolean canInteractWith(PlayerEntity playerIn) {
			if (pet == null) {
				// Pet hasn't been synced yet
				return false;
			}
			return playerIn.equals(pet.getOwner());
		}

		// Caution: This assumes only one player has these open!
		@Override
		public void onContainerClosed(PlayerEntity playerIn) {
			if (this.getCurrentSheet() != null) {
				this.getCurrentSheet().hideSheet(pet, player, this);
			}
			revoke(this.id);
		}
		
		@Override
		public @Nonnull ItemStack transferStackInSlot(PlayerEntity playerIn, int fromSlot) {
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
			this.inventorySlots.clear();
			// this.inventoryItemStacks.clear(); // uh oh? TODO does not having this cause pr oblems?
		}
		
		@Override
		public void dropContainerInventory(IInventory inv) {
			this.clearContainer(player, player.world, inv);
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
		protected void handle(CompoundNBT nbt) {
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
					&& pet.getOwner() instanceof PlayerEntity
					&& ((PlayerEntity) pet.getOwner()).isCreative()) {
					// Reset container sheet. The client will send this as well later.
					this.setSheet(0);
					((IRerollablePet) pet).rerollStats();
				}
				break;
			}
		}
		
		@Override
		public void sendSheetMessageToServer(CompoundNBT data) {
			NetworkHelper.ClientSendSheetData(id, data);
		}
		
		@Override
		public void sendSheetMessageToClient(CompoundNBT data) {
			NetworkHelper.ServerSendSheetData((ServerPlayerEntity) this.player, data);
		}
		
	}
	
	public static int GUI_SHEET_WIDTH = 246;
	public static int GUI_SHEET_HEIGHT = 191;
	
	
	@OnlyIn(Dist.CLIENT)
	public static class PetGUIContainer<T extends IEntityPet> extends AutoGuiContainer<PetContainer<T>> {
		
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

			protected void drawSlotRaw(MatrixStack matrixStackIn, int width, int height, int x, int y) {
				Screen.blit(matrixStackIn, x, y,
						GUI_TEX_CELL_HOFFSET, GUI_TEX_CELL_VOFFSET, 
						width, height,
						GUI_TEX_WIDTH, GUI_TEX_HEIGHT);
			}

			@Override
			protected void drawSingleSlot(MatrixStack matrixStackIn, int width, int height) {
				final Minecraft mc = Minecraft.getInstance();
				mc.getTextureManager().bindTexture(PetGUI.PetGUIContainer.TEXT);
				this.drawSlotRaw(matrixStackIn, width, height, 0, 0);
			}

			@Override
			protected void drawSlots(MatrixStack matrixStackIn, int width, int height, int count, int columns) {
				final Minecraft mc = Minecraft.getInstance();
				mc.getTextureManager().bindTexture(PetGUI.PetGUIContainer.TEXT);
				
				for (int i = 0; i < count; i++) {
					final int x = width * (i % columns);
					final int y = height * (i / columns);
					this.drawSlotRaw(matrixStackIn, width, height, x, y);
				}
			}
		}
		
		//private static int GUI_OPEN_ANIM_TIME = 20 * 1;
		
		private PetContainer<T> container;
		private final PetGUIStatAdapter<T> adapter;
		
		//private int openTicks;
		
		@SuppressWarnings("unchecked")
		public PetGUIContainer(PetContainer<T> container, PlayerInventory playerInv, ITextComponent name) {
			super(container, playerInv, name);
			this.container = container;
			this.adapter = (PetGUIStatAdapter<T>) container.pet.getGUIAdapter();
			//this.openTicks = 0;
		}
		
		@Override
		public void init() {
			this.xSize = this.width;
			this.ySize = this.height;
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
		protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			final int GUI_SHEET_HOFFSET = this.width - (GUI_SHEET_WIDTH + GUI_SHEET_NHOFFSET);
			final int GUI_SHEET_BUTTON_HOFFSET = GUI_SHEET_HOFFSET;
			
			if (this.container.pet == null) {
				drawCenteredString(matrixStackIn, font, "Waiting for server...", this.width / 2, this.height / 2, 0XFFAAAAAA);
				return;
			}
			
			// Draw top-left preview
			{
				AbstractGui.fill(matrixStackIn, 0, 0, GUI_LENGTH_PREVIEW, GUI_LENGTH_PREVIEW, 0xFF283D2A);
				
				int xPosition = GUI_LENGTH_PREVIEW / 2;
				int yPosition = GUI_LENGTH_PREVIEW / 2;
				RenderHelper.disableStandardItemLighting();
				InventoryScreen.drawEntityOnScreen(
						xPosition,
						(int) (GUI_LENGTH_PREVIEW * .75f),
						(int) (GUI_LENGTH_PREVIEW * .2),
						(float) (xPosition) - mouseX,
						(float) (-yPosition) - mouseY,
						(LivingEntity) container.pet);
			}
			
			// Move everything forward ahead of the drawn entity
			// Can't just move entity back cause there's a GRAY plane drawn at just below 0 Z
			matrixStackIn.push();
			matrixStackIn.translate(0, 0, 51);
			
			// Black background (not overlapping preview)
			{
				AbstractGui.fill(matrixStackIn, 0, GUI_LENGTH_PREVIEW, width, height, 0xFF000000);
				AbstractGui.fill(matrixStackIn, GUI_LENGTH_PREVIEW, 0, width, GUI_LENGTH_PREVIEW, 0xFF000000);
			}
			
			// Draw stats and stuff
			{
				//AbstractGui.fill(GUI_INFO_HOFFSET, GUI_INFO_VOFFSET, GUI_SHEET_HOFFSET - 10, height - 10, 0xFF00FFFF);
				
				final int w = (GUI_SHEET_HOFFSET - GUI_SHEET_MARGIN) - (GUI_INFO_HOFFSET * 2);
				int x = GUI_INFO_HOFFSET;
				int y = GUI_INFO_VOFFSET;
				//final int w = 125;
				final int h = 14;
				final int centerX = GUI_SHEET_HOFFSET / 2;
				
				// Health
				{
					drawCenteredString(matrixStackIn, this.font, TextFormatting.BOLD + adapter.getHealthLabel(container.pet), centerX, y, 0xFFFFFFFF);
					y += font.FONT_HEIGHT + 5;
					AbstractGui.fill(matrixStackIn, x, y, x + w, y + h, 0xFFD0D0D0);
					AbstractGui.fill(matrixStackIn, x + 1, y + 1, x + w - 1, y + h - 1, 0xFF201010);
					
					int prog = (int) ((float) (w - 2) * (adapter.getHealth(container.pet) / adapter.getMaxHealth(container.pet)));
					AbstractGui.fill(matrixStackIn, x + 1, y + 1, x + 1 + prog, y + h - 1, 0xFFA02020);
					
					drawCenteredString(matrixStackIn, font,
							String.format("%d / %d", (int) adapter.getHealth(container.pet), (int) adapter.getMaxHealth(container.pet)),
							centerX,
							y + (h / 2) - (font.FONT_HEIGHT / 2),
							0xFFC0C0C0);
					
					y += h + 10;
				}
				
				// Secondary
				if (adapter.supportsSecondaryAmt(container.pet) && adapter.getMaxSecondaryAmt(container.pet) > 0) {
					drawCenteredString(matrixStackIn, this.font, TextFormatting.BOLD + adapter.getSecondaryLabel(container.pet), centerX, y, 0xFFFFFFFF);
					y += font.FONT_HEIGHT + 5;
					AbstractGui.fill(matrixStackIn, x, y, x + w, y + h, 0xFFD0D0D0);
					AbstractGui.fill(matrixStackIn, x + 1, y + 1, x + w - 1, y + h - 1, 0xFF101020);
					
					int prog = (int) ((float) (w - 2) * (adapter.getSecondaryAmt(container.pet) / adapter.getMaxSecondaryAmt(container.pet)));
					AbstractGui.fill(matrixStackIn, x + 1, y + 1, x + 1 + prog, y + h - 1, 0xFF2020A0);
					
					drawCenteredString(matrixStackIn, font,
							String.format("%d / %d", (int) adapter.getSecondaryAmt(container.pet), (int) adapter.getMaxSecondaryAmt(container.pet)),
							centerX,
							y + (h / 2) - (font.FONT_HEIGHT / 2),
							0xFFC0C0C0);
					
					y += h + 10;
				}
				
				// Tertiary
				if (adapter.supportsTertiaryAmt(container.pet) && adapter.getMaxTertiaryAmt(container.pet) > 0) {
					final float cur = adapter.getTertiaryAmt(container.pet);
					final float max = adapter.getMaxTertiaryAmt(container.pet);
					
					drawCenteredString(matrixStackIn, this.font, TextFormatting.BOLD + adapter.getTertiaryLabel(container.pet), centerX, y, 0xFFFFFFFF);
					y += font.FONT_HEIGHT + 5;
					AbstractGui.fill(matrixStackIn, x, y, x + w, y + h, 0xFFD0D0D0);
					AbstractGui.fill(matrixStackIn, x + 1, y + 1, x + w - 1, y + h - 1, 0xFF201020);
					
					int prog = (int) ((float) (w - 2) * (cur/max));
					AbstractGui.fill(matrixStackIn, x + 1, y + 1, x + 1 + prog, y + h - 1, 0xFFA020A0);
					
					drawCenteredString(matrixStackIn, font,
							String.format("%.2f%%", (cur/max) * 100f),
							centerX,
							y + (h / 2) - (font.FONT_HEIGHT / 2),
							cur >= max ? 0xFFC0FFC0 : 0xFFC0C0C0);
					
//					if (container.pet.isSoulBound()) {
//						drawCenteredString(font,
//								"Soulbound",
//								centerX,
//								y + (h / 2) - (font.FONT_HEIGHT / 2),
//								0xFF40FF40);
//					} else {
//						drawCenteredString(font,
//								String.format("%.2f%%", bond * 100f),
//								centerX,
//								y + (h / 2) - (font.FONT_HEIGHT / 2),
//								bond == 1f ? 0xFFC0FFC0 : 0xFFC0C0C0);
//					}
					
					
					y += h + 10;
				}
				
				// XP
				if (adapter.supportsQuaternaryAmt(container.pet) && adapter.getMaxQuaternaryAmt(container.pet) > 0) {
					final float cur = adapter.getQuaternaryAmt(container.pet);
					final float max = adapter.getMaxQuaternaryAmt(container.pet);
					drawCenteredString(matrixStackIn, this.font, TextFormatting.BOLD + adapter.getQuaternaryLabel(container.pet), centerX, y, 0xFFFFFFFF);
					y += font.FONT_HEIGHT + 5;
					AbstractGui.fill(matrixStackIn, x, y, x + w, y + h, 0xFFD0D0D0);
					AbstractGui.fill(matrixStackIn, x + 1, y + 1, x + w - 1, y + h - 1, 0xFF102010);
					
					int prog = (int) ((float) (w - 2) * (cur / max));
					AbstractGui.fill(matrixStackIn, x + 1, y + 1, x + 1 + prog, y + h - 1, 0xFF20A020);
					
					drawCenteredString(matrixStackIn, font,
							String.format("%d / %d", (int) cur, (int) max),
							centerX,
							y + (h / 2) - (font.FONT_HEIGHT / 2),
							0xFFC0C0C0);
					
					y += h + 10;
				}
			}
			
			if (container.getSheets().size() > 0) {
				int x = GUI_SHEET_BUTTON_HOFFSET;
				
				for (IPetGUISheet<T> sheet : container.getSheets()) {
					AbstractGui.fill(matrixStackIn, x, GUI_SHEET_BUTTON_VOFFSET, x + GUI_SHEET_BUTTON_WIDTH, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT, 0xFFFFFFFF);
					AbstractGui.fill(matrixStackIn, x + 1, GUI_SHEET_BUTTON_VOFFSET + 1, x + GUI_SHEET_BUTTON_WIDTH - 1, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT - 1, 0xFF202020);
					
					if (sheet == container.getCurrentSheet()) {
						AbstractGui.fill(matrixStackIn, x, GUI_SHEET_BUTTON_VOFFSET, x + GUI_SHEET_BUTTON_WIDTH, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT, 0x40FFFFFF);
					}
					
					if (mouseX >= x && mouseX <= x + GUI_SHEET_BUTTON_WIDTH && mouseY >= GUI_SHEET_BUTTON_VOFFSET && mouseY <= GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT) {
						AbstractGui.fill(matrixStackIn, x, GUI_SHEET_BUTTON_VOFFSET, x + GUI_SHEET_BUTTON_WIDTH, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT, 0x40FFFFFF);
					}
					
					String text = sheet.getButtonText();
					int strLen = font.getStringWidth(text);
					int strHeight = font.FONT_HEIGHT;
					font.drawString(matrixStackIn, text, x + (GUI_SHEET_BUTTON_WIDTH / 2) - (strLen / 2), GUI_SHEET_BUTTON_VOFFSET + (GUI_SHEET_BUTTON_HEIGHT / 2) - (strHeight / 2), 0xFFFFFFFF);
					x += GUI_SHEET_BUTTON_WIDTH;
				}
				
				if (container.supportsReroll() && PetCommand.GetProxy().getPlayer().isCreative()) {
					AbstractGui.fill(matrixStackIn, x, GUI_SHEET_BUTTON_VOFFSET, x + GUI_SHEET_BUTTON_WIDTH, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT, 0xFFFFDDFF);
					AbstractGui.fill(matrixStackIn, x + 1, GUI_SHEET_BUTTON_VOFFSET + 1, x + GUI_SHEET_BUTTON_WIDTH - 1, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT - 1, 0xFF702070);
					
					if (mouseX >= x && mouseX <= x + GUI_SHEET_BUTTON_WIDTH && mouseY >= GUI_SHEET_BUTTON_VOFFSET && mouseY <= GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT) {
						AbstractGui.fill(matrixStackIn, x, GUI_SHEET_BUTTON_VOFFSET, x + GUI_SHEET_BUTTON_WIDTH, GUI_SHEET_BUTTON_VOFFSET + GUI_SHEET_BUTTON_HEIGHT, 0x40FFFFFF);
					}
					
					String text = "Reroll";
					int strLen = font.getStringWidth(text);
					int strHeight = font.FONT_HEIGHT;
					font.drawString(matrixStackIn, text, x + (GUI_SHEET_BUTTON_WIDTH / 2) - (strLen / 2), GUI_SHEET_BUTTON_VOFFSET + (GUI_SHEET_BUTTON_HEIGHT / 2) - (strHeight / 2), 0xFFFFFFFF);
					x += GUI_SHEET_BUTTON_WIDTH;
				}
			}
			
			mc.getTextureManager().bindTexture(TEXT);
			
			// Draw sheet
			IPetGUISheet<T> sheet = container.getCurrentSheet();
			if (sheet != null) {
				matrixStackIn.push();
				matrixStackIn.translate(GUI_SHEET_HOFFSET, GUI_SHEET_VOFFSET, 0);
				
				RenderSystem.enableAlphaTest();
				RenderSystem.enableBlend();
				RenderSystem.color4f(1f, 1f, 1f, 1f);
				blit(matrixStackIn, -GUI_SHEET_MARGIN, -GUI_SHEET_MARGIN, 0, 0, GUI_SHEET_WIDTH + (GUI_SHEET_MARGIN * 2), GUI_SHEET_HEIGHT + (GUI_SHEET_MARGIN * 2), GUI_TEX_WIDTH, GUI_TEX_HEIGHT);
				
				sheet.draw(matrixStackIn, Minecraft.getInstance(), partialTicks, GUI_SHEET_WIDTH,
						GUI_SHEET_HEIGHT, mouseX - GUI_SHEET_HOFFSET, mouseY - GUI_SHEET_VOFFSET);
				matrixStackIn.pop();
			}
			
			matrixStackIn.pop();
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(MatrixStack matrixStackIn, int mouseX, int mouseY) {
			//super.drawGuiContainerForegroundLayer(matrixStackIn, mouseX, mouseY);
			
			final int GUI_SHEET_HOFFSET = this.width - (GUI_SHEET_WIDTH + GUI_SHEET_NHOFFSET);
			
			IPetGUISheet<T> sheet = container.getCurrentSheet();
			if (sheet != null) {
				matrixStackIn.push();
				matrixStackIn.translate(GUI_SHEET_HOFFSET, GUI_SHEET_VOFFSET, 0);
				
				RenderSystem.enableAlphaTest();
				RenderSystem.enableBlend();
				
				sheet.overlay(matrixStackIn, Minecraft.getInstance(), 0f, GUI_SHEET_WIDTH,
						GUI_SHEET_HEIGHT, mouseX - GUI_SHEET_HOFFSET, mouseY - GUI_SHEET_VOFFSET);
				matrixStackIn.pop();
			}
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
			
			if (!container.canInteractWith(PetCommand.GetProxy().getPlayer())) {
				return false;
			}
			
			// Only allow custom clicking s tuff if there isn't an item being held
			if (PetCommand.GetProxy().getPlayer().inventory.getItemStack().isEmpty()) {
			
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
		
		private static void clientSendInternal(int id, CompoundNBT nbt) {
			PetGUIControlMessage message = new PetGUIControlMessage(id, nbt);
			
			NetworkHandler.sendToServer(message);
		}
		
		private static void serverSendInternal(ServerPlayerEntity player, CompoundNBT nbt) {
			PetGUISyncMessage message = new PetGUISyncMessage(nbt);
			
			NetworkHandler.sendTo(message, player);
		}
		
		private static CompoundNBT base(PetContainerMessageType type) {
			CompoundNBT nbt = new CompoundNBT();
			nbt.putString(NBT_TYPE, type.getKey());
			return nbt;
		}
		
		public static void ClientSendSheet(int id, int sheet) {
			CompoundNBT nbt = base(PetContainerMessageType.SET_SHEET);
			nbt.putInt(NBT_INDEX, sheet);
			
			clientSendInternal(id, nbt);
		}
		
		public static void ClientSendSheetData(int id, CompoundNBT data) {
			CompoundNBT nbt = base(PetContainerMessageType.SHEET_DATA);
			nbt.put(NBT_USERDATA, data);
			
			clientSendInternal(id, nbt);
		}
		
		public static void ClientSendReroll(int id) {
			CompoundNBT nbt = base(PetContainerMessageType.REROLL);
			
			clientSendInternal(id, nbt);
		}
		
		public static void ServerSendSheetData(ServerPlayerEntity player, CompoundNBT data) {
			CompoundNBT nbt = base(PetContainerMessageType.SHEET_DATA);
			nbt.put(NBT_USERDATA, data);
			
			serverSendInternal(player, nbt);
		}
		
		
		public static PetContainerMessageType GetType(CompoundNBT nbt) {
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
		
		public static int GetSendSheetIndex(CompoundNBT nbt) {
			return nbt.getInt(NBT_INDEX);
		}
		
		public static CompoundNBT GetSendSheetData(CompoundNBT nbt) {
			return nbt.getCompound(NBT_USERDATA);
		}
		
	}
	
}