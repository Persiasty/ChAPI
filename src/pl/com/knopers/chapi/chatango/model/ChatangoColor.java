package pl.com.knopers.chapi.chatango.model;

public class ChatangoColor
{
	public static final ChatangoColor BLACK = new ChatangoColor("000");
	public static final ChatangoColor RED = new ChatangoColor("F00");
	public static final ChatangoColor BLUE = new ChatangoColor("00F");
	public static final ChatangoColor GREEN = new ChatangoColor("0F0");
	
	private String _colorCode;

	public ChatangoColor(String colorCode)
	{
		this._colorCode = colorCode;
	}

	public String getCode()
	{
		return _colorCode;
	}
	
	public String getColorTag()
	{
        return "<n" + _colorCode + "/>";
    }

	@Override
	public String toString()
	{
		return "ChatangoColor [_colorCode=" + _colorCode + "]";
	}
}
