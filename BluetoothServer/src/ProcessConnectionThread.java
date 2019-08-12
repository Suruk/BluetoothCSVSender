import org.apache.commons.io.FilenameUtils;

import javax.microedition.io.StreamConnection;
import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ProcessConnectionThread implements Runnable{

    private StreamConnection mConnection;

    private static final String STATE_CONNECTED = "12";

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
                int n = inputStream.read(buffer);

                String result;
                if (Arrays.equals(buffer, new byte[1024])){
                    inputStream.close();
                    mConnection.close();
                    return;
                } else {
                    result = new String(buffer, 0, n, StandardCharsets.UTF_8);
                }
                if (STATE_CONNECTED.equals(result)){
                    System.out.println("Connection completed successfully");
                } else {
                    JFrame parentFrame = new JFrame();

                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Specify a file to save");

                    int userSelection = fileChooser.showSaveDialog(parentFrame);

                    if (userSelection == JFileChooser.APPROVE_OPTION) {
                        File fileToSave = fileChooser.getSelectedFile();
                        if (!FilenameUtils.getExtension(fileToSave.getName()).equalsIgnoreCase("txt")) {
                            fileToSave = new File(fileToSave.toString() + ".txt");
                            fileToSave = new File(fileToSave.getParentFile(), FilenameUtils.getBaseName(fileToSave.getName()) + ".txt");
                        }
                        System.out.println("Saved successfully to: " + fileToSave.getAbsolutePath());
                        FileWriter writer = new FileWriter(fileToSave);
                        writer.append(result);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
