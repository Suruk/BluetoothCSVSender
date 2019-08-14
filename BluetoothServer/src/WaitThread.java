import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

//WaitThread is called when no devices are connected to Bluetooth server

public class WaitThread implements Runnable {

    private static final String appUUID2 = "686ced60a34911e9b4750800200c9a66";

    @Override
    public void run() {
        waiter();
    }

    private void waiter() {
        LocalDevice local = null;

        StreamConnectionNotifier notifier;
        StreamConnection connection = null;

        // setup the server to listen for connection
        try {
            local = LocalDevice.getLocalDevice();
            local.setDiscoverable(DiscoveryAgent.GIAC);

            UUID uuid = new UUID(appUUID2, false);
            String url = "btspp://localhost:" + uuid.toString() + ";name=RemoteBluetooth";
            notifier = (StreamConnectionNotifier) Connector.open(url);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        while (true) {
            try {
                System.out.println("waiting for connection...");
                connection = notifier.acceptAndOpen();

                Thread processThread = new Thread(new ProcessConnectionThread(connection));
                processThread.start();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

        }
    }
}
