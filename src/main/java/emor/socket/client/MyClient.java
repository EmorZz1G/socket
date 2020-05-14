package emor.socket.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

import emor.socket.user.User;
import emor.socket.util.CmdHelper;

/**
 * Created by Emor
 */
@SuppressWarnings("unused")
public class MyClient {
	private User user =null;
	private Socket server = null;
	private CmdHelper cmdHelper = new CmdHelper();
	private OutputStream os;
	private PrintWriter pw;
	private InputStream is;
	private InputStreamReader isr;
	private BufferedReader br;
	private BufferedOutputStream bos;
	private BufferedInputStream bis;
	private ObjectOutputStream oos;
	private Thread receiveMessageListener = null;
	private Thread sendMessageListener = null;
	private Thread sendHeart = null;
	private String serverName = "";

	@SuppressWarnings("deprecation")
	private void start() {
		try {
			// 连接服务器
			server = new Socket("127.0.0.1", 8888);
			server.setKeepAlive(true);
			server.setSoTimeout(0);
			user = new User(null, server.getLocalPort());
			serverName = server.getRemoteSocketAddress().toString();
			System.out.println("连接服务器成功，身份证：" + server.getLocalSocketAddress());
			// 启动接受消息的线程，设置为守护线程
			receiveMessageListener = new ReceiveMessageListener();
            receiveMessageListener.setDaemon(true);
			receiveMessageListener.start();
			// 启动发送消息的线程，设置为守护线程
			sendMessageListener = new SendMessageListener();
            sendMessageListener.setDaemon(true);
			sendMessageListener.start();
			sendHeart = new sendHeart(server);
			sendHeart.setDaemon(true);
//			sendHeart.start();
			
			receiveMessageListener.join();
			receiveMessageListener.join();
			sendMessageListener.join();
		} catch (SocketException e) {
			System.out.println("服务器" + serverName + "已停止运行");
		}catch (NullPointerException e) {
			// TODO: handle exception
			System.out.println("连接失败");
		} catch (IOException e) {
			// log
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				//压制警告
				receiveMessageListener.stop();
				sendMessageListener.stop();
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("请重试！");
			}
		}
	}
	class sendHeart extends Thread{
		Socket server = null;
		sendHeart(Socket server){
			this.server = server;
		}
		@Override
		public void run() {
			while(true){
				try {
					oos = new ObjectOutputStream(server.getOutputStream());
					oos.writeObject(user);
					oos.close();
					System.out.println("目前处于链接状态！");
					Thread.sleep(3000);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					System.out.println("连接已关闭");
				} finally{
					
				}
			}
		}
	}
    /**
     * 发送消息的线程
     */
    class SendMessageListener extends Thread {
        @Override
        public void run() {
        	@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
            try {
                // 监听idea的console输入
                // 循环处理，只要有输入内容就立即发送
                while (true) {
                    sendMsg(scanner.next());
                }
            } catch (SocketException e) {
                System.out.println("服务器" + server.getRemoteSocketAddress() + "已停止运行");
            } catch (IOException e) {
                // log
            	e.printStackTrace();
            } finally {
//            	scanner.close();

            }
        }
    }

	/**
	 * 接受消息的线程
	 */
	class ReceiveMessageListener extends Thread {
		@Override
		public void run() {
			try {
				// 循环监听，除非掉线，或者服务器宕机了
				while (true) {
					String receiveMsg = receiveMsg();
					System.out.println(receiveMsg);
					if (receiveMsg != null&&receiveMsg.equals(""))
						System.out.println(receiveMsg);
				}
			} catch (SocketException e) {
				System.out.println("服务器" + server.getRemoteSocketAddress() + "已停止运行");
			} catch (IOException e) {
				// log
				e.printStackTrace();
			}
		}
	}

	/**
	 * 发送消息
	 *
	 * @param msg 消息内容
	 * @throws IOException
	 */
	private void sendMsg(String msg) throws IOException {
		os = server.getOutputStream();
		pw = new PrintWriter(os);
		pw.println(msg);
		pw.flush();
	}

	/**
     * 接受消息
     *
     * @return 消息内容
     * @throws IOException
     */
    private String receiveMsg() throws IOException {
        is = server.getInputStream();
//        isr = new InputStreamReader(is);
//        br = new BufferedReader(isr);
        bis = new BufferedInputStream(is);
        byte[] buffer = new byte[1024*8];
        int len = 0;
//        StringBuilder sb = new StringBuilder("");
        len = bis.read(buffer);
//        return new String(buffer);
//        while((len=bis.read(buffer))!=-1) {
//        	sb.append(new String(buffer,0,len));
//        	System.out.println(sb);
//        }
        return new String(buffer,0,len);
//        return br.readLine();
    }

	public static void main(String[] args) {
		new MyClient().start();
	}
}