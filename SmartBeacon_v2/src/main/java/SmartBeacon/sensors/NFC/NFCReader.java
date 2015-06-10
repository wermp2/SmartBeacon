package SmartBeacon.sensors.NFC;

import SmartBeacon.Utils.HexConverter;
import com.tinkerforge.BrickletNFCRFID;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import SmartBeacon.Beacon;

/**
 * Class which gets the data of the NFC Bricklet and checks if the NFC tag has valid data or not
 * @author stucn3, wermp2
 */
public class NFCReader {
    
    private final BrickletNFCRFID nfc;
    private NFCReader.NfcListener nfcListener = null;   
    private NFCRecord latestRecord = null;
    private final Beacon beacon;
    
    /**
     * Constructor
     * @param beacon    The Beacon
     * @param uidNFC    The ID of the NFC Bricklet
     * @param ipcon     The IPConnection
     */
    public NFCReader(Beacon beacon, String uidNFC, IPConnection ipcon){
        nfc = new BrickletNFCRFID(uidNFC, ipcon);
        this.beacon = beacon;
    }
    
    /**
     * Returns the latest data of the NFC Bricklet
     * @return the latest data of the NFC Bricklet
     */
    public NFCRecord getLatestNFCRecord(){
        return latestRecord;
    }
    
    /**
     * Adds the listener for NFC data
     * @throws TimeoutException
     * @throws NotConnectedException
     */
    public void addNFCListener() throws TimeoutException, NotConnectedException{
       nfcListener = new NFCReader.NfcListener(); 
       nfc.addStateChangedListener(nfcListener);
       System.out.println("nfc listener hinzugefügt");
    }
    
    /**
     * Removes the listener for NFC data
     */
    public void removeNFCListener(){
        nfc.removeStateChangedListener(nfcListener);
        nfcListener = null;
    }
    
    /**
     * Reads the data of an NFC tag
     * @throws TimeoutException
     * @throws NotConnectedException
     */
    public void readNFCTag() throws TimeoutException, NotConnectedException{
        nfc.requestTagID(BrickletNFCRFID.TAG_TYPE_TYPE2);
    }
    
    /**
     * The NFC Listener class used to detect if a nfc tag is placed on the nfc reader.
     * If a NFC tag could be found, the tag will be read an saved as latestNFCRecord.
     * After this the NFCReader instructs the beacon class to decide on an advertising case.
    */
    private class NfcListener implements BrickletNFCRFID.StateChangedListener{
        short currentTagType = 0;
        int page = 4;
        String completeNfcRecord = "";
        
        int readingAttempts = 0;

        @Override
        public void stateChanged(short state, boolean idle) 
        {        
            try {
                
                if (state == BrickletNFCRFID.STATE_REQUEST_TAG_ID_READY) {

                    nfc.requestPage(page);

		} else if (state == BrickletNFCRFID.STATE_REQUEST_PAGE_READY) {
                    // Get and print pages
                    System.out.println("read tag pages..");
                    short[] nfcData = nfc.getPage();  
                    String pageDataString = HexConverter.shortToHexString(nfcData);
                    String record = "";
                    
                    if (page == 4) {
                        System.out.println(pageDataString);
                        Pattern p = Pattern.compile("03 [a-fA-F0-9]{2} d1 01 [a-fA-F0-9]{2} 54( [a-fA-F0-9]{2}){10}");
                        //Pattern p = Pattern.compile("03 .{2} d1 01(.*)");
                        Matcher m = p.matcher(pageDataString);

                        if (m.find()) {
                            //omitt first 26 characters
                            record = pageDataString.substring(26);
                        }
                        
                        else{
                            //completeNFCrecord is nothing, so the json will be invalid
                            extractJSONdata(completeNfcRecord);
                            nfc.removeStateChangedListener(this);     
                            beacon.chooseAdvertisingCase();
                        }
                    } 
                    else{record = pageDataString;}                                          
                    
                    //if record is not finished
                    if(!record.contains("fe"))
                    {
                        page = page + 4;
                        completeNfcRecord = completeNfcRecord + record;   
                        nfc.requestTagID(BrickletNFCRFID.TAG_TYPE_TYPE2);
                    } 
                    else
                    {
                       String recordData = record.split("fe")[0];
                       completeNfcRecord = completeNfcRecord + recordData;
                       extractJSONdata(completeNfcRecord);
                       System.out.println("entferne nfc listener..");
                       nfc.removeStateChangedListener(this);     
                       beacon.chooseAdvertisingCase();
                    }

                } else if ((state & (1 << 6)) == (1 << 6)) {
                    // All errors have bit 6 set
                    System.out.println("Error: " + "Kein NFC Tag gefunden");
                    if(readingAttempts < 5)
                    {
                        Thread.sleep(1000); 
                        nfc.requestTagID(BrickletNFCRFID.TAG_TYPE_TYPE2);
                        readingAttempts++;
                    }
                    if(readingAttempts == 5)
                    {
                        nfc.removeStateChangedListener(this);
                        latestRecord = new NFCRecord(null, false);
                        beacon.chooseAdvertisingCase();
                    }
                }
            } catch (TimeoutException | NotConnectedException ex) {
                Logger.getLogger(Beacon.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(NFCReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
    * Used to extract a JSON object from a nfcd string.
    * The extracted JSON object will be stored in the latestNFCRecord variable.
    * Throws an error if no valid json format could be found.
    * @param nfcd 
    */
    private void extractJSONdata(String nfcd)
    {
        String jsonString = HexConverter.hexStringToUTF8(nfcd);
        
        if(!isValidJSONFormat(jsonString))
        {
            this.latestRecord = new NFCRecord(null, false);
            //this.nfcTagDataHexString = "4e6f2076616c6964204a534f4e20666f726d6174";
            System.out.println("invalid");
        }
        else
        {
            JSONObject jsonObj = new JSONObject(jsonString);
            this.latestRecord = new NFCRecord(jsonObj, true);
            System.out.println("Valid");
        }        
    }
    
    private boolean isValidJSONFormat(String json)
    {
        try 
        {
            new JSONObject(json);
        } catch (JSONException ex) {
           return false;
        }
        return true;
    }
    
}
