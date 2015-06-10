package SmartBeacon;

import ch.quantasy.tinkerforge.tinker.agency.implementation.TinkerforgeStackAgency;
import ch.quantasy.tinkerforge.tinker.application.definition.TinkerforgeApplication;
import ch.quantasy.tinkerforge.tinker.core.implementation.TinkerforgeStackAddress;
import com.tinkerforge.AlreadyConnectedException;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Entry point
 *
 * @author stucn3, wermp2
 */
public class SmartBeaconV3 {

    // The 'server'-name of the fridge-sensor-stack
    //public final TinkerforgeStackAddress STACK_ADDRESS = new TinkerforgeStackAddress("1234567890");
    // Assumes to be connected via USB
    public final TinkerforgeStackAddress STACK_ADDRESS2 = new TinkerforgeStackAddress(
            "localhost");

    private final TinkerforgeApplication beacon;

    public SmartBeaconV3() throws TimeoutException, NotConnectedException {
        this.beacon = new Beacon();
    }

    public void start() {
        TinkerforgeStackAgency.getInstance().getStackAgent(STACK_ADDRESS2).addApplication(beacon);
    }

    public void stop() {
        TinkerforgeStackAgency.getInstance().getStackAgent(STACK_ADDRESS2).removeApplication(beacon);
    }

    /**
     * Main class
     *
     * @param args
     * @throws IOException
     * @throws UnknownHostException
     * @throws AlreadyConnectedException
     * @throws TimeoutException
     * @throws NotConnectedException
     */
    public static void main(String[] args) throws IOException, UnknownHostException, AlreadyConnectedException, TimeoutException, NotConnectedException {
        final SmartBeaconV3 manager = new SmartBeaconV3();
	manager.start();
	System.in.read();
	manager.stop();;
    }

}
