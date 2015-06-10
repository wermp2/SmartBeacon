/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SmartBeacon;

import com.tinkerforge.AlreadyConnectedException;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import java.io.IOException;
import java.net.UnknownHostException;

/**
 *
 * @author Pixel
 */
public class SmartBeaconv1 {
    
    private static final String HOST = "localhost";
    private static final int PORT = 4223;
    private static final String UID_gps = "qGj"; // Change to your UID
    private static final String UID_tilt = "jaj"; // Change to your UID
    private static final String UID_master = "5VkYJ6"; // Change to your UID
    private static final String UID_nfc = "p7k"; // Change to your UID

    public static void main(String[] args) throws IOException, UnknownHostException, AlreadyConnectedException, TimeoutException, NotConnectedException 
    {
       Beacon iBeacon = new Beacon(HOST, PORT, UID_master, UID_tilt, UID_nfc, UID_gps);
       System.out.println("Press key to exit"); System.in.read();
    }
    
}
