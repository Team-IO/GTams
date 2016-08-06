package net.teamio.gtams.client.entities;

import java.util.List;
import java.util.UUID;

import net.teamio.gtams.client.Goods;

public class ETerminalGoodsAdd {

	public UUID id;
	public List<Goods> goods;

	public ETerminalGoodsAdd() {
	}

	public ETerminalGoodsAdd(UUID id, List<Goods> goods) {
		this.id = id;
		this.goods = goods;
	}

}
