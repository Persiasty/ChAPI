package pl.com.knopers.chapi.chatango.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Credentials
{
	private String _login;
	private String _password;

	public Credentials(String login, String password)
	{
		this._login = login;
		this._password = password;
	}

	public String getLogin()
	{
		return _login;
	}

	public String getPassword()
	{
		return _password;
	}
	public String getEncodedLogin()
	{
		try
		{
			return URLEncoder.encode(_login, "UTF-8");
		}
		catch(UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		return _login;
	}

	public String getEncodedPassword()
	{
		try
		{
			return URLEncoder.encode(_password, "UTF-8");
		}
		catch(UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		return _password;
	}
}
