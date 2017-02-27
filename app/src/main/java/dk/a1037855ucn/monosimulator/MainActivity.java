package dk.a1037855ucn.monosimulator;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button mButtonOpen = null;
    private Button mButtonSend = null;
    private Button mButtonClose = null;
    private TextView resText = null;
    private TcpClient client = null;
    //private ListView tempList = null;
   // Context context = MainActivity.this;
    //ArrayList<String> tData = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonOpen = (Button) findViewById(R.id.button_open_connection);
        mButtonSend = (Button) findViewById(R.id.button_send_connection);
        mButtonClose = (Button) findViewById(R.id.button_close_connection);
        resText = (TextView) findViewById(R.id.textView);
       // tempList = (ListView) findViewById(R.id.listViewTemp);

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



    private class DataSendReceive extends AsyncTask<String, Void, ArrayList<String>> {
        private String responce;
        private ArrayList<String> tData = new ArrayList<String>();

 @Override
        protected ArrayList<String> doInBackground(String... params) {
            String req = params[0];
            try {

                for (int i = 0; i < 10; i++) {
                    client.sendData(req.getBytes());
                    responce = client.getData();
                    tData.add(responce);
                }

                Log.d("Received from mono : ", String.valueOf(tData.size()));

            } catch (Exception e) {
                e.printStackTrace();
            }
            return tData;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            //Log.d("Text to display : ", result);
            //resText.setText(result);
             ArrayAdapter<String> tAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, result);
            ListView tempList = (ListView)findViewById(R.id.listViewTemp);
            tempList.setAdapter(tAdapter);
        }
    }
}
