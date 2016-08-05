package net.teamio.gtams.client;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.RequestContent;
import org.apache.http.util.EntityUtils;

import net.teamio.gtams.Config;

public class GTamsClient {


	public static void main(String[] args) {
		// Client test

		new GTamsClient("localhost", 20405).authenticate();
	}

	private final String host;
	private final int port;

	public GTamsClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void authenticate() {
		if(Config.client_token == null) {
			HttpProcessor processor = HttpProcessorBuilder.create()
					.add(new RequestContent()).build();

			HttpRequestExecutor executor = new HttpRequestExecutor();
			HttpCoreContext context = HttpCoreContext.create();

			DefaultBHttpClientConnection clientConnection = new DefaultBHttpClientConnection(4096);

			BasicHttpRequest request = new BasicHttpRequest("GET", "/authenticate");
			try {
				Socket socket = new Socket(host, port);
				clientConnection.bind(socket);

				executor.preProcess(request, processor, context);
				HttpResponse response = executor.execute(request, clientConnection, context);
				executor.postProcess(response, processor, context);


				HttpEntity entity = response.getEntity();
				if(entity == null) {
					//TODO: handle
				} else {
					String responseString = EntityUtils.toString(entity);
					//TODO => json?
					System.out.println("Response from server: " + responseString);
				}

				clientConnection.close();
			} catch (HttpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
			}
		}
	}

	public static GTamsClient forOwner(UUID owner) {
		// TODO Auto-generated method stub
		return new GTamsClient("localhost", 20405);
	}

	private Map<UUID, TradeTerminal> terminalCache;

	public TradeTerminal getTerminal(UUID terminalId) {
		if(terminalId == null) {
			TradeTerminal terminal = new TradeTerminal(this, null);
			registerNewTerminal(terminal);
			return terminal;
		}
		TradeTerminal terminal = terminalCache.get(terminalId);
		if(terminal == null) {
			terminal = new TradeTerminal(this, terminalId);
			notifyTerminalOnline(terminal);
		}
		return terminal;
	}

	private void notifyTerminalOnline(TradeTerminal terminal) {
		// TODO Auto-generated method stub

	}

	private void notifyTerminalOffline(TradeTerminal terminal) {
		// TODO Auto-generated method stub

	}

	private void registerNewTerminal(TradeTerminal terminal) {
		// TODO Auto-generated method stub

	}

	/**
	 * Release a use handle on a terminal.
	 * Default implementation for now will mark the terminal and all related trades as offline.
	 * @param tradeTerminal
	 */
	public void releaseTerminal(TradeTerminal terminal) {
		// TODO Auto-generated method stub
		notifyTerminalOffline(terminal);
	}

	/**
	 * Removes a terminal from the list, deleting all related trades.
	 * @param terminal
	 */
	public void destroyTerminal(TradeTerminal terminal) {
//TODO
	}

	public void release() {
		// TODO Auto-generated method stub

	}
}
