package SmartBeacon;

import com.tinkerforge.AlreadyConnectedException;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Entry point
 * @author stucn3, wermp2
 */
public class SmartBeaconv2 {
    
    private static final String HOST = "localhost";
    private static final int PORT = 4223;
    private static final String UID_gps = "qGx"; // Change to your UID
    private static final String UID_tilt = "jau"; // Change to your UID
    private static final String UID_master = "68cv96"; // Change to your UID
    private static final String UID_nfc = "oGS"; // Change to your UID

    /**
     * Main class
     * @param args
     * @throws IOException
     * @throws UnknownHostException
     * @throws AlreadyConnectedException
     * @throws TimeoutException
     * @throws NotConnectedException
     */
    public static void main(String[] args) throws IOException, UnknownHostException, AlreadyConnectedException, TimeoutException, NotConnectedException 
    {
       Beacon iBeacon = new Beacon(HOST, PORT, UID_master, UID_tilt, UID_nfc, UID_gps);
       System.out.println("Press key to exit"); System.in.read();
    }
    
}
