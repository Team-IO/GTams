package net.teamio.gtams.client.entities;

import java.util.List;
import java.util.UUID;

import net.teamio.gtams.client.entities2.Goods;

public class ETerminalGoodsData {

	public UUID id;
	public List<Goods> goods;

	public ETerminalGoodsData() {
	}

	public ETerminalGoodsData(UUID id, List<Goods> goods) {
		this.id = id;
		this.goods = goods;
	}

}
