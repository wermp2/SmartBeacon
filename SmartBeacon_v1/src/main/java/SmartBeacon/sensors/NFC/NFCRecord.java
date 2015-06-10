/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SmartBeacon.sensors.NFC;

import org.json.JSONException;
import org.json.JSONObject;


public class NFCRecord 
{
    private boolean isValid;
    private JSONObject nfcRecord;
    
    public NFCRecord(JSONObject nfcRecord, boolean isValid) {
        this.isValid = isValid;
        this.nfcRecord = nfcRecord;
    }
    
    public boolean isValidNFCRecord()
    {
        return isValid;
    }
    
    public boolean hasGPS()
    {
        return (getLongitude() != null);
    }
    
    public boolean hasData()
    {
        return (getData() != null);
    }

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
}
