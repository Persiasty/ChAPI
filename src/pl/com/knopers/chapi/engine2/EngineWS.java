package pl.com.knopers.chapi.engine2;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;


public class EngineWS
{
	private WebSocketClient _client;
	private WebSocketAdapter _adapter;
	private List<Receiver> _listeners;
	private Session _session;
	private boolean _fcs = false;
	
	private ScheduledExecutorService _executorService = Executors.newSingleThreadScheduledExecutor();
	

	public EngineWS()
	{	
		Log.setLog(new NoLogging());
		_client = new WebSocketClient();
		_listeners = new ArrayList<>();
		try
		{
			_client.start();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	protected void connect(String host, int port)
	{
		URI destination;
		try
		{
			destination = new URI("ws://" + host + ":" + port);
		}
		catch(URISyntaxException e)
		{
			System.err.println(e.getMessage());
			return;
		}

		ClientUpgradeRequest request = new ClientUpgradeRequest();
		request.setSubProtocols("chat");
		request.setHeader("Origin", "http://st.chatango.com");
		
		_adapter = new WebSocketAdapter()
		{
			@Override
			public void onWebSocketText(String message)
			{
				super.onWebSocketText(message);
				message = message.endsWith("\r\n") ? message.substring(0, message.length() - "\r\n".length()) : message;
				kickListeners(message);
			}

			@Override
			public void onWebSocketConnect(Session sess)
			{
				super.onWebSocketConnect(sess);
			}
		};

		try
		{
			Future<Session> session = _client.connect(_adapter, destination, request);
			
			_session = session.get();
			_executorService.scheduleWithFixedDelay(() -> send(""), 20L, 20L, TimeUnit.SECONDS);
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
		}
	}

	protected void addListener(Receiver listener)
	{
		if(listener != null) _listeners.add(listener);
	}

	protected void removeListener(Receiver listener)
	{
		if(_listeners.contains(listener)) _listeners.remove(listener);
	}

	protected synchronized void send(String command, String ... args)
	{
		StringBuffer sbuff = new StringBuffer();
		sbuff.append(command);
		for(String arg : args)
			sbuff.append(":").append(arg);
		
		sbuff.append("\r\n");
		if(!_fcs)
		{
			sbuff.append((char)0x00);
			_fcs = true;
		}
		if(_client == null || !_client.isRunning()) return;
		try
		{
			_adapter.getRemote().sendBytes(ByteBuffer.wrap(sbuff.toString().getBytes()));
		}
		catch(IOException e)
		{
			System.err.println(e.getMessage());
		}
	}

	public void close()
	{
		if(_session == null || !_session.isOpen())
			return;
		
		_executorService.shutdownNow();
		_session.close();
	}

	private void kickListeners(String msg)
	{
		String[] tar = msg.split(":");
		String cmd = tar[0];
		String[] args = new String[tar.length - 1];
		if(tar.length > 1)
			System.arraycopy(tar, 1, args, 0, args.length);

		for (Receiver rec : _listeners)
			if(rec != null) rec.OnReceived(cmd, args);
	}
}
