package net.teamio.gtams.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
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
import net.teamio.gtams.client.entities2.Goods;
import net.teamio.gtams.client.entities2.Mode;
import net.teamio.gtams.client.entities2.Trade;
import net.teamio.gtams.client.entities2.TradeDescriptor;
import net.teamio.gtams.client.entities2.TradeInfo;
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
			List<Trade> trades = container.getTrades();
			List<ItemStack> tradeStacks = container.getTradeStacks();
			if (trades == null) {
				drawString(fontRendererObj, "Waiting for trade data", left, slotTop, 0xFF0000);
			} else if (trades.isEmpty()) {
				drawString(fontRendererObj, "No Active Trades", left, slotTop, 0xFFFF00);
			} else {
				Trade trade = trades.get(slotIdx);
				ItemStack itemStack = tradeStacks.get(slotIdx);
				if (itemStack != null) {
					RenderHelper.enableGUIStandardItemLighting();
					GlStateManager.enableDepth();
					itemRender.renderItemAndEffectIntoGUI(mc.thePlayer, itemStack, left, slotTop);
					itemRender.renderItemOverlayIntoGUI(fontRendererObj, itemStack, left, slotTop, null);
					RenderHelper.disableStandardItemLighting();
				}
				if(trade.descriptor == null) {
					drawString(fontRendererObj, "Invalid Data", left + 16, slotTop, 0xFFFF00);
				} else {
					if (itemStack == null) {
						drawString(fontRendererObj, "Invalid Data", left + 16, slotTop, 0xFFFF00);
					} else {
						drawString(fontRendererObj, itemStack.getDisplayName(), left + 16, slotTop, 0xFFFFFF);
					}
					drawString(fontRendererObj, trade.descriptor.toString(), left + 16, slotTop + 10, 0xFFFFFF);
				}
				drawString(fontRendererObj, trade.toDisplayString(), left + 16, slotTop + 20, 0xFFFFFF);
			}
		}

		@Override
		protected void drawBackground() {
		}
	}

	public static class Badge {
		int u;
		int v;
		int size;
		int xPos;
		int yPos;
		List<String> hover;
		boolean visible = true;

		public Badge(int xPos, int yPos, int u, int v, int size, List<String> hover) {
			this.u = u;
			this.v = v;
			this.size = size;
			this.xPos = xPos;
			this.yPos = yPos;
			this.hover = hover;
		}

		public boolean contains(int mouseX, int mouseY) {
			return mouseX > xPos && mouseX < xPos + size &&
			mouseY > yPos && mouseY < yPos + size;
		}
	}

	public static ResourceLocation debug_gui = new ResourceLocation("gtams", "textures/gui/debug_gui.png");
	public static ResourceLocation debug_gui_settings = new ResourceLocation("gtams", "textures/gui/debug_gui_settings.png");
	public static ResourceLocation badges_tex = new ResourceLocation("gtams", "textures/gui/badges.png");

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
	private static final int CB_PARTIAL = 6003;

	private final ContainerTraderTE container;

	private List<GuiTextField> textFields = new ArrayList<>();
	private List<Badge> badges = new ArrayList<>();

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
	private boolean allowPartial = true;
	private int price;
	private int amount = 1;
	private int interval = 1;
	private int stopAfter = 0;
	private NumericField txtAmount;
	private Badge badgePrice;
	private Badge badgeAmount;
	private Badge badgeWarning;
	private int refreshTicker;
	private Badge badgeFunds;
	private Badge badgeDebug;
	private GuiCheckBox cbPartial;

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
		badges.clear();

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

		btnNewTrade = new GuiButton(BTN_NEWTRADE, guiLeft + 188, guiTop + 5, 60, 20, "New Trade");
		btnCancel = new GuiButton(BTN_CANCEL, btnNewTrade.xPosition - 55, guiTop + 5, 50, 20, "Cancel");
		buttonList.add(btnCancel);
		buttonList.add(btnNewTrade);

		txtPrice = new NumericField(TXT_PRICE, fontRendererObj, guiLeft + 75, guiTop + 136, 40, 10) {
			@Override
			protected void onTextChanged(String text) {
				updateVisibility();
			}
		};
		txtPrice.setMaxStringLength(5);

		txtAmount = new NumericField(TXT_PRICE, fontRendererObj, guiLeft + 75, guiTop + 146, 40, 10) {
			@Override
			protected void onTextChanged(String text) {
				updateVisibility();
			}
		};
		txtAmount.setMaxStringLength(5);
		btnCreateTrade = new GuiButton(BTN_CREATETRADE, guiLeft + 166, guiTop + 30, 80, 20, "Create Trade");
		buttonList.add(btnCreateTrade);

		cbBuy = new GuiCheckBox(CB_BUY, guiLeft + 10, guiTop + 135, "Buy", false);
		cbSell = new GuiCheckBox(CB_SELL, guiLeft + 10, guiTop + 145, "Sell", true);

		cbPartial = new GuiCheckBox(CB_PARTIAL, guiLeft + 10, guiTop + 125, "Allow Partial Fulfillment", true);

		buttonList.add(cbBuy);
		buttonList.add(cbSell);
		buttonList.add(cbPartial);

		int modeTop = 125;
		int modeLeft = 150;
		cbModeOnce = new GuiCheckBox(BTN_MODE_ONCE, guiLeft + modeLeft, guiTop + modeTop, "One-Time", true);
		cbModeRecurring = new GuiCheckBox(BTN_MODE_RECURRING, guiLeft + modeLeft, guiTop + modeTop + 10, "Recurring", false);
		cbModeInfinite = new GuiCheckBox(BTN_MODE_INFINITE, guiLeft + modeLeft, guiTop + modeTop + 20, "Infinite", false);

		int configTop = 105;
		int configLeft = 75;

		txtInterval = new NumericField(TXT_INTERVAL, fontRendererObj, guiLeft + configLeft, guiTop + configTop, 26, 10) {
			@Override
			protected void onTextChanged(String text) {
				updateVisibility();
			}
		};
		txtInterval.setMaxStringLength(3);
		txtStopAfter = new NumericField(TXT_STOP_AFTER, fontRendererObj, guiLeft + configLeft, guiTop + configTop + 10, 26, 10) {
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

		List<String> textLines = Lists.newArrayList("Price", "Minecoins");
		badgePrice = new Badge(txtPrice.xPosition - 10, txtPrice.yPosition + 1, 0, 0, 8, textLines);

		textLines = Lists.newArrayList("Your current balance", "Minecoins");
		badgeFunds = new Badge(guiLeft + 8, guiTop + 6, 0, 0, 8, textLines);
		badgeFunds.visible = false;

		textLines = Lists.newArrayList("Amount");
		badgeAmount = new Badge(txtAmount.xPosition - 10, txtAmount.yPosition + 1, 1, 0, 8, textLines);

		badgeWarning = new Badge(btnCreateTrade.xPosition - 10, btnCreateTrade.yPosition + 6, 0, 1, 8, new ArrayList<String>());
		badgeWarning.visible = false;

		badgeDebug = new Badge(guiLeft, guiTop + 2, 1, 1, 8, new ArrayList<String>());

		badges.add(badgeFunds);
		badges.add(badgePrice);
		badges.add(badgeAmount);
		badges.add(badgeWarning);
		badges.add(badgeDebug);

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

		if(++this.refreshTicker > 20) {
			refreshTicker = 0;
			container.requestTrades();
		}

		UUID playerPersistentID = mc.thePlayer.getPersistentID();
		//TODO: For later! Also do that in updateVisibility
//		boolean isNotPlayersOwn = container.playerInfo == null || !playerPersistentID.equals(container.playerInfo.id);
//
//		if(isNotPlayersOwn) {
//			btnNewTrade.enabled = false;
//			btnCreateTrade.enabled = false;
//		}

//		TradeTerminal terminal = container.trader.terminal;
		badgeDebug.hover.clear();
		badgeDebug.hover.add("Debug Info");
//		badgeDebug.hover.add("Terminal ID (defined): " + container.trader.terminalId);
//		badgeDebug.hover.add("Terminal ID (loaded): " + (terminal == null ? "Terminal Not Loaded" : terminal.id));
//		badgeDebug.hover.add("Owner ID (defined): " + container.trader.ownerId);
		badgeDebug.hover.add("Owner ID (loaded): " + (container.playerInfo == null ? "Owner not loaded" : container.playerInfo.id));
		badgeDebug.hover.add("Your player ID: " + playerPersistentID);
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
			newTrade.allowPartialFulfillment = allowPartial;
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

		/*
		 * Visibility update
		 */

		btnNewTrade.enabled = !isEditingTrade;

		btnCancel.visible = isEditingTrade;
		btnCancel.enabled = isEditingTrade;

		btnCreateTrade.visible = isEditingTrade;
		btnCreateTrade.enabled = isEditingTrade;

		availableOffers.visible = !isEditingTrade;
		txtPrice.setVisible(isEditingTrade);
		txtAmount.setVisible(isEditingTrade);

		badgePrice.visible = isEditingTrade;
		badgeAmount.visible = isEditingTrade;

		txtInterval.setVisible(isEditingTrade && mode == Mode.Recurring);
		txtStopAfter.setVisible(isEditingTrade && mode == Mode.Recurring);

		cbBuy.enabled = isEditingTrade;
		cbBuy.visible = isEditingTrade;
		cbSell.enabled = isEditingTrade;
		cbSell.visible = isEditingTrade;
		cbPartial.enabled = isEditingTrade;
		cbPartial.visible = isEditingTrade;

		cbModeOnce.enabled = isEditingTrade && mode != Mode.Once;
		cbModeOnce.visible = isEditingTrade;
		cbModeOnce.setIsChecked(!cbModeOnce.enabled);
		cbModeRecurring.enabled = isEditingTrade && mode != Mode.Recurring;
		cbModeRecurring.visible = isEditingTrade;
		cbModeRecurring.setIsChecked(!cbModeRecurring.enabled);
		cbModeInfinite.enabled = isEditingTrade && mode != Mode.Infinite;
		cbModeInfinite.visible = isEditingTrade;
		cbModeInfinite.setIsChecked(!cbModeInfinite.enabled);

		/*
		 * Fetch info for creating a trade later
		 */

		isBuy = cbBuy.isChecked();
		allowPartial = cbPartial.isChecked();
		this.price = txtPrice.getIntValue();
		this.amount = txtAmount.getIntValue();
		this.interval = txtInterval.getIntValue();
		this.stopAfter = txtStopAfter.getIntValue();

		/*
		 * Validity Check
		 */
		btnCreateTrade.enabled = true;
		badgeWarning.visible = false;
		badgeWarning.hover.clear();

		if(isEditingTrade) {
			if(price <= 0) {
				btnCreateTrade.enabled = false;
				badgeWarning.visible = true;
				badgeWarning.hover.add("You need to specify a price");
			}
			if(amount <= 0) {
				btnCreateTrade.enabled = false;
				badgeWarning.visible = true;
				badgeWarning.hover.add("You need to specify the amount of items to trade");
			}
			if(tradeStack == null) {
				btnCreateTrade.enabled = false;
				badgeWarning.visible = true;
				badgeWarning.hover.add("You need to specify the item to trade");
			}

			if(mode == Mode.Recurring && interval <= 0) {
				btnCreateTrade.enabled = false;
				badgeWarning.visible = true;
				badgeWarning.hover.add("You need to specify an interval for recurring trades");
			}
		}
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

		if(container.playerInfo != null) {
			String text;
			int txtWidth;
			if(this.mc.thePlayer.getPersistentID().equals(container.playerInfo.id)) {
				text = Long.toString(container.playerInfo.funds);
				txtWidth = fontRendererObj.getStringWidth(text);
				drawString(fontRendererObj, text, guiLeft + 18, guiTop + 6, 0xFFFFFF);
				text = "This terminal belongs to you.";
				badgeFunds.visible = true;
			} else {
				text = "Owner: " + container.playerInfo.name;
				badgeFunds.visible = false;
			}
			txtWidth = fontRendererObj.getStringWidth(text);
			drawString(fontRendererObj, text, guiLeft + 256 - txtWidth - 8, guiTop + 162, 0xFFFFFF);
		}

		for(Badge badge : badges) {
			if(badge == badgeFunds && container.playerInfo != null) {
				badge.hover.set(1, container.playerInfo.funds + " Minecoins");
			}
			mc.renderEngine.bindTexture(badges_tex);
			if(badge.visible) {
				Gui.drawModalRectWithCustomSizedTexture(
						badge.xPos, badge.yPos,
						badge.u * badge.size, badge.v * badge.size,
						badge.size, badge.size, badge.size * 2, badge.size * 2);
			}
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
					drawString(fontRendererObj, tradeStack.getDisplayName(), offsetX + 20, offsetY, 0xFFFFFF);
					drawString(fontRendererObj, trade.toString(), offsetX + 20, offsetY + 10, 0xFFFFFF);
					drawString(fontRendererObj, "Supply/Demand " + Float.toString(Math.round(tradeInfo.supplyDemandFactor * 100) / 100f) + "%", offsetX, offsetY + 20, 0xFFFFFF);
					drawString(fontRendererObj, "Statistics from last period:", offsetX, offsetY + 38, 0xFFFF00);
					drawString(fontRendererObj, "Trade Volume: " + Integer.toString(tradeInfo.volumeLastPeriod), offsetX, offsetY + 46, 0xFFFFFF);
					drawString(fontRendererObj, "Trade Count: " + Integer.toString(tradeInfo.tradesLastPeriod), offsetX, offsetY + 54, 0xFFFFFF);
					drawString(fontRendererObj, "Mean Price: " + Float.toString(Math.round(tradeInfo.meanPrice * 100) / 100f), offsetX, offsetY + 62, 0xFFFFFF);
				}
			}
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
		for(Badge badge : badges) {
			if(badge.visible && badge.contains(mouseX, mouseY)) {
				drawHoveringText(badge.hover, mouseX, mouseY);
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
