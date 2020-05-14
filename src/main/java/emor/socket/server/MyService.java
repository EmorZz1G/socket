package emor.socket.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Emor
 */

public class MyService {
	private ServerSocket server = null;
	/**
	 * 客户端集合
	 */
	private Lock clientListLock = new ReentrantLock();
	private static ArrayList<Socket> clients = new ArrayList<>();

	private void start() {
//    	 server = null;
		try {
			// 开启服务，设置指定端口
			server = new ServerSocket(8888);
			System.out.println("服务开启，等待客户端连接中...");
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					while(true) {
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						for(Socket s :clients) {
							System.out.println(s);
						}
						System.out.println("当前在线用户"+clients.size()+"个");
					}
				}
			},"link-num").start();
			// 循环监听
			while (true) {
				// 等待客户端进行连接
				Socket client = server.accept();
				// 将客户端添加到集合
				clientListLock.lock();
				clients.add(client);
				System.out.println("客户端 [" + client.getRemoteSocketAddress() + "]连接成功，当前在线用户" + clients.size() + "个");
				clientListLock.unlock();
				// 每一个客户端开启一个线程处理消息
				new MessageListener(client,"msg").start();
//				new Thread(new HeartCheckServiceImpl(client,new User(null,client.getPort()),clients,clientListLock)).start();  
				
			}
		} catch (IOException e) {
			// log
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}
	
	
	/**
	 * 消息处理线程，负责转发消息到聊天室里的人
	 */
	
	class MessageListener extends Thread {

		// 将每个连接上的客户端传递进来，收消息和发消息
		private Socket client;

		// 将这几个变量抽出来公用，避免频繁new对象
		private OutputStream os;
		private PrintWriter pw;
		private InputStream is;
		private InputStreamReader isr;
		private BufferedReader br;
		private BufferedOutputStream bos;
		@SuppressWarnings("unused")
		private BufferedInputStream bis;

		// 一个监听对象针对一个用户
		public MessageListener(Socket socket) {
			this.client = socket;
		}
		public MessageListener(Socket socket,String name) {
			this.client = socket;
			setName(name);
		}
		@Override
		public void run() {
			try {
				// 每个用户连接上了，就发送一条系统消息（类似于广播）
				sendMsg(0,
						"[系统消息]：欢迎" + client.getRemoteSocketAddress() + "来到聊天室，当前共有" + (clients.size()) + "人在聊天");
				
				// 循环监听消息
				while (true) {
					try {
						String receiveMsg = receiveMsg();
						if (receiveMsg != null && receiveMsg.length() > 0) {
							System.out.println("收到消息：" + receiveMsg);
							if (receiveMsg.charAt(0) == '#')
								sendMsg(2, receiveMsg);
							else {
								sendMsg(1, "[" + client.getRemoteSocketAddress() + "]：" + receiveMsg);
							}
						}
					}catch (Exception e) {
						e.printStackTrace();
						if(clients.contains(client)) {
							clients.remove(client);
							client.close();
							this.stop();
							return;
						}
					} finally{
						
					}
					
				}
			} catch (IOException e) {
				// log
				e.printStackTrace();
			} 
		}

		/**
		 * 发送消息
		 *
		 * @param type 消息类型（0、系统消息；1、用户广播消息；2、用户P2P消息）
		 * @param msg  消息内容
		 * @throws IOException
		 */
		private void sendMsg(int type, String msg) {
			if (type == 0) {
				for (Socket socket : clients) {
//					System.out.println(socket.getRemoteSocketAddress());
//					if (type != 0 && socket == client) {
//						continue;
//					}
					try {
						os = socket.getOutputStream();
//					pw = new PrintWriter(os);
//					pw.println(msg);
//					pw.flush();
						bos = new BufferedOutputStream(os);
						bos.write(msg.getBytes());
						bos.flush();
					} catch (Exception e) {
						continue;
					}
				}
			} else if (type == 1) {
				System.out.println("处理消息：" + msg);
				for (Socket socket : clients) {
//					System.out.println(socket.getRemoteSocketAddress());
//					if (type != 0 && socket == client) {
//						continue;
//					}
					try {
						os = socket.getOutputStream();
//					pw = new PrintWriter(os);
//					pw.println(msg);// 这里需要特别注意，对方用readLine获取消息，就必须用print而不能用write，否则会导致消息获取不了
//					pw.flush();
						bos = new BufferedOutputStream(os);
						bos.write(msg.getBytes());
						bos.flush();
					} catch (Exception e) {
						continue;
					}
				}
			} else if (type == 2) {
				String[] split = msg.split("#");
				int toPort = 0;
				Socket toSocket = null;
				String splitMsg =null;
				SocketAddress from = null;
				try {
					toPort = Integer.parseInt(split[1]);
					System.out.println(toPort);
					splitMsg = split[2];
					System.out.println(splitMsg);
					from = client.getRemoteSocketAddress();
				}catch (NumberFormatException e) {
					System.out.println("NumberFormatException");
					sendMsg(1,"[" + client.getRemoteSocketAddress() + "]：" + msg);
					return;
				} finally{
					
				}
				for (Socket socket : clients) {
					if (socket.getPort() == toPort) {
						toSocket = socket;
					}
				}
				try {
					if (toSocket == null) {
						os = client.getOutputStream();
						pw = new PrintWriter(os);
						pw.print("[系统消息]：该用户不存在");
						pw.flush();
						return;
					}
					os = toSocket.getOutputStream();
//				pw = new PrintWriter(os,false);
					String finalMsg = "来自" + from.toString() + "的私信\r\n" + splitMsg;
					System.out.println(finalMsg);
					bos = new BufferedOutputStream(os);
					bos.write(finalMsg.getBytes());
					bos.flush();

				} catch (Exception e) {
				}

			}
		}

		/**
		 * 接收消息
		 *
		 * @return 消息内容
		 * @throws IOException
		 */
		private String receiveMsg() throws IOException {
			is = client.getInputStream();
			isr = new InputStreamReader(is);

			br = new BufferedReader(isr);
			return br.readLine();
		}
	}

	public static void main(String[] args) {
		new MyService().start();
	}

}