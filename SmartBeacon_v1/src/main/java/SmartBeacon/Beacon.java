/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SmartBeacon;

import SmartBeacon.Advertising.BLEAdvertiser;
import SmartBeacon.sensors.GPS.GPSReceiver;
import SmartBeacon.sensors.NFC.NFCReader;
import SmartBeacon.sensors.NFC.NFCRecord;
import com.tinkerforge.AlreadyConnectedException;
import com.tinkerforge.BrickMaster;
import com.tinkerforge.BrickletGPS;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import java.io.IOException;
import java.net.UnknownHostException;
import SmartBeacon.sensors.tilt.TiltSensor;


public class Beacon {
    
    private final   String HOST;
    private final   int PORT;
    private final   String UID_master;
    
    private final IPConnection ipcon;
    
    private  BLEAdvertiser advertiser;
    private final TiltSensor tiltSensor;
    private final NFCReader nfcReader;
    private final GPSReceiver gpsReceiver;
    
    //private NFCRecord currentNFCRecord = null;

    Beacon(String HOST, int PORT, String UID_master, String UID_tilt, String UID_nfc, String UID_gps) throws IOException, UnknownHostException, AlreadyConnectedException, TimeoutException, NotConnectedException 
    {
        this.HOST = HOST;
        this.PORT = PORT;
        this.UID_master = UID_master;
        
        //Für Test
        advertiser = new BLEAdvertiser("ich bin ein beacon", "E2C56DB5DFFB48D2B060D0F5A71096E0", "00 00", "00 00");
        
        ipcon = new IPConnection(); 
        
        tiltSensor = new TiltSensor(this, UID_tilt, ipcon);
        nfcReader = new NFCReader(this, UID_nfc, ipcon);
        gpsReceiver = new GPSReceiver(UID_gps,ipcon);
        
        ipcon.connect(HOST, PORT); 
        
        tiltSensor.addNewTiltListener();
    }
    
    
    public void searchNFCTag() throws InterruptedException, TimeoutException, NotConnectedException
    {       
        nfcReader.addNFCListener();
        nfcReader.readNFCTag();
    }
    
    public void chooseAdvertisingCase() throws TimeoutException, NotConnectedException, InterruptedException
    {
        NFCRecord currentNFCRecord = nfcReader.getLatestNFCRecord();
        String advertisement = "{\"Adv\":{\"GPS\":{\"long\":\"00.000000\",\"lat\":\"00.000000\",\"alt\":\"0000.00\"},\"data\":\"xxx\"}}";     
        
        if(currentNFCRecord.isValidNFCRecord() && currentNFCRecord.hasGPS() && currentNFCRecord.hasData())
        {
            //Fall 1: NFC Record mit GPS und Daten
            System.out.println("Fall 1: NFC Record mit GPS und Daten");
            advertisement = "{\"Adv\":{\"GPS\":{\"long\":"+  currentNFCRecord.getLongitude() +",\"lat\":" + currentNFCRecord.getLatitude() + ",\"alt\":" + currentNFCRecord.getAltitude() +"},\"data\":"+ currentNFCRecord.getData() +"}}";          
        }
        else if (gpsReceiver.isOutdoor()) //GPS aktivieren nötig
        {
            //Fall 2: Valid NFC tag without position, GPS position found
            if(currentNFCRecord.isValidNFCRecord() && !currentNFCRecord.hasGPS() && currentNFCRecord.hasData())
            {
                System.out.println("Fall 2: Valid NFC tag without position, GPS position found");
                advertisement = "{\"Adv\":{\"GPS\":{\"long\":"+  gpsReceiver.getLongitude() +",\"lat\":" + gpsReceiver.getLatitude() + ",\"alt\":" + gpsReceiver.getAltitude() +"},\"data\":"+ currentNFCRecord.getData() +"}}";

            }
            //Fall 3b: No valid NFC tag found but valid gps position
            else if(!currentNFCRecord.isValidNFCRecord())
            {
                System.out.println("Fall 3b: No valid NFC tag found but valid gps position");
                advertisement = "{\"Adv\":{\"GPS\":{\"long\":"+  gpsReceiver.getLongitude() +",\"lat\":" + gpsReceiver.getLatitude() + ",\"alt\":" + gpsReceiver.getAltitude() +"}}}";
            }        
        }
        //Fall 5: valid nfc tag found but without GPS data, no GPS position found
        else if(!gpsReceiver.isOutdoor() && currentNFCRecord.isValidNFCRecord() && currentNFCRecord.hasData())
        {
            System.out.println("Fall 5");
            advertisement = "{\"Adv\":{\"data\":{\"data\":"+ currentNFCRecord.getData() +"}}}";
        }       
        //Fall 3a/4: No NFC Tag found & no GPS position found -> Error
        else
        {
            System.out.println("Fall 3a + 4");
            advertisement = "{\"Adv\":{\"data\":{\"data\":Error}}}";
        }   
       
        setNewAdvertisement(advertisement);
    }
    
    public void setNewAdvertisement(String adv) throws TimeoutException, NotConnectedException
    {
        System.out.println("Advertisement set: " + adv);
        advertiser = new BLEAdvertiser(adv, "E2C56DB5DFFB48D2B060D0F5A71096E0", "00 00", "00 00");
        tiltSensor.enableTiltSensor();
    }
}
