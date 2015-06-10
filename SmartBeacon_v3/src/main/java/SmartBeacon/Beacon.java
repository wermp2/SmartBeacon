package SmartBeacon;

import SmartBeacon.Advertising.BLEAdvertiser;
import SmartBeacon.Advertising.AdvertisingPreparer;
import SmartBeacon.sensors.tilt.TiltSensor;
import SmartBeacon.sensors.NFC.NFCRecord;
import SmartBeacon.sensors.NFC.NFCReader;
import SmartBeacon.sensors.GPS.GPSReceiver;
import ch.quantasy.tinkerforge.tinker.agent.implementation.TinkerforgeStackAgent;
import ch.quantasy.tinkerforge.tinker.application.implementation.AbstractTinkerforgeApplication;
import com.tinkerforge.Device;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import java.util.ArrayList;

/**
 * Actual beacon class. Manages the communication with the Bricklets and chooses
 * how to advertise
 *
 * @author stucn3, wermp2
 */
public class Beacon extends AbstractTinkerforgeApplication{
    
    private BLEAdvertiser advertiser = null;
    private  final TiltSensor tiltSensor;
    private final NFCReader nfcReader;
    private  final GPSReceiver gpsReceiver;
    
    private ArrayList<String> advertisingDataCommands = new ArrayList<>();
    String beaconDataCommand = null;
    
    private Thread t1;
    
  

    /**
     * Constructor
     * @throws TimeoutException
     * @throws NotConnectedException
     */
    public Beacon() throws TimeoutException, NotConnectedException
    {
        this.tiltSensor = new TiltSensor(this);
        this.nfcReader = new NFCReader(this);
        this.gpsReceiver = new GPSReceiver(this);  
        super.addTinkerforgeApplication(this.tiltSensor, this.nfcReader, this.gpsReceiver);
       
    }

    /**
     * Enables searching for an NFC tag
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws NotConnectedException
     */
    public void searchNFCTag() throws InterruptedException, TimeoutException, NotConnectedException {
        System.out.println("beacon, tilt enabled: "+tiltSensor.isEnabled());
        nfcReader.addNFCListener();
        System.out.println("---------------readNFCTag---------------");
        nfcReader.readNFCTag();
    }

    /**
     * Chooses the right data to advertise 
     * @throws TimeoutException
     * @throws NotConnectedException
     * @throws InterruptedException
     */
    public void chooseAdvertisingCase() throws TimeoutException, NotConnectedException, InterruptedException {
        NFCRecord currentNFCRecord = nfcReader.getLatestNFCRecord();
        
        AdvertisingPreparer preparer = new AdvertisingPreparer();
        advertisingDataCommands = null;
        beaconDataCommand = null;

        //if the data on the NFC tag is valid and the NFC tag has UUID, major and minor data
        if (currentNFCRecord.isValidNFCRecord() && currentNFCRecord.hasBeaconData()) {
            //set beacon data of the NFC tag
            preparer.setUuid(currentNFCRecord.getUuid());
            preparer.setMajor(currentNFCRecord.getMajor());
            preparer.setMinor(currentNFCRecord.getMinor());
        } 
        else {
            //set default beacon data
            preparer.setUuid("E2 C5 6D B5 DF FB 48 D2 B0 60 D0 F5 A7 10 96 E0");
            preparer.setMajor("00 00");
            preparer.setMinor("00 00");
        }
        
        //NFC tag is valid, has GPS and additional data
        if (currentNFCRecord.isValidNFCRecord() && currentNFCRecord.hasGPS() && currentNFCRecord.hasData()) {
            System.out.println("NFC record with GPS and additional data");
            preparer.setLongitude(currentNFCRecord.getLongitude());
            preparer.setLatitude(currentNFCRecord.getLatitude());
            preparer.setAltitude(currentNFCRecord.getAltitude());
            preparer.setData(currentNFCRecord.getData());
        } 
        // NFC is valid, has GPS but no additional data
        else if (currentNFCRecord.isValidNFCRecord() && currentNFCRecord.hasGPS() && !currentNFCRecord.hasData()) {
            System.out.println("NFC record with GPS but no additional data");
            preparer.setLongitude(currentNFCRecord.getLongitude());
            preparer.setLatitude(currentNFCRecord.getLatitude());
            preparer.setAltitude(currentNFCRecord.getAltitude());
        } 
        // if the beacon is outdoor, the GPS receiver has the position
        else if (gpsReceiver.isOutdoor()){
            //NFC tag is valid, has no GPS. GPS receiver has position
            if (currentNFCRecord.isValidNFCRecord() && !currentNFCRecord.hasGPS() && currentNFCRecord.hasData()) {
                System.out.println("NFC tag is valid, has no GPS. GPS receiver has position");
                preparer.setLongitude(gpsReceiver.getLongitude());
                preparer.setLatitude(gpsReceiver.getLatitude());
                preparer.setAltitude(gpsReceiver.getAltitude());
                preparer.setData(currentNFCRecord.getData());
            } 
            //NFC tag is not valid. GPS receiver has position
            else if (!currentNFCRecord.isValidNFCRecord()) {
                System.out.println("NFC tag is not valid. GPS receiver has position");
                preparer.setLongitude(gpsReceiver.getLongitude());
                preparer.setLatitude(gpsReceiver.getLatitude());
                preparer.setAltitude(gpsReceiver.getAltitude());
            }
        }
        //Beacon is indoor. NFC tag is valid, has no GPS but additional data 
        else if (!gpsReceiver.isOutdoor() && currentNFCRecord.isValidNFCRecord() && currentNFCRecord.hasData()) {
            System.out.println("Beacon is indoor. NFC tag is valid, has no GPS but additional data ");
            preparer.setData(currentNFCRecord.getData());
        } 
        //Beacon is indoor. NFC tag is not valid. 
        else {
            System.out.println("Case 3a + 4");
            preparer.setData("Error");
        }

        //prepare the commands for advertising
        advertisingDataCommands = preparer.createAdvertisingDataCommmands();
        beaconDataCommand = preparer.createBeaconDataCommand();
        System.out.println(beaconDataCommand);
       
        tiltSensor.enableTiltSensor();
        //if there was already an advertisement
        if (advertiser != null) {
            //stop the advertising thread
            advertiser.terminate();
            //wait for the thread to stop
            advertiser.join();            
        }
        //start advertising with new thread
        advertiser = new BLEAdvertiser(advertisingDataCommands, beaconDataCommand, tiltSensor);
        advertiser.setRunning(true);        
        advertiser.start();      
    }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public void deviceConnected(TinkerforgeStackAgent tinkerforgeStackAgent, Device device) {
       //we don't care here
    }

    @Override
    public void deviceDisconnected(TinkerforgeStackAgent tinkerforgeStackAgent, Device device) {
        //we don't care here
    }
}
