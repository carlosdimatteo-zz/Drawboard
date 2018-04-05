package cdds;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONObject;

@ServerEndpoint("/Socket/{nombre}")
public class Socket {
	private static Set<Session> clients = 
			Collections.synchronizedSet(new HashSet<Session>());
	private static HashMap<String,String> usuarios=new HashMap<>();
	JSONObject json= new JSONObject();
	
	
	@OnMessage
	public void onMessage (String msg, Session session)
			throws IOException{
		//JSONObject json = new JSONObject();
		JSONObject json = new JSONObject(msg);
		
		//json.put("status", 200).put("url", msg);
		System.out.println(msg);
		synchronized (clients) {
			for (Session client : clients) {
				if(!client.getId().equals(session.getId()))
					client.getBasicRemote().sendText(json.toString());
			}
		}
		
	}
	
	@OnOpen
	public void onOpen(Session session,@PathParam("nombre")String nombre) throws IOException{
			clients.add(session);
			usuarios.put(session.getId(), nombre);
			JSONObject json=new JSONObject();
			json.put("users", usuarios);
//			json.put("status", 200).put("msg", "Alguien se unio a  la sala");
			synchronized (clients) {
				for (Session client : clients) {
					client.getBasicRemote().sendText( json.toString());
				}
			}
	}
	
	@OnClose
	public void onClose (Session session) throws IOException{
		clients.remove(session);
		//JSONObject json = new JSONObject();
		String mensaje="El usuario "+ usuarios.get(session.getId())+" abandono la sala";
		json.put("status", 200).put("msg", mensaje);
		usuarios.remove(session.getId());
		clients.remove(session);
		synchronized (clients) {
			for (Session client : clients) {
				client.getBasicRemote().sendText( json.toString());
			}
		}
		
	}
	@OnError
	public void onError(Throwable e) throws IOException {
	    e.printStackTrace();
		JSONObject json = new JSONObject();
		json.put("status", 500).put("msg", e.getStackTrace());
	    synchronized (clients) {
			for (Session client : clients) {
				client.getBasicRemote().sendText( json.toString());
			}
		}
	}

}
