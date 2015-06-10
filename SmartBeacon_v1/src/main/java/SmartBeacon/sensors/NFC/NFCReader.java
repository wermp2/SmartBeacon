/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SmartBeacon.sensors.NFC;

import SmartBeacon.Beacon;
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


public class NFCReader {
    
    private final BrickletNFCRFID nfc;
    private NFCReader.NfcListener nfcListener = null;   
    private NFCRecord latestRecord = null;
    private final Beacon beacon;
    
    public NFCReader(Beacon beacon, String uidNFC, IPConnection ipcon)
    {
        nfc = new BrickletNFCRFID(uidNFC, ipcon);
        this.beacon = beacon;
    }
    
    public NFCRecord getLatestNFCRecord()
    {
        return latestRecord;
    }
    
    public void addNFCListener() throws TimeoutException, NotConnectedException
    {
       nfcListener = new NFCReader.NfcListener(); 
       nfc.addStateChangedListener(nfcListener);
       System.out.println("nfc listener hinzugefügt");
    }
    
    public void removeNFCListener()
    {
        nfc.removeStateChangedListener(nfcListener);
        nfcListener = null;
    }
    
    public void readNFCTag() throws TimeoutException, NotConnectedException
    {
        nfc.requestTagID(BrickletNFCRFID.TAG_TYPE_TYPE2);
    }
    
    private class NfcListener implements BrickletNFCRFID.StateChangedListener
    {
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
                        Pattern p = Pattern.compile("03 .{2} d1 01(.*)");
                        Matcher m = p.matcher(pageDataString);


                        if (m.find()) {
                            record = m.group(1);
                            record = record.substring(15);
                        }
                    } 
                    else{record = pageDataString;}                                          
                    
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
