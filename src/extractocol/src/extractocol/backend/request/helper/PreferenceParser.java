
package extractocol.backend.request.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PreferenceParser
{
	private String DecompileFolder = "";
	private String APKPath = "";
	private String SearchKey = "";
	private final String Rfile = "R$string.smali";
	private String ResourcePath;
	public static void main(String args[]) throws IOException
	{
		PreferenceParser PP = new PreferenceParser(
				"D:\\Dropbox\\Dropbox\\Android_Apk_repo\\Ground_Truth\\Selected\\[9]_Adblock_Plus\\org.adblockplus.android_270.apk",
				"2131165279");
		// String APkPath = ;
		// PP.DecompileXML();
		PP.SearchConstantString();
	}
	public PreferenceParser(String _ApkPath, String _SearchKey)
	{
		APKPath = _ApkPath;
		SearchKey = _SearchKey;
		DecompileFolder = APKPath.substring(APKPath.lastIndexOf("\\") + 1);
		ResourcePath = new String(DecompileFolder.replace(".apk", "")) + "\\res\\values\\strings.xml";
		String[] name = DecompileFolder.replace(".apk", "").split("\\.");
		DecompileFolder = DecompileFolder.replace(".apk", "") + "\\smali\\";
		// last is not included.
		for (int i = 0; i < name.length - 1; i++)
		{
			DecompileFolder += name[i] + "\\";
		}
		// + "\\res\\" + "values\\";
	}
	public String SearchConstantString() throws IOException
	{
		String ElementName = SearchRString();
		try
		{
			File file = new File(ResourcePath);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			// System.out.println("Root element "
			// + doc.getDocumentElement().getNodeName());
			NodeList nodeLst = doc.getElementsByTagName("string");
			for (int s = 0; s < nodeLst.getLength(); s++)
			{
				Node fstNode = nodeLst.item(s);
				// System.out.println(fstNode.getAttributes().getNamedItem("name").getNodeValue());
				if (fstNode.getAttributes().getNamedItem("name").getNodeValue().equals(ElementName))
				{
					return fstNode.getTextContent();
				}
				// if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
				// Element fstElmnt = (Element) fstNode;
				// NodeList fstNmElmntLst = fstElmnt
				// .getElementsByTagName("PreferenceCategory");
				// Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
				// NodeList fstNm = fstNmElmnt.getChildNodes();
				// // System.out.println("URL : "
				// // + ((Node) fstNm.item(0)).getNodeValue());
				// }
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	private String SearchRString() throws IOException
	{
		File deDir = new File(DecompileFolder);
		String ResourcePath = FileFinder.SearchFile(deDir, Rfile);
		@SuppressWarnings("resource")
		BufferedReader br = new BufferedReader(new FileReader(new File(ResourcePath)));
		String line;
		String Hex = "0x" + String.format("%x", Long.parseLong(SearchKey));
		while ((line = br.readLine()) != null)
		{
			if (line.indexOf(Hex) != -1)
			{
				break;
			}
		}
		String resourceString = line.split(":")[0];
		if (resourceString != null)
			resourceString = resourceString.substring(resourceString.lastIndexOf(" ") + 1);
		return resourceString;
	}
	public void DecompileXML()
	{
		String cmd[] = new String[3];
		cmd[0] = "cmd.exe";
		cmd[1] = "/C";
		cmd[2] = "java -jar d:\\apktool.jar -f d " + APKPath;
		// -jar AXMLPrinter2.jar /Users/aaaaa/Downloads/call_in.xml >
		// /Users/aaaaa/Downloads/asdfg/call_in.xml
		// String[] cmd = { "cmd", "/c", "tasklist", "/?" };
		// String[] cmd = { "cmd", "/c", "tasklist111", "/?" };
		Process process = null;
		try
		{
			process = new ProcessBuilder(cmd).start();
			SequenceInputStream seqIn = new SequenceInputStream(process.getInputStream(), process.getErrorStream());
			// Scanner s = new Scanner(process.getInputStream());
			// Scanner s = new Scanner(process.getErrorStream());
			Scanner s = new Scanner(seqIn);
			while (s.hasNextLine() == true)
			{
				// System.out.println(s.nextLine());
				s.nextLine();
			}
			s.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
