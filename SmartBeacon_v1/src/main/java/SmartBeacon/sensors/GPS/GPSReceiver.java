/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SmartBeacon.sensors.GPS;

import com.tinkerforge.BrickletGPS;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import java.util.ArrayList;


public class GPSReceiver 
{
    private final BrickletGPS gps;

    public GPSReceiver(String UID_gps, IPConnection ipcon) 
    {
        gps = new BrickletGPS(UID_gps, ipcon);
    }

    /**
     * Checks if the beacon is indoor or outdoor based on the GPS error
     * @return true if the beacon is located outdoor
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
        for(int i = 0; i < scanPeriod; i++)
        {   
            if(gps.getCoordinates().epe != 0 && gps.getStatus().fix >= 2)
            {
                errorLog.add(gps.getCoordinates().epe);
            }
            
            Thread.sleep(350);
        }
        
        int sum = 0;
        for (int d : errorLog) sum += d;
        double average = 1.0d * sum / errorLog.size();
        
        System.out.println("Average gps error: " + average);
        
         //outdoor aber ungenau... (12m fehler möglich...! [unter dach oder an hauswand kann dies aber normal sein... [mehr testen])
        if(average < 1200 && average != Double.NaN && gps.getStatus().fix >= 2)
        {
            isOutdoor = true;
        }
        //Keine GPS Angabe möglich
        else if(average == Double.NaN || gps.getStatus().fix < 2)
        {
            isOutdoor = false;
        }
        //Angabe möglich aber höchst wahrscheinlich indoor oder sehr ungenaues outdoor d.h. gps position unbrauchbar
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
    public long getLongitude() throws TimeoutException, NotConnectedException
    {
        return gps.getCoordinates().longitude;
    }
    
    /**
     * Returns the latitude coordinate
     * @return the latitude
     * @throws TimeoutException
     * @throws NotConnectedException
     */
    public long getLatitude() throws TimeoutException, NotConnectedException
    {
        return gps.getCoordinates().latitude;
    }
    
    /**
     * Returns the altitude
     * @return the altitude
     * @throws TimeoutException
     * @throws NotConnectedException
     */
    public long getAltitude() throws TimeoutException, NotConnectedException
    {
        return gps.getAltitude().altitude;
    }
}
