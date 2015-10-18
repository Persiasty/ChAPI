package pl.com.knopers.chapi.chatango;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import pl.com.knopers.chapi.chatango.listeners.JoinListener;
import pl.com.knopers.chapi.chatango.listeners.MessageListener;
import pl.com.knopers.chapi.chatango.listeners.UsersCounter;
import pl.com.knopers.chapi.chatango.model.ChatangoUser;
import pl.com.knopers.chapi.chatango.model.Credentials;
import pl.com.knopers.chapi.chatango.model.RoomMessage;
import pl.com.knopers.chapi.engine2.EngineWS;
import pl.com.knopers.chapi.engine2.Receiver;

public class ChatangoRoom extends EngineWS implements Receiver
{
	private String _rName;
	private Credentials _uc;
	private ChatangoUser _cUser;
	
	private List<ChatangoUser> _userList;
	private Set<RoomMessage> _messageHistory;
	private short _cUsers = -1;
	
	private List<MessageListener> _msgListeners;
	private List<JoinListener> _jListeners;
	private List<UsersCounter> _cListeners;
	
	private Thread _senderThread;
	private ArrayBlockingQueue<String> _msgQueue;
	private Runnable _senderFunction = () ->
	{
		while(!Thread.interrupted())
		{
			try
			{
				String msg = _msgQueue.take();
				if(!msg.isEmpty())
					send(String.format("bmsg:tl2r:%s", msg));
				Thread.sleep(1000);
			}
			catch(Exception e)
			{
				break;
			}
		}
	};
	
	public ChatangoRoom(String roomName, Credentials uc)
	{
		super();
		_rName = roomName;
		_uc = uc;
		addListener(this);
		_messageHistory = new HashSet<>();
		_userList = new ArrayList<>();
		
		_msgListeners = new ArrayList<>();
		_jListeners = new ArrayList<>();
		_cListeners = new ArrayList<>();
		
		_senderThread = new Thread(_senderFunction, "Sender Thread");
		_msgQueue = new ArrayBlockingQueue<>(10);
	}
	
	public void connect()
	{
		String host = String.format("s%s.chatango.com", ChatangoSupport.getServerId(_rName));
		connect(host, 8080);
		send("bauth", _rName, ChatangoSupport.generateUID(), _uc.getLogin(), _uc.getPassword());
		_senderThread.start();
	}
	@Override
	public void close()
	{
		super.close();
		_senderThread.interrupt();
	}
	public void login()
	{
		send("blogin", _uc.getLogin(), _uc.getPassword());
	}
	public void logout()
	{
		send("blogout");
	}
	
	public void sendMessaage(String text)
	{
		send(String.format("bmsg:tl2r:%s", text));
	}
	
	public void queueMessage(String text, Object ... args)
	{
		if(_msgQueue.remainingCapacity() > 0)
			_msgQueue.add(String.format(text, args));
	}

	@Override
	public void OnReceived(String cmd, String ... arg)
	{	
		switch(cmd)
		{
			case "ok":
				if(arg[2].equals("M"))
				{
					_cUser = new ChatangoUser(arg[3]);
					_cUser.uid = Long.parseLong(arg[1]);
					_cUser.aid = Integer.parseInt(arg[1].substring(4, 8));
				}
				//N for Annon, other for error
				
				break;
			case "i": //Msg history
				RoomMessage archivMsg = new RoomMessage(arg[5], arg[0], arg[1].isEmpty() ? arg[3] : arg[1], arg[1].isEmpty(), 
																			ChatangoSupport.concatArray(":", 9, arg), arg[6]);
				break;
			case "inited":
				send("g_participants", "start");
				//send("getpremium", "1"); //request premium info
				break;
			case "n":
				_cUsers = Short.parseShort(arg[0], 16);
				kickListeners(_cUsers);
				break;
			case "":
				//System.out.println("Ping from server");
				break;
			case "g_participants":
				int counter = 0;
				long uid = -1L;
				_userList = new ArrayList<>();
				for(String token : arg)
				{
					if(token.startsWith(";"))
					{
						counter = 0;
						uid = -1;
					}
					if(counter == 3)
					{
						if(token.equals("None"))
							continue;
						ChatangoUser u = new ChatangoUser(token);
						u.uid = uid;
						_userList.add(u);
					}
					counter ++;
				}
				break;
			case "participant": // Join/Leave Event 
				if(arg[3].equals("None")) // When annon comes that he has no name yet
					break;
				Optional<ChatangoUser> usr = _userList.stream().filter(u -> u.getName().toLowerCase().equals(arg[3].toLowerCase())).findFirst();
				if(usr.isPresent())
				{
					ChatangoUser u = usr.get();
					if(arg[0].equals("0"))
					{
						_userList.remove(u);
						kickListeners(false, u);
					}
					else
						u.uid = Long.parseLong(arg[2]);
				}
				else
				{
					ChatangoUser u = new ChatangoUser(arg[3]);
					u.uid = Long.parseLong(arg[2]);
					_userList.add(u);
					kickListeners(true, u);
				}		
				break;
			case "b": //New Msg
				archivMsg = new RoomMessage(arg[5], arg[0], arg[1].isEmpty() ? arg[3] : arg[1], arg[1].isEmpty(), 
																	ChatangoSupport.concatArray(":", 9, arg), arg[6]);
				_messageHistory.add(archivMsg);
				break;
			case "u": //Id for Msg
				Optional<RoomMessage> msg = _messageHistory.stream().filter(m -> m.getUID().equalsIgnoreCase(arg[0])).findFirst();
				if(msg.isPresent())
				{
					RoomMessage message = msg.get();
					message.setMsgID(arg[1]);
					kickListeners(message);
				}
				break;
			case "show_fw":
				System.out.println("Flood Warning Get");
				break;
			case "show_tb":
				System.out.println("Flood Ban Get");
				break;
			case "tb":
				System.out.println("Flood Ban Repeat Get");
				break;
			default:
				kickListeners(cmd, arg);			
				break;
		}
	}
	public String getUserName(boolean lowerCase)
	{
		return lowerCase ? _uc.getLogin().toLowerCase() : _uc.getLogin();
	}
	
	//MsgListener 
	public void addMessageListener(MessageListener listener)
	{
		if(!_msgListeners.contains(listener))
			_msgListeners.add(listener);
	}
	public void deleteMessageListener(MessageListener listener)
	{
		if(_msgListeners.contains(listener))
			_msgListeners.remove(listener);
	}
	private void kickListeners(RoomMessage msg)
	{
		for(MessageListener listener : _msgListeners)
			listener.onMessage(msg);
	}
	private void kickListeners(String cmd, String ... args)
	{
		for(MessageListener listener : _msgListeners)
			listener.onUnknown(cmd, args);
	}
	
	//Join Listener
	public void addJoinListener(JoinListener listener)
	{
		if(!_jListeners.contains(listener))
			_jListeners.add(listener);
	}
	public void deleteJoinListener(JoinListener listener)
	{
		if(_jListeners.contains(listener))
			_jListeners.remove(listener);
	}
	private void kickListeners(boolean join, ChatangoUser user)
	{
		if(join)
		{
			for(JoinListener listener : _jListeners)
				listener.onJoin(user);
		}
		else
		{
			for(JoinListener listener : _jListeners)
				listener.onLeave(user);
		}	
	}
	
	//Current users count listener
	public void addUsersCounterListener(UsersCounter listener)
	{
		if(!_cListeners.contains(listener))
			_cListeners.add(listener);
	}
	public void deleteUsersCounterListener(UsersCounter listener)
	{
		if(_cListeners.contains(listener))
			_cListeners.remove(listener);
	}
	private void kickListeners(short count)
	{
		for(UsersCounter listener : _cListeners)
			listener.onUsersCountChange(count);
	}
	
	//Get users online
	public short getUOnline()
	{
		return _cUsers;
	}
	
	public Optional<ChatangoUser> getUserForName(String name)
	{
		return _userList.stream().filter(u -> u.getName().equalsIgnoreCase(name)).findFirst();
	}
	public List<ChatangoUser> getUsersList()
	{
		return _userList;
	}
}
