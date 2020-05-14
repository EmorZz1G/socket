package emor.socket.server;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;

import emor.socket.user.User;

public class HeartCheckServiceImpl implements Runnable {
	Socket client = null;
	User user =null;
	ArrayList<Socket> clients = null;
	Lock clientListLock = null;
	ObjectInputStream ois = null;
	public HeartCheckServiceImpl(Socket s,User user,ArrayList<Socket> clients,Lock clientListLock) {  
        this.client = s;
        this.user=user;
        this.clients = clients;
        
        this.clientListLock = clientListLock;
    }  

	@Override
	public void run() {
		try {
			int index = 1;
			while(true) {
				if(index>10) {
					clientListLock.lock();
					if(clients.contains(client))
						clients.remove(client);
					client.close();
					clientListLock.unlock();
					System.out.println("服务器已经停止该连接");
					break;
				}
				index++;
				while(client.getInputStream().available()==0) {
					Thread.sleep(500);
				}
				ois = new ObjectInputStream(client.getInputStream());
				User check = (User) ois.readObject();
				System.out.println(check);
				if(check==user) {
					index=0;
				}
				Thread.sleep(2000);
			}
		}catch (Exception e) {
			e.printStackTrace();
		} finally{
			
		}
	}
}