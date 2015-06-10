/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SmartBeacon.sensors.tilt;

import SmartBeacon.Beacon;
import com.tinkerforge.BrickletTilt;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TiltSensor {
    
    private final Beacon beacon;
    private final BrickletTilt tilt;
    private TiltListener tiltListener = null;
    
    private final IPConnection ipcon;
    private final String UID_tilt;
    
    public TiltSensor(Beacon beacon, String UID_tilt, IPConnection ipcon)
    {
        this.beacon = beacon;
        this.ipcon = ipcon;
        this.UID_tilt = UID_tilt;
        tilt = new BrickletTilt(UID_tilt, ipcon);  
    }
 
    public void addNewTiltListener() throws TimeoutException, NotConnectedException
    {
        tiltListener = new TiltListener();
        tilt.addTiltStateListener(tiltListener);
        tilt.enableTiltStateCallback();
    }
    
    public void removeTiltListener()
    {
        tilt.removeTiltStateListener(tiltListener);
        tiltListener = null;
    }
    
    public void enableTiltSensor() throws TimeoutException, NotConnectedException
    {
        tilt.enableTiltStateCallback();
    }
    
    private class TiltListener implements BrickletTilt.TiltStateListener {

        @Override
        public void tiltState(short state) {
            switch (state) {
                case BrickletTilt.TILT_STATE_CLOSED:
                    System.out.println("closed");
                    break;

                case BrickletTilt.TILT_STATE_OPEN:
                    System.out.println("open");
                    break;

                case BrickletTilt.TILT_STATE_CLOSED_VIBRATING:
                    System.out.println("closed vibrating"); 
            {
                try {
                    tilt.disableTiltStateCallback();
                    //removeTiltListener();
                    beacon.searchNFCTag();
                } catch (InterruptedException | TimeoutException | NotConnectedException ex) {
                    Logger.getLogger(TiltSensor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
                    break;
            }
        }

    }

}
