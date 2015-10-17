package pl.com.knopers.chapi.chatango.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.com.knopers.chapi.chatango.ChatangoSupport;


public class RoomMessage
{
	private String _msgid;
	private String _text;
	private String _raw;
	private ChatangoFont _tFont;
	private ChatangoColor _aColor;
	private long _tTimestamp;
	private String _tAuthor;
	private String _tIP;
	private String _uid;
	private boolean _anon = true;
	
	private boolean _bold = false;
	private boolean _italic = false;
	private boolean _underline = false;
	
	public RoomMessage(String uid, String timestamp, String author, boolean anonymous, String msg, String ip)
	{
		_uid = uid;
		_tTimestamp = Long.parseLong(timestamp.replaceAll("\\.", ""));
		_tIP = ip;
		if(_anon = anonymous)
		{
			String pattern = "<n(\\d{4})>";
			Pattern nameTag = Pattern.compile(pattern);
			Matcher mt = nameTag.matcher(msg);
			
			String nTag = "";
			if(mt.find())
				nTag = mt.group(1);
			
			_text = msg.replaceAll(pattern, "");
			_tAuthor = ChatangoSupport.getAnonName(nTag, author);
		}
		else
		{
			_raw = new String(msg);
			_tAuthor = author;
			String fontTag = "<f x(\\d{1,2})?([a-fA-F0-9]{3})?=\"(\\d)?\">";
			String fontTagRemove = "<f x[^>]*>";
			String nameColorTag = "<n([0-9a-fA-F]{3,6})/>";
			
			String bTag = "(<b>[^@][^<]{1,}</b>)|(<b>[^@])";
			String bTagRemove = "</?b>";
			String iTag = "</?i>";
			String uTag = "</?u>";
			String brTag = "<br ?/?>";
			
			Matcher mt = Pattern.compile(nameColorTag).matcher(msg);
			if(mt.find())
				_aColor = new ChatangoColor(mt.group(1).toLowerCase());
			msg = msg.replaceAll(nameColorTag, "");
			
			mt = Pattern.compile(fontTag).matcher(msg);
			if(mt.find())
			{
				int size = mt.group(1) == null || mt.group(1).isEmpty() ? 0 : Integer.parseInt(mt.group(1));
				ChatangoColor cc = mt.group(2) == null || mt.group(2).isEmpty() ? ChatangoColor.BLACK : new ChatangoColor(mt.group(2).toLowerCase());
				int shape  = mt.group(3) == null || mt.group(3).isEmpty() ? 0 : Integer.parseInt(mt.group(3));
				_tFont = new ChatangoFont(ChatangoFont.FontName.values()[shape], size, cc);
			}
			else
				_tFont = ChatangoFont.DEFAULT;
			msg = msg.replaceAll(fontTagRemove, "");
			
			mt = Pattern.compile(bTag).matcher(msg);
			_bold = mt.find();
			msg = msg.replaceAll(bTagRemove, "");
			
			mt = Pattern.compile(iTag).matcher(msg);
			_italic = mt.find();
			msg = msg.replaceAll(iTag, "");
			
			mt = Pattern.compile(uTag).matcher(msg);
			_underline = mt.find();
			msg = msg.replaceAll(uTag, "");
			
			msg = msg.replaceAll(brTag, "");
			
			_text = msg;
		}
	}
	public void setMsgID(String id)
	{
		_msgid = id;
	}
	public String getMsgId()
	{
		return _msgid;
	}
	public String getIP()
	{
		return _tIP;
	}
	public String getAuthorName()
	{
		return _tAuthor;
	}
	public ChatangoColor getAuthorColor()
	{
		return _aColor == null ? ChatangoColor.BLACK : _aColor;
	}
	public ChatangoFont getFont()
	{
		return _tFont;
	}
	public String getText()
	{
		return _text;
	}
	public long getTimestamp()
	{
		return _tTimestamp;
	}
	public String getUID()
	{
		return _uid;
	}
	@Override
	public String toString()
	{
		return _raw;
	}

	public boolean isBold()
	{
		return _bold;
	}
	public boolean isItalic()
	{
		return _italic;
	}
	public boolean isUnderline()
	{
		return _underline;
	}
	public void postFixMsg(int count)
	{
		if(count < _text.length())
			_text = _text.substring(count);
		else
			_text = "";
	}
	public boolean isAnon()
	{
		return _anon;
	}
}