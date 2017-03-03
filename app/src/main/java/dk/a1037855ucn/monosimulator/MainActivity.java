package dk.a1037855ucn.monosimulator;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private Button mButtonOpen = null;
    private Button mButtonSend = null;
    private Button mButtonClose = null;
    private TextView resMin = null;
    private TextView resAvr = null;
    private TextView resMax = null;

    private TcpClient client = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonOpen = (Button) findViewById(R.id.button_open_connection);
        mButtonSend = (Button) findViewById(R.id.button_send_connection);
        mButtonClose = (Button) findViewById(R.id.button_close_connection);
        resMin = (TextView) findViewById(R.id.textView_min);
        resAvr = (TextView) findViewById(R.id.textView_avr);
        resMax = (TextView) findViewById(R.id.textView_max);


        mButtonSend.setEnabled(false);
        mButtonClose.setEnabled(false);


        mButtonOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                client = new TcpClient();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            client.openConnection();

                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    mButtonSend.setEnabled(true);
                                    mButtonClose.setEnabled(true);
                                }
                            });
                        } catch (Exception e) {
                            Log.e("Message: ", e.getMessage());
                            client = null;
                        }
                    }
                }).start();
            }
        });

        mButtonSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String arg = "*t<cr><lf>";
                DataSendReceive dsr = new DataSendReceive();
                dsr.execute(arg);
            }
        });

        mButtonClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                client.closeConnection();

                mButtonSend.setEnabled(false);
                mButtonClose.setEnabled(false);
            }
        });

    }


    private class DataSendReceive extends AsyncTask<String, Void, String> {
        private String responce;
        private ArrayList<Float> tData = new ArrayList<>();



        @Override
        protected String doInBackground(String... params) {
            String par = params[0];
            try {
                client.sendData(par.getBytes());
                responce = client.getData();
                Log.d("Received from mono : ", responce);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return responce;
        }

        @Override
        protected void onPostExecute(String result) {
                        //fil up the array of temperature without t for building graph
            String t = result.replace("t", "");
            Log.d("Text to display : ", t);

            for (int i = 0; i < 10; i++) {
               tData.add(Float.valueOf(result.replace("t", "")) + i);
            }

             resMin.setText("Min temperature : " + Collections.min(tData).toString());
             resAvr.setText("Average temperature : " + getAvr());
             resMax.setText("Max temperature : " + Collections.max(tData).toString());
        }

        private String getAvr() {

            float sum = 0;
            float avr = 0;
            for (Float f : tData) {
                sum += f;
            }
            return String.valueOf(sum / tData.size());
        }


    }
}
