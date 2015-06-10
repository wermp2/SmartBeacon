package SmartBeacon.Advertising;

import SmartBeacon.sensors.tilt.TiltSensor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to enable/disable Bluetooth and set advertising with Linux commands.
 *
 * @author stucn3, wermp2
 */
public class BLEAdvertiser extends Thread {

    private ArrayList<String> advertisingDataCommands;
    private String beaconDataCommand;
    private TiltSensor tiltSensor;
    private volatile boolean running = false;

    /**
     * Constructor
     *
     * @param advertisingDataCommands The commands used for advertising data
     * @param beaconDataCommand The command used for setting the UUID, major and
     * minor values
     * @param tiltSensor The tilt sensor
     */
    public BLEAdvertiser(ArrayList<String> advertisingDataCommands, String beaconDataCommand, TiltSensor tiltSensor) {
        this.advertisingDataCommands = advertisingDataCommands;
        this.beaconDataCommand = beaconDataCommand;
        this.tiltSensor = tiltSensor;
    }

    /**
     * Starts the Bluetooth Low Energy dongle
     */
    private void startBLEDongle() {
        String command = "sudo hciconfig hci0 up";
        runCommand(command);
    }

    /**
     * Stops the Bluetooth Low Energy dongle
     */
    private void stopBLEDongle() {
        String command = "sudo hciconfig hci0 down";
        runCommand(command);
    }

    /**
     * Stops Low Energy advertising
     */
    private void stopLEAdvertising() {
        String command = "sudo hciconfig hci0 noleadv";
        runCommand(command);

    }

    /**
     * Starts Low Energy advertising with parameter 3, which means that no
     * connections are allowed.
     */
    private void startLEAdvertising() {
        String command = "sudo hciconfig hci0 leadv 0";
        runCommand(command);
    }

    /**
     * Starts page and inquiry scan
     */
    private void setPIScan() {
        String command = "sudo hciconfig hci0 piscan";
        runCommand(command);
    }

    /**
     * Runs a prepared command in Linux
     *
     * @param command
     */
    public void runCommand(String command) {
        if (command != null && !command.equals("")) {
            //System.out.println(command);
            Runtime runtime = Runtime.getRuntime();
            try {
                Process process = runtime.exec(command);
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Stops and restarts Low Energy advertising
     */
    public void resetAdvertising() {
        stopLEAdvertising();
        startLEAdvertising();
    }

    /**
     * Starts advertising
     */
    public void startAdvertising() {
        startBLEDongle();
        setPIScan();
        stopLEAdvertising();
        startLEAdvertising();
    }

    /**
     * Stops advertising
     */
    public void stopAdvertising() {
        stopBLEDongle();
        advertisingDataCommands.clear();
        beaconDataCommand = "";
        System.out.println("--------STOP ADVERTISING---------");
    }

    /**
     * Runs the actual advertising of GPS coordinates, additional and beacon
     * data
     */
    @Override
    public void run() {
        //start advertising
        startAdvertising();
        while (running) {
            //go trough array of advertising data commands
            for (int i = 0; i <= this.advertisingDataCommands.size(); i++) {
                if (!running) {
                    break;
                }
                //stop advertising when beacon is replaced
                if (!tiltSensor.isEnabled()) {
                    stopAdvertising();
                    running = false;
                    break;
                }

                //restart for-loop when last part is advertised
                if (i == (this.advertisingDataCommands.size())) {
                    i = 0;
                }

                //reset advertising and run commands for setting beacon data and advertising data
                resetAdvertising();
                runCommand(this.beaconDataCommand);
                runCommand(this.advertisingDataCommands.get(i));

                //wait 3 seconds with advertising of the next part
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

            }
        }

    }

    public void terminate() {
        this.running = false;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
