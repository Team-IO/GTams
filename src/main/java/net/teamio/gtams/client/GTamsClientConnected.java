package net.teamio.gtams.client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

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
import net.teamio.gtams.client.entities.ETerminalCreateNew;
import net.teamio.gtams.client.entities.ETerminalCreateTrade;
import net.teamio.gtams.client.entities.ETerminalData;
import net.teamio.gtams.client.entities.ETerminalGoodsAdd;
import net.teamio.gtams.client.entities.ETerminalOwner;

public class GTamsClientConnected extends GTamsClient {


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
		Socket socket;
		try {
			socket = new Socket(host, port);
			newConnection.bind(socket);
		} catch (UnknownHostException e) {
			throw new GTamsException("Error connecting to GTams Server", e);
		} catch (IOException e) {
			throw new GTamsException("Error connecting to GTams Server", e);
		}


		executor = new HttpRequestExecutor();
		context = HttpCoreContext.create();
		processor = HttpProcessorBuilder.create()
				.add(new RequestContent()).build();
		clientConnection = newConnection;
	}

	private <T> T doRequestGET(Class<T> responseEntity, String endpoint) throws GTamsException {
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

	private <T> T doRequestPOST(Class<T> responseEntity, String endpoint, Object postData) throws GTamsException {
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

	public void authenticate() throws GTamsException {
		if(Config.client_token == null) {

			EAuthenticate ent = doRequestGET(EAuthenticate.class, "/authenticate");
			Config.client_token = ent.token;
		}
	}

	@Override
	public void notifyTerminalOnline(TradeTerminal terminal) {
		if(terminal.id != null) {
			try {
				doRequestPOST(Void.class, "/terminal_status", new ETerminalData(terminal.id, true));
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
				doRequestPOST(Void.class, "/terminal_status", new ETerminalData(terminal.id, false));
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
				ent = doRequestPOST(ETerminalData.class, "/newterminal", new ETerminalCreateNew(terminal.owner.persistentID));
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
				doRequestPOST(Void.class, "/destroyterminal", new ETerminalData(terminal.id, false));
			} catch (GTamsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void notifyClientOffline(Owner owner) {
		try {
			doRequestPOST(Void.class, "/player_status", new ETerminalData(owner.persistentID, false));
		} catch (GTamsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void notifyClientOnline(Owner owner) {
		try {
			doRequestPOST(Void.class, "/player_status", new ETerminalData(owner.persistentID, true));
		} catch (GTamsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void changeTerminalOwner(TradeTerminal tradeTerminal, Owner newOwner) {
		//TODO: what to do with not fully registered terminals? id == null!
		try {
			doRequestPOST(Void.class, "/terminal_owner", new ETerminalOwner(tradeTerminal.id, newOwner.persistentID));
		} catch (GTamsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public TradeInfo getTradeInfo(TradeDescriptor tradeDescriptor) {
		TradeInfo ti = null;
		try {
			ti = doRequestPOST(TradeInfo.class, "/trade", tradeDescriptor);
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
			tl = doRequestPOST(TradeList.class, "/terminal_trades", new ETerminalData(terminal.id, true));
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
			tl = doRequestPOST(TradeList.class, "/terminal_newtrade", new ETerminalCreateTrade(terminal.id, trade));
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
			gl = doRequestPOST(GoodsList.class, "/terminal_goods", new ETerminalData(terminal.id, true));
		} catch (GTamsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return gl;
	}

	@Override
	public void addGoods(TradeTerminal terminal, GoodsList gl) {
		try {
			doRequestPOST(Void.class, "/terminal_goods_add", new ETerminalGoodsAdd(terminal.id, gl.goods));
		} catch (GTamsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
