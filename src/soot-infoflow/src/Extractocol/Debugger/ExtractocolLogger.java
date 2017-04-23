package Extractocol.Debugger;

public class ExtractocolLogger {
	public static enum MYCOLOR {BLACK, RED, GREEN, BLUE};
	
	public static void Info(String m){
		System.err.println("[Extractocol] " + m);
	}
	public static void Warn(String m){
		System.err.println("[Extractocol] " + m);
	}
	public static void Log(String m){
		System.out.println("[Extractocol] " + m);
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
		System.out.print("[Extractocol] " + m);
	}
	
	public static void Log(){
		System.out.println();
	}
}
