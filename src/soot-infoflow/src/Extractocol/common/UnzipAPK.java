package Extractocol.common;

import Extractocol.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnzipAPK
{
	public static void UnzipAPK(String path, String basePath) throws Exception
	{
		String ClonePath = CopyAPK(path);
		
		FileInputStream fis = null;
		ZipInputStream zis = null;
		ZipEntry zentry = null;
		
		try {
			fis = new FileInputStream(ClonePath);
			zis = new ZipInputStream(fis);
			
			int i = 2;
			
			while ((zentry = zis.getNextEntry()) != null)
			{
				
				String fileNameToUnzip = zentry.getName();
				
				if (fileNameToUnzip.contains(".dex") && !fileNameToUnzip.contains("classes.dex"))
				{
					File targetFile = new File("../../SerializationFiles/" + Constants.getMultiDexName(basePath, i++));
					unzipEntry(zis,targetFile);
				}
			}
		} 
		finally {
			if (zis != null)
				zis.close();
			
			if (fis != null)
				fis.close();
			
			File clone = new File(ClonePath);
			clone.delete();
		}
	}
	
	protected static File unzipEntry(ZipInputStream zis, File targetFile) throws Exception
	{
		FileOutputStream fos = null;
		try 
		{
			fos = new FileOutputStream(targetFile);
			byte[] buffer = new byte[1024*2];
			int len = 0;
			while ((len = zis.read(buffer)) != -1)
			{
				fos.write(buffer, 0, len);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		finally {
			if (fos != null){
				fos.close();
			}
		}
		return targetFile;
	}
	
	public static String CopyAPK(String path)
	{
		try{
			FileInputStream fis = new FileInputStream(path);
			FileOutputStream fos = new FileOutputStream(path.replace("apk", "zip"));
			
			byte[] data = new byte[1024];
			int length = 0;
			while((length = fis.read(data)) > 0)
			{
				fos.write(data, 0 , length);
			}
			fis.close();
			fos.close();
			
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return path.replace("apk", "zip");
	}
}
