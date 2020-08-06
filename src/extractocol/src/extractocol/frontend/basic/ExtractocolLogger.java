package extractocol.frontend.basic;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ExtractocolLogger {
	public static enum MYCOLOR {BLACK, RED, GREEN, BLUE};
	
	public static void Info(String m){
		System.err.println(prefix() + m);
	}
	public static void Warn(String m){
		StackTraceElement i = new Throwable().getStackTrace()[1];
		System.err.println(prefix() + "[" + i.getClassName() + ":" + i.getMethodName() + "] "+ m);
	}
	public static void Log(String m){
		System.out.println(prefix() + m);
	}
	
	public static void coloredLog(String m, MYCOLOR c, boolean adopt){
		if(adopt){
			switch(c){
			case BLACK: System.out.print("\033[0m"); break;
			case RED: System.out.print("\033[1;31m"); break;
			case GREEN: System.out.print("\033[32m"); break;
			case BLUE: System.out.print("\033[1;34m"); break;
			default: System.out.print("\033[0m"); break;
			}
			
			System.out.println(m);
			System.out.print("\033[0m");
		}else
			System.out.println(m);
	}
	
	public static void coloredLog(String m, MYCOLOR c){
		coloredLog(m, c, true);
	}
	
	public static void LogNoLineBreak(String m){
		System.out.print(prefix() + m);
	}
	
	public static void Log(){
		System.out.println();
	}
	
	private static String prefix() {
		return "[E][" + getCurrentTime() + "] ";
	}
	
	private static String getCurrentTime() {
		return (new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
	}
}
