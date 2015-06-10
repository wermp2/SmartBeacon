/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SmartBeacon.sensors.NFC;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * NFCRecord stores all the important data found on an NFC tag
 * @author stucn3, wermp2
 */
public class NFCRecord 
{
    private boolean isValid;
    private JSONObject nfcRecord;
    
    /**
     *Constructor
     * @param nfcRecord
     * @param isValid
     */
    public NFCRecord(JSONObject nfcRecord, boolean isValid) {
        this.isValid = isValid;
        this.nfcRecord = nfcRecord;
    }
    
    /**
     *
     * @return a boolean if the NFC record is valid
     */
    public boolean isValidNFCRecord()
    {
        return isValid;
    }
    
    /**
     *
     * @return a boolean if there are GPS coordinates
     */
    public boolean hasGPS()
    {
        return (getLongitude() != null);
    }
    
    /**
     *
     * @return true if the NFC tag contains optional data
     */
    public boolean hasData()
    {
        return (getData() != null);
    }
    
    /**
     *
     * @return true if the NFC tag contains beacon data (UUID, Major, Minor)
     */
    public boolean hasBeaconData(){
        return (getUuid()!= null);
    }

    /**
     *
     * @return the longitude on this NFC tag in String representation
     */
    public String getLongitude() {
        JSONObject gpsJson;
        try {
            JSONObject nfcJson = nfcRecord.getJSONObject("NFC");
            gpsJson = nfcJson.getJSONObject("GPS");
        } catch (JSONException ex) {
            return null;
        }
        return gpsJson.getString("long");
    }

    /**
     *
     * @return the latitude on this NFC tag in String representation
     */
    public String getLatitude() {
        JSONObject gpsJson;
        try {
            JSONObject nfcJson = nfcRecord.getJSONObject("NFC");
            gpsJson = nfcJson.getJSONObject("GPS");
        } catch (JSONException ex) {
            return null;
        }
        return gpsJson.getString("lat");
    }

    /**
     *
     * @return the altitude on this NFC tag in String representation
     */
    public String getAltitude() {
        JSONObject gpsJson;
        try {
            JSONObject nfcJson = nfcRecord.getJSONObject("NFC");
            gpsJson = nfcJson.getJSONObject("GPS");
        } catch (JSONException ex) {
            return null;
        }
        return gpsJson.getString("alt");
    }

    /**
     *
     * @return the additional data on this NFC tag in String representation
     */
    public String getData() {
        JSONObject dataJson;
        try {
            JSONObject nfcJson = nfcRecord.getJSONObject("NFC");
            dataJson = nfcJson.getJSONObject("data");
        } catch (JSONException ex) {
            return null;
        }
        return dataJson.getString("data");
    }
    
    /**
     *
     * @return the uuid on this NFC tag in String representation
     */
    public String getUuid() {
        JSONObject dataJson;
        try {
            JSONObject nfcJson = nfcRecord.getJSONObject("NFC");
            dataJson = nfcJson.getJSONObject("beacon");
        } catch (JSONException ex) {
            return null;
        }
        return dataJson.getString("uuid");
    }
    
    /**
     *
     * @return the major value on this NFC tag in String representation
     */
    public String getMajor() {
        JSONObject dataJson;
        try {
            JSONObject nfcJson = nfcRecord.getJSONObject("NFC");
            dataJson = nfcJson.getJSONObject("beacon");
        } catch (JSONException ex) {
            return null;
        }
        return dataJson.getString("major");
    }
    
    /**
     *
     * @return the minor value on this NFC tag in String representation
     */
    public String getMinor() {
        JSONObject dataJson;
        try {
            JSONObject nfcJson = nfcRecord.getJSONObject("NFC");
            dataJson = nfcJson.getJSONObject("beacon");
        } catch (JSONException ex) {
            return null;
        }
        return dataJson.getString("minor");
    }
}
