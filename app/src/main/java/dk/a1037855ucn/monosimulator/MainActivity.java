package dk.a1037855ucn.monosimulator;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button mButtonOpen = null;
    private Button mButtonSend = null;
    private Button mButtonClose = null;
    private TextView resText = null;
    private TcpClient client = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonOpen = (Button) findViewById(R.id.button_open_connection);
        mButtonSend = (Button) findViewById(R.id.button_send_connection);
        mButtonClose = (Button) findViewById(R.id.button_close_connection);
        resText = (TextView) findViewById(R.id.textView);

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
                DataSendReceive dsr = new DataSendReceive();
                dsr.execute("*t<cr><lf>");
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

    private class DataSendReceive extends AsyncTask<String, String, String> {
        private String responce;


        @Override
        protected String doInBackground(String... params) {
            String req = params[0];
            try {
                client.sendData(req.getBytes());
                responce = client.getData();
                Log.d("Received from mono : ", responce);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return responce;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("Text to display : ", result);
            resText.setText(result);

        }
    }
}
