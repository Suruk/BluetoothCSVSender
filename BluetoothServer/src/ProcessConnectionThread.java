import javax.microedition.io.StreamConnection;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ProcessConnectionThread implements Runnable{

    private StreamConnection mConnection;


    ProcessConnectionThread(StreamConnection connection)
    {
        mConnection = connection;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = mConnection.openInputStream();

            System.out.println("waiting for input");

            while (true) {
                byte [] buffer = new byte [1024];
                int command = inputStream.read(buffer);
                System.out.println(command);
                String result = new String(buffer, StandardCharsets.UTF_8);
                if (Arrays.equals(buffer, new byte[1024])){
                    inputStream.close();
                    mConnection.close();
                    return;
                }
                System.out.println(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
