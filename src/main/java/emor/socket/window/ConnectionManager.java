package emor.socket.window;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ConnectionManager {
	private ConnectionManager() {
	}

	private static final ConnectionManager instance = new ConnectionManager();

	public static ConnectionManager getChatManager() {
		return instance;
	}

	MyClientWindow window;// 为了能在界面上显示服务器发来的信息，就需要传一个MainWindow的引用进来
	Socket server;
	private String IP;
	BufferedReader bReader;
	PrintWriter pWriter;
	BufferedOutputStream bos;
	BufferedInputStream bis;
	InputStream is;
	private Thread sendHeart = null;
	class sendHeart extends Thread{
		Socket server = null;
		sendHeart(Socket server){
			this.server = server;
		}
		@Override
		public void run() {
			while(true){
				try {
					server.sendUrgentData(0xFF);
					System.out.println("目前处于链接状态！");
					Thread.sleep(3000);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	public void setWindow(MyClientWindow window) {
		this.window = window;
	}

	public void connect(String ip) {
		this.IP = ip;
		new Thread() {
			@Override
			public void run() {
				// 实现网络方法
				try {
					server = new Socket(IP, 8888);
					sendHeart = new sendHeart(server);
					sendHeart.setDaemon(true);
//					sendHeart.start();
					window.getTxtIp().setText("您的SA: "+server.getLocalSocketAddress());
					// 输出流
					pWriter = new PrintWriter(new OutputStreamWriter(server.getOutputStream()));
					// 输入流
					bReader = new BufferedReader(new InputStreamReader(server.getInputStream()));
					// 如果读取数据为空
					while (true) {
						String line = receiveMsg();
						if(line==null||line.equals("")) {
							continue;
						}
						window.appendText(line);
						System.out.println(line);
					}
					// 读完数据之后要关闭

				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (SocketException e) {
					System.out.println("服务器已停止运行");
					window.appendText("服务器已停止运行.");
				} catch (IOException e) {
					e.printStackTrace();
				} 
				
			}
		}.start();
	}

	public void send(String sendMsg) {
		if (pWriter != null) {
			pWriter.write(sendMsg + "\n");
			pWriter.flush();
		} else {
			window.appendText("当前链接已经中断...");
		}
	}

	public String receiveMsg() throws IOException {
		is = server.getInputStream();
//      isr = new InputStreamReader(is);
//      br = new BufferedReader(isr);
		bis = new BufferedInputStream(is);
		byte[] buffer = new byte[1024 * 8];
		int len = 0;
//      StringBuilder sb = new StringBuilder("");
		len = bis.read(buffer);
//      return new String(buffer);
//      while((len=bis.read(buffer))!=-1) {
//      	sb.append(new String(buffer,0,len));
//      	System.out.println(sb);
//      }
		return new String(buffer, 0, len);
//      return br.readLine();
	}

}
