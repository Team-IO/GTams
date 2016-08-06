package net.teamio.gtams.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.teamio.gtams.client.Goods;
import net.teamio.gtams.client.Mode;
import net.teamio.gtams.client.Trade;
import net.teamio.gtams.client.TradeDescriptor;
import net.teamio.gtams.client.TradeInfo;
import net.teamio.gtams.gui.ContainerTraderTE.SlotChangeListener;

public class GuiTraderTE extends GuiContainer {

	private static class NumericField extends GuiTextField {
		private NumericField(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width,
				int par6Height) {
			super(componentId, fontrendererObj, x, y, par5Width, par6Height);
		}

		@Override
		public void writeText(String textToWrite) {
			System.out.println(textToWrite);
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < textToWrite.length(); i++) {
				char ch = textToWrite.charAt(i);
				if(ch >= '0' && ch <= '9') {
					sb.append(ch);
				}
			}
			super.writeText(sb.toString());
			onTextChanged(getText());
		}

		@Override
		public void setText(String textIn) {
			super.setText(textIn);
			onTextChanged(getText());
		}

		@Override
		public void deleteFromCursor(int num) {
			super.deleteFromCursor(num);
			onTextChanged(getText());
		}

		public int getIntValue() {
			String txt = getText();
			if(txt.isEmpty()) {
				return 0;
			}
			return Integer.parseInt(txt);
		}

		protected void onTextChanged(String text) {
		}
	}
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
			List<Trade> offers = container.getTrades();
			return offers == null || offers.isEmpty() ? 1 : offers.size();
		}

		@Override
		protected void elementClicked(int index, boolean doubleClick) {
			selected = index;
		}

		@Override
		protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
			List<Trade> offers = container.getTrades();
			List<ItemStack> offerStacks = container.getTradeStacks();
			if (offers == null) {
				drawString(fontRendererObj, "Waiting for trade data", left, slotTop, 0xFF0000);
			} else if (offers.isEmpty()) {
				drawString(fontRendererObj, "No Active Trades", left, slotTop, 0xFFFF00);
			} else {
				Trade offer = offers.get(slotIdx);
				ItemStack itemStack = offerStacks.get(slotIdx);
				if (itemStack != null) {
					RenderHelper.enableGUIStandardItemLighting();
					GlStateManager.enableDepth();
					itemRender.renderItemAndEffectIntoGUI(mc.thePlayer, itemStack, left, slotTop);
					itemRender.renderItemOverlayIntoGUI(fontRendererObj, itemStack, left, slotTop, null);
					RenderHelper.disableStandardItemLighting();
				}
				if(offer.descriptor == null) {
					drawString(fontRendererObj, "Invalid Data", left + 16, slotTop, 0xFFFF00);
				} else {
					if (itemStack == null) {
						drawString(fontRendererObj, "Invalid Data", left + 16, slotTop, 0xFFFF00);
					} else {
						drawString(fontRendererObj, itemStack.getDisplayName(), left + 16, slotTop, 0xFFFFFF);
					}
					drawString(fontRendererObj, offer.descriptor.toString(), left + 16, slotTop + 10, 0xFFFFFF);
				}
				drawString(fontRendererObj, "Some text here", left + 16, slotTop + 20, 0xFFFFFF);
			}
		}

		@Override
		protected void drawBackground() {
		}
	}

	public static ResourceLocation debug_gui = new ResourceLocation("gtams", "textures/gui/debug_gui.png");
	public static ResourceLocation debug_gui_settings = new ResourceLocation("gtams", "textures/gui/debug_gui_settings.png");

	private static final int TXT_PRICE = 4001;
	private static final int TXT_INTERVAL = 4002;
	private static final int TXT_STOP_AFTER = 4003;

	private static final int BTN_NEWTRADE = 5001;
	private static final int BTN_CANCEL = 5002;
	private static final int BTN_CREATETRADE = 5003;
	private static final int BTN_MODE_ONCE = 5004;
	private static final int BTN_MODE_RECURRING = 5005;
	private static final int BTN_MODE_INFINITE = 5006;

	private static final int CB_BUY = 6001;
	private static final int CB_SELL = 6002;

	private final ContainerTraderTE container;

	private List<GuiTextField> textFields = new ArrayList<GuiTextField>();

	private OfferList availableOffers;
	private GuiButton btnNewTrade;
	private GuiButton btnCancel;
	private boolean isEditingTrade = false;

	private GuiButton btnCreateTrade;
	private NumericField txtPrice;
	private NumericField txtInterval;
	private NumericField txtStopAfter;
	private GuiCheckBox cbBuy;
	private GuiCheckBox cbSell;
	private GuiCheckBox cbModeOnce;
	private GuiCheckBox cbModeRecurring;
	private GuiCheckBox cbModeInfinite;

	private ItemStack tradeStack;
	private Mode mode = Mode.Once;
	private boolean isBuy;
	private int price;
	private int amount = 1;
	private int interval = 1;
	private int stopAfter = 0;
	private NumericField txtAmount;

	public GuiTraderTE(ContainerTraderTE conta) {
		super(conta);
		this.container = conta;
		container.onSlotChange = new SlotChangeListener() {

			@Override
			public void slotChanged(ItemStack newStack) {
				tradeStack = newStack;
				if(tradeStack != null) {
					tradeStack.stackSize = 1;
					container.requestTradeInfo(tradeStack);
				}
				updateVisibility();
			}
		};
		setGuiSize(256, 256);
	}

	@Override
	public void initGui() {
		textFields.clear();

		this.xSize = 256;
		this.ySize = 256;
		super.initGui();

		container.requestTrades();

		int listWidth = this.xSize - 16;
		int listHeight = 129;
		int left = guiLeft + 8;
		int top = guiTop + 29;
		int bottom = top + listHeight;
		int lineHeight = 40;

		availableOffers = new OfferList(Minecraft.getMinecraft(), listWidth, listHeight, top, bottom, left, lineHeight, this.width, this.height);
		availableOffers.registerScrollButtons(buttonList, 5, 6);

		btnNewTrade = new GuiButton(BTN_NEWTRADE, guiLeft + 190, guiTop + 5, 60, 20, "New Trade");
		btnCancel = new GuiButton(BTN_CANCEL, guiLeft + 5, guiTop + 5, 50, 20, "Cancel");
		buttonList.add(btnCancel);
		buttonList.add(btnNewTrade);

		txtPrice = new NumericField(TXT_PRICE, fontRendererObj, guiLeft + 75, guiTop + 116, 40, 10) {
			@Override
			protected void onTextChanged(String text) {
				updateVisibility();
			}
		};
		txtPrice.setMaxStringLength(5);

		txtAmount = new NumericField(TXT_PRICE, fontRendererObj, guiLeft + 75, guiTop + 126, 40, 10) {
			@Override
			protected void onTextChanged(String text) {
				updateVisibility();
			}
		};
		txtAmount.setMaxStringLength(5);
		btnCreateTrade = new GuiButton(BTN_CREATETRADE, guiLeft + 166, guiTop + 30, 80, 20, "Create Trade");
		buttonList.add(btnCreateTrade);

		cbBuy = new GuiCheckBox(CB_BUY, guiLeft + 10, guiTop + 110, "Buy", false);
		cbSell = new GuiCheckBox(CB_SELL, guiLeft + 10, guiTop + 120, "Sell", true);

		buttonList.add(cbBuy);
		buttonList.add(cbSell);

		int modeTop = 95;
		int modeLeft = 150;
		cbModeOnce = new GuiCheckBox(BTN_MODE_ONCE, guiLeft + modeLeft, guiTop + modeTop, "One-Time", true);
		cbModeRecurring = new GuiCheckBox(BTN_MODE_RECURRING, guiLeft + modeLeft, guiTop + modeTop + 10, "Recurring", false);
		cbModeInfinite = new GuiCheckBox(BTN_MODE_INFINITE, guiLeft + modeLeft, guiTop + modeTop + 20, "Infinite", false);

		int configTop = 130;
		int configLeft = 135;

		txtInterval = new NumericField(TXT_INTERVAL, fontRendererObj, guiLeft + configLeft, guiTop + configTop, 26, 10) {
			@Override
			protected void onTextChanged(String text) {
				updateVisibility();
			}
		};
		txtInterval.setMaxStringLength(3);
		txtStopAfter = new NumericField(TXT_STOP_AFTER, fontRendererObj, guiLeft + configLeft, guiTop + configTop + 15, 26, 10) {
			@Override
			protected void onTextChanged(String text) {
				updateVisibility();
			}
		};
		txtStopAfter.setMaxStringLength(3);

		textFields.add(txtPrice);
		textFields.add(txtAmount);
		textFields.add(txtInterval);
		textFields.add(txtStopAfter);

		buttonList.add(cbModeOnce);
		buttonList.add(cbModeRecurring);
		buttonList.add(cbModeInfinite);

		txtPrice.setText(Integer.toString(price));
		txtAmount.setText(Integer.toString(amount));
		txtInterval.setText(Integer.toString(interval));
		txtStopAfter.setText(Integer.toString(stopAfter));

		updateVisibility();
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		txtPrice.updateCursorCounter();
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if(button.id == BTN_NEWTRADE) {
			System.out.println("New Trade");
			isEditingTrade = true;

			tradeStack = null;
			container.tradeInfo = null;

		}
		if(button.id == BTN_CANCEL) {
			System.out.println("Cancel New Trade");
			isEditingTrade = false;

			tradeStack = null;
			container.tradeInfo = null;
		}

		if(button.id == CB_BUY) {
			cbSell.setIsChecked(!cbBuy.isChecked());
		}

		if(button.id == CB_SELL) {
			cbBuy.setIsChecked(!cbSell.isChecked());
		}

		if(button.id == BTN_MODE_ONCE) {
			mode = Mode.Once;
		}
		if(button.id == BTN_MODE_RECURRING) {
			mode = Mode.Recurring;
		}
		if(button.id == BTN_MODE_INFINITE) {
			mode = Mode.Infinite;
		}
		if(button.id == BTN_CREATETRADE) {
			Trade newTrade = new Trade();

			newTrade.descriptor = new TradeDescriptor(tradeStack);

			newTrade.isBuy = isBuy;
			newTrade.price = price;
			newTrade.mode = mode;
			newTrade.interval = interval;
			newTrade.stopAfter = stopAfter;
			newTrade.amount = amount;

			container.requestCreateTrade(newTrade);
			isEditingTrade = false;

			tradeStack = null;
		}

		updateVisibility();
	}

	private void updateVisibility() {

		btnNewTrade.visible = !isEditingTrade;
		btnNewTrade.enabled = !isEditingTrade;

		btnCancel.visible = isEditingTrade;
		btnCancel.enabled = isEditingTrade;

		btnCreateTrade.visible = isEditingTrade;
		btnCreateTrade.enabled = isEditingTrade;

		availableOffers.visible = !isEditingTrade;
		txtPrice.setVisible(isEditingTrade);
		txtAmount.setVisible(isEditingTrade);

		txtInterval.setVisible(isEditingTrade && mode == Mode.Recurring);
		txtStopAfter.setVisible(isEditingTrade && mode == Mode.Recurring);

		cbBuy.enabled = isEditingTrade;
		cbBuy.visible = isEditingTrade;
		cbSell.enabled = isEditingTrade;
		cbSell.visible = isEditingTrade;

		cbModeOnce.enabled = isEditingTrade && mode != Mode.Once;
		cbModeOnce.visible = isEditingTrade;
		cbModeOnce.setIsChecked(!cbModeOnce.enabled);
		cbModeRecurring.enabled = isEditingTrade && mode != Mode.Recurring;
		cbModeRecurring.visible = isEditingTrade;
		cbModeRecurring.setIsChecked(!cbModeRecurring.enabled);
		cbModeInfinite.enabled = isEditingTrade && mode != Mode.Infinite;
		cbModeInfinite.visible = isEditingTrade;
		cbModeInfinite.setIsChecked(!cbModeInfinite.enabled);

		isBuy = cbBuy.isChecked();
		this.price = txtPrice.getIntValue();
		this.amount = txtAmount.getIntValue();
		this.interval = txtInterval.getIntValue();
		this.stopAfter = txtStopAfter.getIntValue();

		btnCreateTrade.enabled = this.price > 0 && (mode != Mode.Recurring || interval > 0) && tradeStack != null && amount > 0;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		if(isEditingTrade) {
			mc.renderEngine.bindTexture(debug_gui_settings);
		} else {
			mc.renderEngine.bindTexture(debug_gui);
		}
		drawTexturedModalRect(this.width / 2 - 128, this.height / 2 - 128, 0, 0, 256, 256);
		//TODO: Translate
		drawString(fontRendererObj, "Inventory", guiLeft + 8, guiTop + 162, 0xFFFFFF);
		if(!isEditingTrade) {
			drawString(fontRendererObj, "Active Trades:", guiLeft + 8, guiTop + 16, 0xFFFFFF);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		int offsetX = 10;
		int offsetY = 30;
		if(isEditingTrade) {

			if(tradeStack != null) {
				TradeInfo tradeInfo = container.tradeInfo;


				RenderHelper.enableGUIStandardItemLighting();
				GlStateManager.enableDepth();
				itemRender.renderItemAndEffectIntoGUI(mc.thePlayer, tradeStack, 10, 31);
				itemRender.renderItemOverlayIntoGUI(fontRendererObj, tradeStack, 10, 31, null);
				RenderHelper.disableStandardItemLighting();

				if(tradeInfo == null) {
					drawString(fontRendererObj, "Requesting Trade Information...", 40, offsetY, 0xFFFF00);
				} else {
					TradeDescriptor trade = tradeInfo.trade;
					drawString(fontRendererObj, trade.itemName + ":" + Integer.toString(trade.damage), offsetX + 20, offsetY, 0xFFFFFF);
					drawString(fontRendererObj, "Supply/Demand " + Float.toString(Math.round(tradeInfo.supplyDemandFactor * 100) / 100f) + "%", offsetX, offsetY + 20, 0xFFFFFF);
					drawString(fontRendererObj, "Statistics from last period:", offsetX, offsetY + 38, 0xFFFF00);
					drawString(fontRendererObj, "Trade Volume: " + Integer.toString(tradeInfo.volumeLastPeriod), offsetX, offsetY + 46, 0xFFFFFF);
					drawString(fontRendererObj, "Trade Count: " + Integer.toString(tradeInfo.tradesLastPeriod), offsetX, offsetY + 54, 0xFFFFFF);
					drawString(fontRendererObj, "Mean Price: " + Float.toString(Math.round(tradeInfo.meanPrice * 100) / 100f), offsetX, offsetY + 62, 0xFFFFFF);
				}
			}
			drawTextFieldLabel(txtPrice, "Price");
			drawTextFieldLabel(txtAmount, "Amount");
			drawTextFieldLabel(txtInterval, "Every", "seconds");
			drawTextFieldLabel(txtStopAfter, "Stop after", "transactions");
		}
		offsetX = 175;
		offsetY = 174;

		List<Goods> goods = container.getGoods();
		List<ItemStack> goodsStacks = container.getGoodsStacks();
		if(goods == null) {
			drawString(fontRendererObj, "Requesting Goods Information...", offsetX, offsetY, 0xFFFF00);
		} else {
			RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.enableDepth();
			for(int i = 0; i < goods.size(); i++) {
				ItemStack goodsStack = goodsStacks.get(i);
				itemRender.renderItemAndEffectIntoGUI(mc.thePlayer, goodsStack, offsetX + i*18, offsetY);
				itemRender.renderItemOverlayIntoGUI(fontRendererObj, goodsStack, offsetX + i*18, offsetY, null);
			}
			RenderHelper.disableStandardItemLighting();
		}
	}

	private void drawTextFieldLabel(GuiTextField textField, String labelBefore, String labelAfter) {
		if(!textField.getVisible()) {
			return;
		}
		int colorBefore = 0xFFFFFF;
		int colorAfter = 0xFFFFFF;

		int yPosition = textField.yPosition - guiTop;
		int xPosition = textField.xPosition - guiLeft;

		drawString(fontRendererObj, labelBefore, xPosition - fontRendererObj.getStringWidth(labelBefore) - 2, yPosition,colorBefore);
		drawString(fontRendererObj, labelAfter, xPosition + textField.width + 2, yPosition, colorAfter);
	}

	private void drawTextFieldLabel(GuiTextField textField, String labelBefore) {
		if(!textField.getVisible()) {
			return;
		}
		int colorBefore = 0xFFFFFF;

		int yPosition = textField.yPosition - guiTop;
		int xPosition = textField.xPosition - guiLeft;

		drawString(fontRendererObj, labelBefore, xPosition - fontRendererObj.getStringWidth(labelBefore) - 2, yPosition,colorBefore);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (availableOffers != null && availableOffers.visible) {
			availableOffers.drawScreen(mouseX, mouseY, partialTicks);
		}
		for(GuiTextField txt : textFields) {
			if(txt.getVisible()) {
				txt.drawTextBox();
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

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		for(GuiTextField txt : textFields) {
			if(txt.getVisible()) {
				txt.mouseClicked(mouseX, mouseY, mouseButton);
			}
		}
	}
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		for(GuiTextField txt : textFields) {
			if(txt.getVisible()) {
				txt.textboxKeyTyped(typedChar, keyCode);
			}
		}
	}
}
