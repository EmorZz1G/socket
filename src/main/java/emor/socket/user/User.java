package emor.socket.user;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class User implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String username = null;
	private long portId = 0;
	
	public User(String username, long portId) {
		super();
		this.username = username;
		this.portId = portId;
	}

	public long getPortId() {
		return portId;
	}

	public void setPortId(long portId) {
		this.portId = portId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	public static void main(String[] args)  {
		InetAddress ina = null;
		try {
			ina = InetAddress.getByName("server.natappfree.cc");
			Socket s = new Socket(ina,36217);
			System.out.println("he");
			System.out.println(s.isConnected());
			OutputStream os = s.getOutputStream();
			os.write("nihao".getBytes());
			os.flush();
			System.out.println(ina);
			Thread.sleep(10000);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
