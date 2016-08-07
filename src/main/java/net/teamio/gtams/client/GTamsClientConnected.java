package net.teamio.gtams.client;

import java.io.IOException;
import java.net.Socket;

import org.apache.http.HttpClientConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.RequestContent;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.teamio.gtams.Config;
import net.teamio.gtams.client.entities.EAuthenticate;
import net.teamio.gtams.client.entities.EPlayerData;
import net.teamio.gtams.client.entities.ETerminalCreateNew;
import net.teamio.gtams.client.entities.ETerminalCreateTrade;
import net.teamio.gtams.client.entities.ETerminalData;
import net.teamio.gtams.client.entities.ETerminalDeleteTrade;
import net.teamio.gtams.client.entities.ETerminalGoodsData;
import net.teamio.gtams.client.entities.ETerminalOwner;
import net.teamio.gtams.client.entities2.GoodsList;
import net.teamio.gtams.client.entities2.Owner;
import net.teamio.gtams.client.entities2.Player;
import net.teamio.gtams.client.entities2.Trade;
import net.teamio.gtams.client.entities2.TradeDescriptor;
import net.teamio.gtams.client.entities2.TradeInfo;
import net.teamio.gtams.client.entities2.TradeList;
import net.teamio.gtams.client.entities2.TradeTerminal;

public class GTamsClientConnected extends GTamsClient {


	private static final String EP_AUTHENTICATE = "/authenticate";

	private static final String EP_TERMINAL_NEW = "/terminal/new";
	private static final String EP_TERMINAL_DESTROY = "/terminal/destroy";
	private static final String EP_TERMINAL_STATUS = "/terminal/status";
	private static final String EP_TERMINAL_OWNER = "/terminal/owner";

	private static final String EP_TERMINAL_TRADES = "/terminal/trades";
	private static final String EP_TERMINAL_TRADES_ADD = "/terminal/trades/add";
	private static final String EP_TERMINAL_TRADES_REMOVE = "/terminal/trades/remove";
	private static final String EP_TERMINAL_GOODS = "/terminal/goods";
	private static final String EP_TERMINAL_GOODS_ADD = "/terminal/goods/add";
	private static final String EP_TERMINAL_GOODS_REMOVE = "/terminal/goods/remove";

	private static final String EP_PLAYER = "/player";
	private static final String EP_PLAYER_STATUS = "/player/status";
	private static final String EP_MARKET_QUERY = "/market/query";
	private static final String EP_MARKET_GOODS = "/market/goods";

	public static void main(String[] args) throws GTamsException {
		// Client test

		new GTamsClientConnected("localhost", 20405).authenticate();
	}

	private final String host;
	private final int port;

	private HttpClientConnection clientConnection;
	HttpRequestExecutor executor;
	HttpCoreContext context;
	HttpProcessor processor;
	Gson gson;

	public GTamsClientConnected(String host, int port) {
		super();
		this.host = host;
		this.port = port;
		gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
	}

	private void checkConnection() throws GTamsException {
		if(clientConnection == null || !clientConnection.isOpen() || clientConnection.isStale()) {
			connect();
		}
	}

	private void connect() throws GTamsException {
		if(clientConnection != null) {
			try {
				clientConnection.shutdown();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		DefaultBHttpClientConnection newConnection = new DefaultBHttpClientConnection(4096);
		Socket socket = null;
		try {
			socket = new Socket(host, port);
			newConnection.bind(socket);
		} catch (IOException e) {
			if(socket != null) {
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			try {
				newConnection.shutdown();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			throw new GTamsException("Error connecting to GTams Server", e);
		}


		executor = new HttpRequestExecutor();
		context = HttpCoreContext.create();
		processor = HttpProcessorBuilder.create()
				.add(new RequestContent()).build();
		clientConnection = newConnection;
	}

	private <T> T doRequestGET(Class<T> responseEntity, String endpoint) throws GTamsException {
		synchronized(this) {
			checkConnection();
			BasicHttpRequest request = new BasicHttpRequest("GET", endpoint);
			try {

				executor.preProcess(request, processor, context);
				HttpResponse response = executor.execute(request, clientConnection, context);
				executor.postProcess(response, processor, context);


				HttpEntity entity = response.getEntity();
				if(entity == null) {
					return null;
				}
				String responseString = EntityUtils.toString(entity);
				System.out.println("Response from server: " + responseString);
				return gson.fromJson(responseString, responseEntity);
			} catch (HttpException e) {
				throw new GTamsException("Error processing HTTP response", e);
			} catch (IOException e) {
				throw new GTamsException("Network error when contacting server", e);
			} catch (Exception e) {
				throw new GTamsException("Error processing answer from server", e);
			}
		}
	}

	private <T> T doRequestPOST(Class<T> responseEntity, String endpoint, Object postData) throws GTamsException {
		synchronized(this) {
			checkConnection();
			BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("POST", endpoint);

			try {
				String postJson = gson.toJson(postData);
				request.setEntity(new StringEntity(postJson));

				executor.preProcess(request, processor, context);
				HttpResponse response = executor.execute(request, clientConnection, context);
				executor.postProcess(response, processor, context);


				HttpEntity entity = response.getEntity();
				if(entity == null) {
					return null;
				}
				String responseString = EntityUtils.toString(entity);
				System.out.println("Response from server: " + responseString);
				return gson.fromJson(responseString, responseEntity);
			} catch (HttpException e) {
				throw new GTamsException("Error processing HTTP response", e);
			} catch (IOException e) {
				throw new GTamsException("Network error when contacting server", e);
			} catch (Exception e) {
				throw new GTamsException("Error processing answer from server", e);
			}
		}
	}

	public void authenticate() throws GTamsException {
		if(Config.client_token == null) {

			EAuthenticate ent = doRequestGET(EAuthenticate.class, EP_AUTHENTICATE);
			Config.client_token = ent.token;
		}
	}

	@Override
	public void notifyTerminalOnline(TradeTerminal terminal) {
		if(terminal.id != null) {
			try {
				doRequestPOST(Void.class, EP_TERMINAL_STATUS, new ETerminalData(terminal.id, terminal.getOwnerId(), true));
			} catch (GTamsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void notifyTerminalOffline(TradeTerminal terminal) {
		if(terminal.id != null) {
			try {
				doRequestPOST(Void.class, EP_TERMINAL_STATUS, new ETerminalData(terminal.id, terminal.getOwnerId(), false));
			} catch (GTamsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void registerNewTerminal(TradeTerminal terminal) {
		synchronized (sync_object) {
			ETerminalData ent;
			try {
				ent = doRequestPOST(ETerminalData.class, EP_TERMINAL_NEW, new ETerminalCreateNew(terminal.owner.id));
				terminal.id = ent.id;
				if(!terminal.isOnline) {
					destroyTerminal(terminal);
				}
			} catch (GTamsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void destroyTerminal(TradeTerminal terminal) {
		synchronized (sync_object) {
			try {
				doRequestPOST(Void.class, EP_TERMINAL_DESTROY, new ETerminalData(terminal.id, terminal.getOwnerId(), false));
			} catch (GTamsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void notifyClientOffline(Owner owner) {
		try {
			doRequestPOST(Void.class, EP_PLAYER_STATUS, new EPlayerData(owner.id, false));
		} catch (GTamsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void notifyClientOnline(Owner owner) {
		try {
			doRequestPOST(Void.class, EP_PLAYER_STATUS, new EPlayerData(owner.id, true));
		} catch (GTamsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void updateOwnerInfo(Owner owner) {
		try {
			Player info = doRequestPOST(Player.class, EP_PLAYER, new EPlayerData(owner.id, true));
			if(info != null) {
				owner.funds = info.funds;
			}
		} catch (GTamsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void changeTerminalOwner(TradeTerminal tradeTerminal, Owner newOwner) {
		//TODO: what to do with not fully registered terminals? id == null!
		try {
			doRequestPOST(Void.class, EP_TERMINAL_OWNER, new ETerminalOwner(tradeTerminal.id, newOwner.id));
		} catch (GTamsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public TradeInfo getTradeInfo(TradeDescriptor tradeDescriptor) {
		TradeInfo ti = null;
		try {
			ti = doRequestPOST(TradeInfo.class, EP_MARKET_QUERY, tradeDescriptor);
			ti.trade = tradeDescriptor;
		} catch (GTamsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ti;
	}

	@Override
	public TradeList getTrades(TradeTerminal terminal) {
		TradeList tl = null;
		try {
			if(terminal != null) {
				tl = doRequestPOST(TradeList.class, EP_TERMINAL_TRADES, new ETerminalData(terminal.id, terminal.getOwnerId(), true));
			}
		} catch (GTamsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tl;
	}

	@Override
	public TradeList createTrade(TradeTerminal terminal, Trade trade) {
		TradeList tl = null;
		try {
			tl = doRequestPOST(TradeList.class, EP_TERMINAL_TRADES_ADD, new ETerminalCreateTrade(terminal.id, trade));
		} catch (GTamsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tl;
	}

	@Override
	public TradeList removeTrade(TradeTerminal terminal, long tradeId) {
		TradeList tl = null;
		try {
			tl = doRequestPOST(TradeList.class, EP_TERMINAL_TRADES_ADD, new ETerminalDeleteTrade(terminal.id, tradeId));
		} catch (GTamsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tl;
	}

	@Override
	public GoodsList getGoods(TradeTerminal terminal) {
		GoodsList gl = null;
		try {
			gl = doRequestPOST(GoodsList.class, EP_TERMINAL_GOODS, new ETerminalData(terminal.id, terminal.getOwnerId(), true));
		} catch (GTamsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return gl;
	}

	@Override
	public GoodsList addGoods(TradeTerminal terminal, GoodsList request) {
		GoodsList gl = null;
		try {
			gl = doRequestPOST(GoodsList.class, EP_TERMINAL_GOODS_ADD, new ETerminalGoodsData(terminal.id, request.goods));
		} catch (GTamsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return gl;
	}

	@Override
	public GoodsList removeGoods(TradeTerminal terminal, GoodsList request) {
		GoodsList gl = null;
		try {
			gl = doRequestPOST(GoodsList.class, EP_TERMINAL_GOODS_REMOVE, new ETerminalGoodsData(terminal.id, request.goods));
		} catch (GTamsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return gl;
	}
}
