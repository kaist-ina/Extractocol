package Extractocol.BufferExtractor_Request.Helper;

import java.io.File;

public class FileFinder {
	public static String SearchFile(File f, String Filename) {
		
		String out;
		
		if (f.isDirectory()) {
			String[] s = f.list();

			for (int i = 0; i < s.length; i++) {
				File f1 = new File(f.getPath() + "/" + s[i]);

				if (f1.isDirectory()) {
					out = SearchFile(f1, Filename);
					if (out != null)
						return out;
				} else {
					if (Filename.equals(f1.getName())) {
						out = f1.getPath();
						return out;
					}
				}
			}
		} else {
			if (Filename.equals(f.getName())) {
				out = f.getPath();
				return out;
			}
		}
		return null;
	}
}
