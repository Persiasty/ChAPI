package pl.com.knopers.chapi.chatango.model;


public class ChatangoFont
{
	//Default Chatango font
	public static final ChatangoFont DEFAULT = new ChatangoFont();
	//Font family names
	public static enum FontName
	{
        ARIAL,
        COMIC,
        GEORGIA,
        HANDWRITING,
        IMPACT,
        PALATINO,
        PAPYRUS,
        TIMES,
        TYPEWRITER;
    }
	private FontName _fName;
	private ChatangoColor _fColor;
	private int _fSize; // 9 - 22
	
	public ChatangoFont()
	{
		this(null, 0, null);
	}
	
	public ChatangoFont(FontName family)
	{
		this(family, 0, null);
	}
	
	public ChatangoFont(int size)
	{
		this(null, size, null);
	}
	
	public ChatangoFont(FontName family, int size)
	{
		this(family, size, null);
	}
	
	public ChatangoFont(FontName family, int size, ChatangoColor color)
	{
		_fName = family == null ? FontName.ARIAL : family;
		_fSize = size == 0 ? 12 : size > 22 ? 22 : size < 9 ? 9 : size;
		_fColor = color == null ? ChatangoColor.BLACK : color;
	}
	public String getFontTag()
	{
        return String.format("<f x%02d%s=\"%s\">", _fSize, _fColor.getCode(), _fName.ordinal());
    }

	
}
