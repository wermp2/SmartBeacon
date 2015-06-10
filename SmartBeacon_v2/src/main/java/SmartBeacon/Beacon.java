package SmartBeacon;

import SmartBeacon.Advertising.BLEAdvertiser;
import SmartBeacon.Advertising.AdvertisingPreparer;
import SmartBeacon.sensors.tilt.TiltSensor;
import SmartBeacon.sensors.NFC.NFCRecord;
import SmartBeacon.sensors.NFC.NFCReader;
import SmartBeacon.sensors.GPS.GPSReceiver;
import com.tinkerforge.AlreadyConnectedException;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Actual beacon class. Manages the communication with the Bricklets and chooses
 * how to advertise
 *
 * @author stucn3, wermp2
 */
public class Beacon {

    private final String HOST;
    private final int PORT;
    private final String UID_master;

    private final IPConnection ipcon;

    private BLEAdvertiser advertiser;
    private final TiltSensor tiltSensor;
    private final NFCReader nfcReader;
    private final GPSReceiver gpsReceiver;

    private NFCRecord currentNFCRecord = null;
    
    private ArrayList<String> advertisingDataCommands = new ArrayList<>();
    String beaconDataCommand = null;

    /**
     * Constructor
     * @param HOST          The host (eg. localhost)
     * @param PORT          The port for connection
     * @param UID_master    The ID of the Master Brick
     * @param UID_tilt      The ID of the Tilt Bricklet
     * @param UID_nfc       The ID of the NFC Bricklet
     * @param UID_gps       The ID of the GPS Bricklet
     * @throws IOException
     * @throws UnknownHostException
     * @throws AlreadyConnectedException
     * @throws TimeoutException
     * @throws NotConnectedException
     */
    public Beacon(String HOST, int PORT, String UID_master, String UID_tilt, String UID_nfc, String UID_gps) throws IOException, UnknownHostException, AlreadyConnectedException, TimeoutException, NotConnectedException {
        this.HOST = HOST;
        this.PORT = PORT;
        this.UID_master = UID_master;

        ipcon = new IPConnection();

        tiltSensor = new TiltSensor(this, UID_tilt, ipcon);
        nfcReader = new NFCReader(this, UID_nfc, ipcon);
        gpsReceiver = new GPSReceiver(UID_gps, ipcon);

        ipcon.connect(HOST, PORT);

        tiltSensor.addNewTiltListener();

    }

    /**
     * Enables searching for an NFC tag
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws NotConnectedException
     */
    public void searchNFCTag() throws InterruptedException, TimeoutException, NotConnectedException {
        System.out.println("tilt enabled: " + tiltSensor.isEnabled());
        nfcReader.addNFCListener();
        nfcReader.readNFCTag();
        System.out.println("tilt enabled: " + tiltSensor.isEnabled());
    }

    /**
     * Chooses the data to advertise
     * @throws TimeoutException
     * @throws NotConnectedException
     * @throws InterruptedException
     */
    public void chooseAdvertisingCase() throws TimeoutException, NotConnectedException, InterruptedException {
        currentNFCRecord = nfcReader.getLatestNFCRecord();
        
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
        if (gpsReceiver.isOutdoor()){
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
       
        tiltSensor.enableTiltSensor();
        
        //start advertising
        advertiser = new BLEAdvertiser(advertisingDataCommands, beaconDataCommand, tiltSensor);
        Thread t1 = new Thread(advertiser);
        t1.start();
    }
}
