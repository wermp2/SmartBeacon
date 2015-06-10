package SmartBeacon.sensors.GPS;

import com.tinkerforge.BrickletGPS;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import java.util.ArrayList;

/**
 * Class which manages the GPS position of the GPS receiver. 
 * @author stucn3, wermp2
 */

public class GPSReceiver 
{
    private final BrickletGPS gps;

    /**
     * Constructor
     * @param UID_gps   the ID of the GPS Bricklet
     * @param ipcon     the IPConncection
     */
    public GPSReceiver(String UID_gps, IPConnection ipcon){
        gps = new BrickletGPS(UID_gps, ipcon);
    }

    /**
     * Checks if the beacon is indoor or outdoor based on the GPS error
     * @return a boolean if the beacon is outdoor or not
     * @throws TimeoutException
     * @throws NotConnectedException
     * @throws InterruptedException
     */
    public boolean isOutdoor() throws TimeoutException, NotConnectedException, InterruptedException 
    {
        ArrayList<Integer> errorLog = new ArrayList();
        boolean isOutdoor = false;
        int scanPeriod = 6;
        
        //Collects some GPS position error values over a span of two seconds
        //This is due some inconciencies of the Tinkerfogre GPS bricklet
        for(int i = 0; i < scanPeriod; i++){   
            if(gps.getCoordinates().epe != 0 && gps.getStatus().fix >= 2){
                errorLog.add(gps.getCoordinates().epe);
            }
            Thread.sleep(350);
        }
        
        //Calculates the average position error
        int sum = 0;
        for (int d : errorLog) sum += d;
        double average = 1.0d * sum / errorLog.size();
        
        System.out.println("Average gps error: " + average);
        
        //If the average position error is below 12m and the gps bricklet is connected to at least 3 staelites, the beacon is located indoor
        if(average < 1200 && average != Double.NaN && gps.getStatus().fix >= 2)
        {
            isOutdoor = true;
        }
        //beacon is located outdoor
        else if(average == Double.NaN || gps.getStatus().fix < 2)
        {
            isOutdoor = false;
        }
        else if(average >= 1200)
        {
            isOutdoor = false;
        }            
        return isOutdoor;
    }
    
    /**
     * Returns the longitude coordinate
     * @return the longitude
     * @throws TimeoutException
     * @throws NotConnectedException
     */
    public String getLongitude() throws TimeoutException, NotConnectedException
    {
        long longitude = gps.getCoordinates().longitude;
        String longitudeWithDecimal= String.valueOf(longitude);
        int length = longitudeWithDecimal.length();
        int decimalPlace = length - 6;
        //add decimal point 
        longitudeWithDecimal = new StringBuilder(longitudeWithDecimal).insert(decimalPlace, ".").toString();
        return longitudeWithDecimal;
    }
    
    /**
     * Returns the latitude coordinate
     * @return the latitude
     * @throws TimeoutException
     * @throws NotConnectedException
     */
    public String getLatitude() throws TimeoutException, NotConnectedException
    {
        long latitude = gps.getCoordinates().latitude;
        String latitudeWithDecimal = String.valueOf(latitude);
        int length = latitudeWithDecimal.length();
        int decimalPlace = length - 6;
        //add decimal point 
        latitudeWithDecimal = new StringBuilder(latitudeWithDecimal).insert(decimalPlace, ".").toString();
        return latitudeWithDecimal;
    }
    
    /**
     * Returns the altitude
     * @return the altitude
     * @throws TimeoutException
     * @throws NotConnectedException
     */
    public String getAltitude() throws TimeoutException, NotConnectedException
    {
        long altidute = gps.getAltitude().altitude;
        String altiduteWithDecimal = String.valueOf(altidute);
        int length = altiduteWithDecimal.length();
        int decimalPlace = length - 2;
        //add decimal point 
        altiduteWithDecimal = new StringBuilder(altiduteWithDecimal).insert(decimalPlace, ".").toString();
        return altiduteWithDecimal;
    }
}
