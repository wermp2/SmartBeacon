package com.example.android.bluetoothlegatt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;


public class SmartBeacon
{
    private ArrayList<JSONObject> jsonParts = new ArrayList<>();
    private BluetoothDevice device;
    private boolean scanComplete = false;
    private boolean scanInterrupted = false;
    private int partScanCounter = 0;
    private long lastTimeStamp = 0L;

    /**
     * This class represents a Smart Beacon
     * @param device
     */
    public SmartBeacon(BluetoothDevice device)
    {
        this.device = device;
    }

    /**
     * This method is called if the bluetooth scan found a advertisement of a smart beacon.
     * It checks if the advertisement part is already in our list of advertisements. If not, it adds the new advertisement other wiese is checks if all the parts are found.
     * @param obj the advertisement part as json object
     * @param currentTimeStamp the timestamp of the advertisement part
     * @throws JSONException
     */
    public void addPart(JSONObject obj, long currentTimeStamp) throws JSONException {

        lastTimeStamp = currentTimeStamp;

        if(hasPart(obj))
        {
            Log.d("PART already in list: ", obj.toString());

            if(isNewAdvertisementPart(obj))
            {
                scanInterrupted = true;
            }
            else if(allPartsFound())
            {
                scanComplete = true;
            }
        }
        else
        {
            Log.d("Add new Part: ", obj.toString());
            jsonParts.add(obj);
            partScanCounter = 0;
        }
    }

    /**
     * Returns the index number of the given part
     * @param obj
     * @return
     * @throws JSONException
     */
    private int getPartIndexNumber(JSONObject obj) throws JSONException {
        JSONArray keys = obj.names();
        return keys.getInt(0);
    }

    /**
     * Checks if a part is already in the list
     * @param obj
     * @return
     * @throws JSONException
     */
    public boolean hasPart(JSONObject obj) throws JSONException {

        for(JSONObject part : jsonParts)
        {
           if(getPartIndexNumber(part) == getPartIndexNumber(obj))
           {
               return true;
           }
        }
        return false;
    }

    /**
     * Sorts the different advertisement parts
     * @return
     * @throws JSONException
     */
    private ArrayList<JSONObject> sortAdvertisementParts() throws JSONException {

        ArrayList<String> sortedStringParts = new ArrayList<>();

        for(JSONObject obj : jsonParts)
        {
            sortedStringParts.add(obj.toString());
        }

        Collections.sort(sortedStringParts);

        ArrayList<JSONObject> sortedJsonParts = new ArrayList<>();

        for(String obj : sortedStringParts)
        {
            JSONObject json = new JSONObject(obj);
            sortedJsonParts.add(json);
        }

        return sortedJsonParts;
    }

    /**
     * Checks if an advertisement is already in the list
     * @param obj
     * @return
     * @throws JSONException
     */
    private boolean isNewAdvertisementPart(JSONObject obj) throws JSONException {

        for(JSONObject part : jsonParts)
        {
            if(getPartIndexNumber(part) == getPartIndexNumber(obj))
            {
               return !(part.toString().equals(obj.toString()));
            }
        }

        return false;
    }

    /**
     * Prints all the scanned parts in the order they were scanned.
     * The printing format is in JSON notation.
     * @return
     * @throws JSONException
     */
    public String getCurrentAdvertisementParts() throws JSONException {

        String advertisement = "";
        ArrayList<JSONObject> sortedJsonParts = sortAdvertisementParts();

        for(JSONObject advPart : sortedJsonParts)
        {
            advertisement = advertisement + advPart.toString() + "\n";
        }

        return advertisement;
    }

    /**
     * Returns the complete, well formatted and ordered advertisement as a string.
     * @return
     * @throws JSONException
     */
    public String getCompleteAdvertisement() throws JSONException {

        ArrayList<JSONObject> sortedJsonParts = sortAdvertisementParts();
        sortedJsonParts.remove(sortedJsonParts.size()-1);

        StringBuilder advString = new StringBuilder();

        for(JSONObject jsonPart : sortedJsonParts)
        {
            String part = "";

            int keyOne = getPartIndexNumber(jsonPart);

            JSONObject innerObject = jsonPart.getJSONObject(String.valueOf(keyOne));
            JSONArray keyTwo = innerObject.names();


            if (keyTwo.get(0).equals("data"))
            {
                part = (String) innerObject.get(keyTwo.getString(0));
            }
            else if(keyTwo.get(0).equals("alt"))
            {
                //altitude
                part = "Altitude: " + (String) innerObject.get(keyTwo.getString(0)) + "\n";
            }
            else if(keyTwo.get(0).equals("long"))
            {
                //Longitude, altitude, latitude auf neue Zeile
                double value = Double.parseDouble((String) innerObject.get(keyTwo.getString(0)));
                String coord = this.decimalToDMS(value);
                String intro = "Ihre Position: " + "\n";
                part = intro + "Longitude: " + coord + "\n";
            }
            else
            {
                double value = Double.parseDouble((String) innerObject.get(keyTwo.getString(0)));
                String coord = this.decimalToDMS(value);
                part = "Latitude: " + coord + "\n";
            }

            advString.append(part);
        }

        return advString.toString();
    }

    /**
     * Used to check if a part is missing. Should only be called before a new part is added to the list.
     * @param obj The new part to be added
     * @return
     * @throws JSONException
     */
    public boolean checkCompletion(JSONObject obj) throws JSONException {

        int partIndex = getPartIndexNumber(obj);
        int indexLastPartReceived = getPartIndexNumber(jsonParts.get(jsonParts.size() - 1));
        int difference = partIndex - 1;

        if(difference == indexLastPartReceived)
        {
            return true;
        }
        else if (partIndex < indexLastPartReceived)
        {
            JSONArray key = obj.names();
            String partData = (String) obj.get(key.get(0).toString());

            if(partData == "of advertisement" && partIndex == 1)
            {
                return true;
            }

            return false;
        }
        else
        {
            return false;
        }
    }

    /**
     * Used to check if a part is missing.
     * @return
     * @throws JSONException
     */
    public boolean allPartsFound() throws JSONException {

        int partCount = jsonParts.size();
        //JSONObject lastPart = null;

        for (JSONObject part : jsonParts)
        {
            JSONArray key = part.names();
            String partData = part.getString(key.getString(0));

            if(partData.equals("end of advertisement") && getPartIndexNumber(part) == partCount)
            {
                System.out.println("All parts found");
                return true;
            }
        }

        return false;
    }

    public int getRecordsSize()
    {
        return jsonParts.size();
    }

    public BluetoothDevice getDevice()
    {
        return device;
    }

    public long getLastTimeStamp()
    {
        return lastTimeStamp;
    }

    public boolean isScanComplete()
    {
        return scanComplete;
    }

    public boolean isScanInterrupted()
    {
        return scanInterrupted;
    }

    public void clearAdvertisements()
    {
        jsonParts.clear();
    }

    // Input a double latitude or longitude in the decimal format
    // e.g. 87.728056
    // http://stackoverflow.com/questions/15547329/how-to-prettily-format-gps-data-in-java-android
    String decimalToDMS(double coord) {
        String output, degrees, minutes, seconds;

        // gets the modulus the coordinate divided by one (MOD1).
        // in other words gets all the numbers after the decimal point.
        // e.g. mod = 87.728056 % 1 == 0.728056
        //
        // next get the integer part of the coord. On other words the whole number part.
        // e.g. intPart = 87

        double mod = coord % 1;
        int intPart = (int)coord;

        //set degrees to the value of intPart
        //e.g. degrees = "87"

        degrees = String.valueOf(intPart);

        // next times the MOD1 of degrees by 60 so we can find the integer part for minutes.
        // get the MOD1 of the new coord to find the numbers after the decimal point.
        // e.g. coord = 0.728056 * 60 == 43.68336
        //      mod = 43.68336 % 1 == 0.68336
        //
        // next get the value of the integer part of the coord.
        // e.g. intPart = 43

        coord = mod * 60;
        mod = coord % 1;
        intPart = (int)coord;

        // set minutes to the value of intPart.
        // e.g. minutes = "43"
        minutes = String.valueOf(intPart);

        //do the same again for minutes
        //e.g. coord = 0.68336 * 60 == 41.0016
        //e.g. intPart = 41
        coord = mod * 60;
        intPart = (int)coord;

        // set seconds to the value of intPart.
        // e.g. seconds = "41"
        seconds = String.valueOf(intPart);

        // I used this format for android but you can change it
        // to return in whatever format you like
        // e.g. output = "87/1,43/1,41/1"
        //output = degrees + "/1," + minutes + "/1," + seconds + "/1";

        //Standard output of D°M′S″
        output = degrees + "°" + minutes + "'" + seconds + "\"";

        return output;
    }

    /*
     * Conversion DMS to decimal
     *
     * Input: latitude or longitude in the DMS format ( example: N 43° 36' 15.894")
     * Return: latitude or longitude in decimal format
     * hemisphereOUmeridien => {W,E,S,N}
     *
     */
    public double DMSToDecimal(String hemisphereOUmeridien,double degres,double minutes,double secondes)
    {
        double LatOrLon=0;
        double signe=1.0;

        if((hemisphereOUmeridien=="W")||(hemisphereOUmeridien=="S")) {signe=-1.0;}
        LatOrLon = signe*(Math.floor(degres) + Math.floor(minutes)/60.0 + secondes/3600.0);

        return(LatOrLon);
    }

    public String getUserFriendlyAdvertisement() throws JSONException {

        if(isScanComplete())
        {
           return getCompleteAdvertisement();
        }

        return "Scanning..." + "\n" + "Parts found: " + jsonParts.size();
    }
}

