package net.teamio.gtams.client;

import java.io.IOException;
import java.net.Socket;

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

		authenticate();
	}

	public static void authenticate() {
		if(Config.client_token == null) {
			HttpProcessor processor = HttpProcessorBuilder.create()
					.add(new RequestContent()).build();

			HttpRequestExecutor executor = new HttpRequestExecutor();
			HttpCoreContext context = HttpCoreContext.create();

			DefaultBHttpClientConnection clientConnection = new DefaultBHttpClientConnection(4096);

			BasicHttpRequest request = new BasicHttpRequest("GET", "/authenticate");
			try {
				Socket socket = new Socket("localhost", 20405);
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
}
