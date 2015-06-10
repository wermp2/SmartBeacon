/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SmartBeacon.sensors.tilt;

import SmartBeacon.Beacon;
import ch.quantasy.tinkerforge.tinker.agent.implementation.TinkerforgeStackAgent;
import ch.quantasy.tinkerforge.tinker.application.implementation.AbstractTinkerforgeApplication;
import ch.quantasy.tinkerforge.tinker.core.implementation.TinkerforgeDevice;
import com.tinkerforge.BrickletTilt;
import com.tinkerforge.BrickletTilt.TiltStateListener;
import com.tinkerforge.Device;
import com.tinkerforge.NotConnectedException;
import com.tinkerforge.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The TiltSensor class detects if the beacon is replaced and instructs the other classes to search for an NFC tag or GPS data.
 * This class implements the TiltStateListener interface and is therefore a listener.
 * @author stucn3, wermp2
 */
public class TiltSensor extends AbstractTinkerforgeApplication implements TiltStateListener {

    private final Beacon beacon;
    private BrickletTilt tilt;
    private boolean isEnabled;

    /**
     * Constructor
     * @param beacon 
     */
    public TiltSensor(final Beacon beacon) {
        System.out.println("new tilt sensor");
        this.beacon = beacon;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public void deviceDisconnected(
            final TinkerforgeStackAgent tinkerforgeStackAgent,
            final Device device) {
        if ((TinkerforgeDevice.getDevice(device) == TinkerforgeDevice.Tilt)
                && device.equals(this.tilt)) {
            ((BrickletTilt) device).removeTiltStateListener(this);
            this.tilt = null;
            this.isEnabled = false;
        }
    }

    @Override
    public void deviceConnected(
            final TinkerforgeStackAgent tinkerforgeStackAgent,
            final Device device) {
        if (TinkerforgeDevice.getDevice(device) == TinkerforgeDevice.Tilt) {
            if (this.tilt != null) {
                return;
            }
            this.tilt = (BrickletTilt) device;
            this.tilt.addTiltStateListener(this);
            try {
                this.enableTiltSensor();
            } catch (TimeoutException ex) {
                Logger.getLogger(TiltSensor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NotConnectedException ex) {
                Logger.getLogger(TiltSensor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Method that is called when the tiltsensor detects a movement.
     * If the state is closed_vibrating, the tiltstatecallback gets diabled and the beacon class is instructed to search for an NFC tag.
     * @param state 
     */
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
                        isEnabled = false;
                        //removeTiltListener();
                        beacon.searchNFCTag();
                        break;
                    } catch (InterruptedException | TimeoutException | NotConnectedException ex) {
                        Logger.getLogger(TiltSensor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
        }
    }

    /**
     * Enables the tiltstatecallback
     * @throws TimeoutException
     * @throws NotConnectedException 
     */
    public void enableTiltSensor() throws TimeoutException, NotConnectedException {
        tilt.enableTiltStateCallback();
        isEnabled = true;
    }

    /**
     * 
     * @return true if the tiltsensor is enabled
     */
    public boolean isEnabled() {
        return isEnabled;
    }

}
