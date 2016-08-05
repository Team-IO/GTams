package net.teamio.gtams.gui;

import java.io.IOException;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.teamio.gtams.client.Offer;
import net.teamio.gtams.client.TradeDescriptor;
import net.teamio.gtams.client.TradeInfo;

public class GuiTraderTE extends GuiContainer {

	private final class OfferList extends GuiScrollingList {
		int selected = -1;
		public boolean visible = true;

		private OfferList(Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight,
				int screenWidth, int screenHeight) {
			super(client, width, height, top, bottom, left, entryHeight, screenWidth, screenHeight);
		}

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
	}

	private final ContainerTraderTE container;
	private OfferList availableOffers;

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

		availableOffers = new OfferList(Minecraft.getMinecraft(), listWidth, listHeight, top, bottom, left, lineHeight, this.width, this.height);
		availableOffers.registerScrollButtons(buttonList, 5, 6);

		newTrade = new GuiButton(BTN_NEWTRADE, 0, 0, 50, 20, "New Trade");
		btnCancel = new GuiButton(BTN_NEWTRADE, 0, 0, 50, 20, "Cancel");
		btnCancel.visible = false;
		buttonList.add(newTrade);
		buttonList.add(btnCancel);
	}

	private static final int BTN_NEWTRADE = 5001;
	private static final int BTN_CANCEL = 5002;
	private GuiButton newTrade;
	private GuiButton btnCancel;
	private boolean creatingNewTrade = false;

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if(button.id == BTN_NEWTRADE) {
			newTrade.visible = false;
			availableOffers.visible = false;
			btnCancel.visible = true;
			container.newTradeSlot.putStack(null);
			container.tradeInfo = null;
			creatingNewTrade = true;
		}
		if(button.id == BTN_CANCEL) {
			newTrade.visible = true;
			availableOffers.visible = true;
			btnCancel.visible = false;
			container.newTradeSlot.putStack(null);
			container.tradeInfo = null;
			creatingNewTrade = false;
		}
	}


	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
//		drawString(fontRendererObj, "No Offers Available", 20, 20, 0xFFFF00);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (availableOffers != null && availableOffers.visible) {
			availableOffers.drawScreen(mouseX, mouseY, partialTicks);
		}
		if(creatingNewTrade) {
			TradeInfo tradeInfo = container.tradeInfo;
			if(tradeInfo == null) {
				drawString(fontRendererObj, "Requesting Trade Information...", 40, 10, 0xFFFF00);
			} else {
				TradeDescriptor trade = tradeInfo.trade;
				drawString(fontRendererObj, trade.itemName + ":" + Integer.toString(trade.damage), 40, 10, 0xFFFFFF);
				drawString(fontRendererObj, "Supply/Demand " + Float.toString(Math.round(tradeInfo.supplyDemandFactor * 100) / 100f) + "%", 40, 20, 0xFFFFFF);
				drawString(fontRendererObj, "Statistics from last period:", 40, 28, 0xFF00FF);
				drawString(fontRendererObj, "Trade Volume: " + Integer.toString(tradeInfo.volumeLastPeriod), 40, 36, 0xFFFFFF);
				drawString(fontRendererObj, "Trade Count: " + Integer.toString(tradeInfo.tradesLastPeriod), 40, 44, 0xFFFFFF);
				drawString(fontRendererObj, "Mean Price: " + Float.toString(Math.round(tradeInfo.meanPrice * 100) / 100f), 40, 52, 0xFFFFFF);
			}
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();

		if (availableOffers != null && availableOffers.visible) {
			int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
			int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

			availableOffers.handleMouseInput(mouseX, mouseY);
		}
	}

}
