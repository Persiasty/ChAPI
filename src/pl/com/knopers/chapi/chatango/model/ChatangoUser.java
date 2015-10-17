package pl.com.knopers.chapi.chatango.model;

public class ChatangoUser
{
	private String _uName;
	public long uid;
	public int aid;
	
	public ChatangoUser(String name)
	{
		_uName = name;
	}
	
	public String getName()
	{
		return _uName;
	}
	@Override
	public String toString()
	{
		return _uName;
	}
}
