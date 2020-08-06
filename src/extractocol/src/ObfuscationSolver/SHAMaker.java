package ObfuscationSolver;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHAMaker {
    public static String make(String plain)
    {
        String SHA = "";
        try {
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(plain.getBytes());
            byte byteData[] = sh.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0 ; i < byteData.length; i++)
            {
                sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
            }
            SHA = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            SHA = null;
        }
        return SHA;
    }
}
