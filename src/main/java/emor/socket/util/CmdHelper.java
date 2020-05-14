package emor.socket.util;

public class CmdHelper {
	static String helpString = "\n输入\"#port#msg\"即可发送私信\n";
	public String getHelp() {
		return helpString;
	}
	public String cmd(String cmd) {
		if(cmd.startsWith("-help")) {
			return getHelp();
		}
		return cmd;
	}
}
