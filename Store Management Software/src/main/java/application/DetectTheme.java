package application;

import java.io.IOException;
import java.io.InputStream;

public class DetectTheme {

	public enum REG_TYPE {
		REG_BINARY, REG_DWORD, REG_EXPAND_SZ, REG_MULTI_SZ, REG_SZ
	}

	private final static String OS_NAME = System.getProperty("os.name", "").toLowerCase();
	public static final boolean IS_WINDOWS = OS_NAME.contains("windows");
	public static final boolean IS_MACOS = OS_NAME.contains("mac os x");
	public static final boolean IS_LINUX = OS_NAME.contains("linux");

	private static boolean execute(StringBuilder stdin, StringBuilder stderr, String cmd) {
		stdin.setLength(0);
		stderr.setLength(0);
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			InputStream is = p.getInputStream();
			InputStream es = p.getErrorStream();
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				stderr.append(e.toString());
			}
			while (is.available() > 0) {
				stdin.append((char) is.read());
			}
			while (es.available() > 0) {
				stderr.append((char) es.read());
			}
			return true;
		} catch (IOException e) {
			stderr.append(e.toString());
		}
		return false;
	}

	private static String getWindowsTheme(String key, String value, REG_TYPE type, StringBuilder stderr) {
		StringBuilder stdin = new StringBuilder();
		String cmd = String.format("reg query \"%s\" /v %s", key, value);
		if (execute(stdin, stderr, cmd)) {
			String temp = stdin.toString();
			String stringType = type.toString();
			int pos = temp.indexOf(stringType);
			if (pos >= 0)
				return temp.substring(pos + stringType.length()).trim();
		}
		return "";
	}

	private static String getMacOSTheme(String key, StringBuilder stderr) {
		StringBuilder stdin = new StringBuilder();
		String cmd = String.format("defaults read -g %s", key);
		if (execute(stdin, stderr, cmd))
			return stdin.toString().trim();

		return "";
	}

	public static boolean isDarkModeActive() {
		StringBuilder stderr = new StringBuilder();
		if (IS_WINDOWS)
			return getWindowsTheme(
					"HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
					"AppsUseLightTheme", REG_TYPE.REG_DWORD, stderr).equalsIgnoreCase("0x0");
		else if (IS_MACOS)
			return (getMacOSTheme("AppleInterfaceStyle", stderr).toLowerCase().contains("dark"));
		else if (IS_LINUX)
			return (getLinuxTheme("/org/gnome/desktop/interface/gtk-theme", stderr).toLowerCase().contains("dark"));
		return false;
	}

	private static String getLinuxTheme(String key, StringBuilder stderr) {
		StringBuilder stdin = new StringBuilder();
		String cmd = String.format("dconf read %s", key);
		if (execute(stdin, stderr, cmd))
			return stdin.toString().trim();
		return "";
	}

}
