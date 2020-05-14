package emor.socket.window;
 
import java.awt.EventQueue;
 
 
public class StartClient {
 
	public static void main(String[] args) {
	  EventQueue.invokeLater(new Runnable() {
		
		@Override
		public void run() {
			try {
				MyClientWindow frame = new MyClientWindow();
				frame.setVisible(true);
				ConnectionManager.getChatManager().setWindow(frame);
			}
			catch (Exception e) {
			}
		}
	});
	  
	}
}
