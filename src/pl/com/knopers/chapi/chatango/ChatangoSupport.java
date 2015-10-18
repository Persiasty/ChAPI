package pl.com.knopers.chapi.chatango;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pl.com.knopers.chapi.chatango.model.UserProfileInfo;
import pl.com.knopers.chapi.chatango.model.UserProfileInfo.Gender;

public class ChatangoSupport
{
	private static HashMap<String, Integer> _weights = new LinkedHashMap<>();
	static
	{
		_weights.put("5", 75);
		_weights.put("6", 75);
		_weights.put("7", 75);
		_weights.put("8", 75);
		_weights.put("16", 75);
		_weights.put("17", 75);
		_weights.put("18", 75);
		_weights.put("9", 95);
		_weights.put("11", 95);
		_weights.put("12", 95);
		_weights.put("13", 95);
		_weights.put("14", 95);
		_weights.put("15", 95);
		_weights.put("19", 110);
		_weights.put("23", 110);
		_weights.put("24", 110);
		_weights.put("25", 110);
		_weights.put("26", 110);
		_weights.put("28", 104);
		_weights.put("29", 104);
		_weights.put("30", 104);
		_weights.put("31", 104);
		_weights.put("32", 104);
		_weights.put("33", 104);
		_weights.put("35", 101);
		_weights.put("36", 101);
		_weights.put("37", 101);
		_weights.put("38", 101);
		_weights.put("39", 101);
		_weights.put("40", 101);
		_weights.put("41", 101);
		_weights.put("42", 101);
		_weights.put("43", 101);
		_weights.put("44", 101);
		_weights.put("45", 101);
		_weights.put("46", 101);
		_weights.put("47", 101);
		_weights.put("48", 101);
		_weights.put("49", 101);
		_weights.put("50", 101);
		_weights.put("52", 110);
		_weights.put("53", 110);
		_weights.put("55", 110);
		_weights.put("57", 110);
		_weights.put("58", 110);
		_weights.put("59", 110);
		_weights.put("60", 110);
		_weights.put("61", 110);
		_weights.put("62", 110);
		_weights.put("63", 110);
		_weights.put("64", 110);
		_weights.put("65", 110);
		_weights.put("66", 110);
		_weights.put("68", 95);
		_weights.put("71", 116);
		_weights.put("72", 116);
		_weights.put("73", 116);
		_weights.put("74", 116);
		_weights.put("75", 116);
		_weights.put("76", 116);
		_weights.put("77", 116);
		_weights.put("78", 116);
		_weights.put("79", 116);
		_weights.put("80", 116);
		_weights.put("81", 116);
		_weights.put("82", 116);
		_weights.put("83", 116);
		_weights.put("84", 116);
	}
	
	public static int getServerId(String name)
	{
        name = name.toLowerCase().replaceAll("[^0-9a-z]", "q");
        float fnv = (float) new BigInteger(name.substring(0, Math.min(5, name.length())), 36).intValue();
        int lnv = 1000;
        if(name.length() > 6)
        {
            lnv = new BigInteger(name.substring(6, 6 + Math.min(3, name.length() - 5)), 36).intValue();
            lnv = Math.max(lnv, 1000);
        }
        float num = (fnv % lnv) / lnv;
        int maxnum = 0;
        for(Integer i : _weights.values())
        	maxnum += i;
        float sumfreq = 0;
        int sn = 0;
        for(String key : _weights.keySet())
        {
            sumfreq += ((float) _weights.getOrDefault(key, 0)) / maxnum;
            if(num <= sumfreq)
            {
                sn = Integer.parseInt(key);
                break;
            }
        }
        return sn;
    }
	
	public static String generateUID()
	{
		long uid = 5200000000000000L + (long)(Math.random() * 100000000000000L);
        return String.valueOf(uid);
	}
	
	public static String getAnonName(String nTag, String ssid)
	{
		StringBuffer sb = new StringBuffer();
		nTag = nTag.isEmpty() || nTag.length() != 4 ? "5504" : nTag;
		ssid = ssid.substring(4);
		sb.append("Anon");
		for(int i = 0; i < 4; i++)
			sb.append(String.valueOf(Integer.parseInt(String.valueOf(nTag.charAt(i))) + Integer.parseInt(String.valueOf(ssid.charAt(i)))));
		return sb.toString();
	}
	public static UserProfileInfo getUserProfileInfo(String name) throws SAXException, IOException, ParserConfigurationException, ParseException
	{
		name = name.toLowerCase();
		String addr = String.format("http://ust.chatango.com/profileimg/%c/%c/%s/mod1.xml", name.charAt(0), name.charAt(1), name);
		
		URL url = new URL(addr);
		
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openStream());
		NodeList cn = doc.getFirstChild().getChildNodes();
		UserProfileInfo info = new UserProfileInfo();
		for(int i = 0; i < cn.getLength(); i++)
		{
			Node n = cn.item(i);
			switch(n.getNodeName())
			{
				case "body":
					info.Description = URLDecoder.decode(n.getNodeValue(), "UTF-8");
					break;
				case "b":
					String date = n.getNodeValue();
					if(date == null || date.isEmpty())
						break;
					
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					Calendar c = Calendar.getInstance();
					c.setTime(sdf.parse(date)); 
					info.Age = Calendar.getInstance().get(Calendar.YEAR) - c.get(Calendar.YEAR);
					break;
				case "l":
					info.Location = URLDecoder.decode(n.getNodeValue(), "UTF-8");
					break;
				case "s":
					String g = n.getNodeValue();
					info.Gender = g.equalsIgnoreCase("M") ? Gender.Male : 
									g.equalsIgnoreCase("F") ? Gender.Female : null;
					break;
				case "t":
					info.About = URLDecoder.decode(n.getNodeValue(), "UTF-8");
					break;
			}
		}
		return info;
	}
	public static String concatArray(String delimiter, int offset, String ... args)
	{
		StringBuffer sb = new StringBuffer();
		for(int i = offset; i < args.length; i++)
			sb.append(args[i]).append(delimiter);
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
}
