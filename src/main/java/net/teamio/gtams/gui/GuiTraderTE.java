package net.teamio.gtams.gui;

import java.io.IOException;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.teamio.gtams.client.Offer;

public class GuiTraderTE extends GuiContainer {

	private final ContainerTraderTE container;
	private GuiScrollingList availableOffers;

	public GuiTraderTE(ContainerTraderTE container) {
		super(container);
		this.container = container;
	}

	@Override
	public void initGui() {
		super.initGui();

		container.requestOffers();

		int listWidth = this.xSize - 40;
		int listHeight = this.ySize - 40;
		int left = guiLeft + 20;
		int top = guiTop + 20;
		int bottom = top + listHeight;
		int lineHeight = 40;

		availableOffers = new GuiScrollingList(Minecraft.getMinecraft(), listWidth, listHeight, top, bottom, left, lineHeight, this.width, this.height) {

			int selected = -1;

			@Override
			protected boolean isSelected(int index) {
				return index == selected;
			}

			@Override
			protected int getSize() {
				return container.offers == null || container.offers.isEmpty() ? 100 : container.offers.size();
			}

			@Override
			protected void elementClicked(int index, boolean doubleClick) {
				selected = index;
			}

			@Override
			protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
				if (container.offers == null) {
					drawString(fontRendererObj, "Waiting for offer data", left, slotTop, 0xFF0000);
				} else if (container.offers.isEmpty()) {
					drawString(fontRendererObj, "No Offers Available", left, slotTop, 0xFFFF00);
				} else {

					Offer offer = container.offers.get(slotIdx);
					drawString(fontRendererObj, offer.itemName + ":" + Integer.toString(offer.damage), left, slotTop, 0xFFFFFF);
					drawString(fontRendererObj, "Some text here", left, slotTop + 20, 0xFFFFFF);
				}
			}

			@Override
			protected void drawBackground() {
//				System.out.println("Draw Background" + left + " " + top + " " + right + " " + bottom);
				drawGradientRect(left, top, 100, 100, 0x000000, 0x303030);
				drawString(fontRendererObj, "This is just background", 20, 20, 0xFF00FF);
			}
		};
		availableOffers.registerScrollButtons(buttonList, 5, 6);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
//		drawString(fontRendererObj, "No Offers Available", 20, 20, 0xFFFF00);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (availableOffers != null) {
			availableOffers.drawScreen(mouseX, mouseY, partialTicks);
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();

		if (availableOffers != null) {
			int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
			int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

			availableOffers.handleMouseInput(mouseX, mouseY);
		}
	}

}
