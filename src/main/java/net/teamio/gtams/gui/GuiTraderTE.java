package net.teamio.gtams.gui;

import net.minecraft.client.gui.inventory.GuiContainer;

public class GuiTraderTE extends GuiContainer {

	private final ContainerTraderTE container;

	public GuiTraderTE(ContainerTraderTE container) {
		super(container);
		this.container = container;
	}

	@Override
	public void initGui() {
		super.initGui();


	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		// TODO Auto-generated method stub

	}

}
