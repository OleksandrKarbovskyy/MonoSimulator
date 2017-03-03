package dk.a1037855ucn.monosimulator;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by oleks on 26-02-2017.
 */

public class TcpClient {

    private String mServerName = "192.168.137.1";// ip address of mono board
    //private String mServerName = "192.168.56.1";// ip address of mono board

    private int mServerPort = 7913;//port on the mono board

    private Socket mSocket = null;

    public TcpClient() {
    }

    public void openConnection() throws Exception {


        closeConnection();

        try {

            mSocket = new Socket(mServerName, mServerPort);

        } catch (IOException e) {
            throw new Exception("Unable to create socket: " + e.getMessage());
        }
    }

    public void closeConnection() {


        if (mSocket != null && !mSocket.isClosed()) {

            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e("Message : ", "Unable to close socket: " + e.getMessage());
            } finally {
                mSocket = null;
            }

        }
        // mSocket = null;
    }

    public void sendData(byte[] data) throws Exception {


        if (mSocket == null || mSocket.isClosed()) {
            throw new Exception("Unable to send data. The socket is not created or closed");
        }


        try {
            OutputStream os = mSocket.getOutputStream();
            if (os != null) {

                os.write(data);
                Log.d("OutputStream : ", os.toString());
                os.flush();
            }

        } catch (IOException e) {
            throw new Exception("Unable to send data: " + e.getMessage());
        }
    }

    public String getData() throws Exception {

        InputStream inputStream = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String data = null;
        try {
            inputStream = mSocket.getInputStream();

            if (inputStream != null) {
                String line = "";
                try {
                    br = new BufferedReader(new InputStreamReader(inputStream));
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                data = sb.toString();
            } else {
                data = "";
            }

        } catch (IOException e) {

            throw new Exception("Unable to read data: " + e.getMessage());
        }
        Log.d("Data received : ", data);
        closeConnection();
        return data;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        closeConnection();
    }
}
