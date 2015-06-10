/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SmartBeacon.Utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public final class HexConverter {
    
    private HexConverter()
    {

    }

    public static String hexStringToUTF8(String hex) 
    {
        //Remove spaces
        hex  = hex.replace(" ", "");

        //convert
        ByteBuffer buff = ByteBuffer.allocate(hex.length() / 2);
        for (int i = 0; i < hex.length(); i += 2) 
        {
            buff.put((byte) Integer.parseInt(hex.substring(i, i + 2), 16));
        }
        buff.rewind();
        Charset cs = Charset.forName("UTF-8"); 
        CharBuffer cb = cs.decode(buff);     
        
        return cb.toString();
    }
    
    public static String shortToHexString(short[] hex)
    {
        StringBuilder sb = new StringBuilder();
        String temp;
        
        for (int i = 0; i < hex.length; i++) 
        {
            temp = Integer.toHexString(hex[i]);
            if(temp.length() < 2)
            {
                temp = "0" + temp;
            }
            
            sb.append(temp + " ");
        }       
        
        String hexString = sb.toString();
        
        return hexString;
    }
    
    public static String UTF8StringToHex(String string)
    {
        
        return null;
    }
    
    

}
