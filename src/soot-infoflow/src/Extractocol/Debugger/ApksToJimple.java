package Extractocol.Debugger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import soot.Timers;
import soot.options.Options;
public class ApksToJimple {
	public static String category_file = "C:/Users/INA/Desktop/categorized";
	public static String output_file = "C:/Users/INA/Desktop/categorized_decompile";
	public static String androidSDK = "D:/APF_REPO/adt-bundle-windows-x86-20140321/sdk/platforms";
	
	public void OnceConverter()
	{
		
	}
	
	public void LoopConverter() {
		/*
		-src-prec apk
		-output-format shimple
		-allow-phantom-refs

		-android-jars
		/Users/hnamkoong/Desktop/AndroidSDK
		
		-process-dir
		/Users/hnamkoong/Desktop/categorized/Weather/com.acmeaom.android.myradar.apk
		
		-d /Users/hnamkoong/Desktop/acmeaom-shimple
		
		-x android
		 */

//		apkToShimple(null, null);
		
		File outputFile = new File(output_file);
		outputFile.mkdir();
		
		File dir = new File(category_file);
		File[] fileList = dir.listFiles();
		try{
			for(int i = 0 ; i < fileList.length ; i++){
				File file = fileList[i];
				if(file.isDirectory()) {
					String sub_category_file = file.getCanonicalPath().toString();
					File sub_category_dir = new File(sub_category_file); 
					File[] sub_category_fileList = sub_category_dir.listFiles(); 
					
					File sub_category_output_file = new File( outputFile.getAbsolutePath() + File.separator + file.getName());
					sub_category_output_file.mkdir();

					
					for(int j=0; j<sub_category_fileList.length; j++) {
						File apkfile = sub_category_fileList[j];
						if(apkfile.isFile()) {
							String apkFilePath = apkfile.getPath();							
							String jimpleOutputDirPath = sub_category_output_file + File.separator + apkfile.getName() + "_jimple";
							String shimpleOutputDirPath = sub_category_output_file + File.separator + apkfile.getName() + "_shimple";
							apkToJimple(apkFilePath, jimpleOutputDirPath);
							apkToShimple(apkFilePath, shimpleOutputDirPath);
						}
					}
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public void apkToShimple(String apkFilePath, String outputDirPath)
	{
		Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_output_format(Options.output_format_shimple);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_android_jars(androidSDK);

        List<String> list = new ArrayList<String>();
        list.add(apkFilePath);
        Options.v().set_process_dir(list);
        Options.v().set_output_dir(outputDirPath);
        
        String[] a = new String[1];
        a[0] = "1";
        soot.Main.main(a);
	}

	public void apkToJimple(String apkFilePath, String outputDirPath)
	{
		Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_android_jars(androidSDK);

        List<String> list = new ArrayList<String>();
        list.add(apkFilePath);
        Options.v().set_process_dir(list);
        Options.v().set_output_dir(outputDirPath);
        
        String[] a = new String[1];
        a[0] = "1";
        soot.Main.main(a);
	}
	
	public void reset()
	{
		soot.G.reset();
	}
}
