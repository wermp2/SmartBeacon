/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SmartBeacon.sensors.NFC;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author stucn3, wermp2
 */
public class NFCRecord 
{
    private boolean isValid;
    private JSONObject nfcRecord;
    
    /**
     *
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
     * @return
     */
    public boolean hasData()
    {
        return (getData() != null);
    }
    
    /**
     *
     * @return
     */
    public boolean hasBeaconData(){
        return (getUuid()!= null);
    }

    /**
     *
     * @return
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
     * @return
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
     * @return
     */
    public String getAltitude() {
        JSONObject gpsJson;
        try {
            JSONObject nfcJson = nfcRecord.getJSONObject("NFC");
            gpsJson = nfcJson.getJSONObject("GPS");
        } catch (JSONException ex) {
            //ex.printStackTrace();
            return null;
        }
        return gpsJson.getString("alt");
    }

    /**
     *
     * @return
     */
    public String getData() {
        JSONObject dataJson;
        try {
            JSONObject nfcJson = nfcRecord.getJSONObject("NFC");
            dataJson = nfcJson.getJSONObject("data");
        } catch (JSONException ex) {
            //ex.printStackTrace();
            return null;
        }
        return dataJson.getString("data");
    }
    
    /**
     *
     * @return
     */
    public String getUuid() {
        JSONObject dataJson;
        try {
            JSONObject nfcJson = nfcRecord.getJSONObject("NFC");
            dataJson = nfcJson.getJSONObject("beacon");
        } catch (JSONException ex) {
            //ex.printStackTrace();
            return null;
        }
        return dataJson.getString("uuid");
    }
    
    /**
     *
     * @return
     */
    public String getMajor() {
        JSONObject dataJson;
        try {
            JSONObject nfcJson = nfcRecord.getJSONObject("NFC");
            dataJson = nfcJson.getJSONObject("beacon");
        } catch (JSONException ex) {
            //ex.printStackTrace();
            return null;
        }
        return dataJson.getString("major");
    }
    
    /**
     *
     * @return
     */
    public String getMinor() {
        JSONObject dataJson;
        try {
            JSONObject nfcJson = nfcRecord.getJSONObject("NFC");
            dataJson = nfcJson.getJSONObject("beacon");
        } catch (JSONException ex) {
            //ex.printStackTrace();
            return null;
        }
        return dataJson.getString("minor");
    }
}
