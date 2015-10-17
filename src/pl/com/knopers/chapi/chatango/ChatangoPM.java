package pl.com.knopers.chapi.chatango;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

import pl.com.knopers.chapi.chatango.model.Credentials;

public class ChatangoPM extends pl.com.knopers.chapi.engine2.EngineWS
{
	private static final String LOGIN_URL = "http://chatango.com/login?user_id=%s&password=%s&storecookie=on&checkerrors=yes";
	private Credentials _uc;
	private String _authKey;
	public ChatangoPM(Credentials uc)
	{
		super();
		if(uc == null)
		{
			//Anon staff here 
		}
		else
		{
			_uc = uc;
		}
	}
	
	public void connect()
	{
		connect("c1.chatango.com", 8080);
		if(auth())
			send("tlogin", _authKey, "2");
	}
	
	public boolean auth()
	{
		if(_authKey != null)
			return true;
		
		try
		{
			URL url = new URL(String.format(LOGIN_URL, _uc.getEncodedLogin(), _uc.getEncodedLogin()));
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			if(connection.getResponseCode() != 200)
				return false;

			Optional<String> authKey = connection.getHeaderFields().get("Set-Cookie").stream().filter(s -> s.startsWith("auth.chatango.com")).map(s -> s.split(";")[0].substring("auth.chatango.com=".length())).findFirst();
			if(authKey.isPresent() && !authKey.get().isEmpty())
			{
				_authKey = authKey.get();
				return true;
			}
		}
		catch(IOException e)
		{
			e.printStackTrace(System.err);
			return false;
		}
		return false;
	}
}
