package dk.a1037855ucn.monosimulator;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.XYChart;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    private Button mButtonOpen = null;
    private Button mButtonSend = null;
    private Button mButtonClose = null;
    private Button mButtonGetData;
    private Button mButtonMakeGraph;
    private TextView resMin = null;
    private TextView resAvr = null;
    private TextView resMax = null;
    private int plotCounter = 0;
    private LinearLayout graphLayout;
    private XYChart myChart;
    private GraphicalView myChart2;

    private ResultContainer con = ResultContainer.getInstance();
    private TcpClient client = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonOpen = (Button) findViewById(R.id.button_open_connection);
        mButtonSend = (Button) findViewById(R.id.button_send_connection);
        mButtonClose = (Button) findViewById(R.id.button_close_connection);
        mButtonGetData = (Button) findViewById(R.id.getData);
        mButtonMakeGraph = (Button) findViewById(R.id.btn_MakeGraph);
        resMin = (TextView) findViewById(R.id.textView_min);
        resAvr = (TextView) findViewById(R.id.textView_avr);
        resMax = (TextView) findViewById(R.id.textView_max);
        graphLayout = (LinearLayout) findViewById(R.id.chart);

        mButtonSend.setEnabled(false);
        mButtonClose.setEnabled(false);


        mButtonOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createThreadOpenConnection();
                //if (client.isConnected()){
                mButtonSend.setEnabled(true);
                mButtonClose.setEnabled(true);
                mButtonOpen.setEnabled(false);
                //}
            }
        });

        mButtonSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mButtonOpen.setEnabled(false);
                mButtonSend.setEnabled(false);
                callDataSendRecive();
            }
        });

        mButtonClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                client.closeConnection();
                mButtonOpen.setEnabled(true);
                mButtonSend.setEnabled(false);
                mButtonClose.setEnabled(false);
            }
        });

        mButtonGetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createThreadOpenConnection();
                sleepNow();
                callDataSendRecive();
                sleepNow();
                client.closeConnection();

            }
        });

        mButtonMakeGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initilizeGraph();
                graphLayout.addView(myChart2);
            }
        });
    }

    private void initilizeGraph(){
        ResultContainer con = ResultContainer.getInstance();
        ArrayList<Float> tData = con.getResultData();
        TimeSeries series = new TimeSeries("Line1");
        for (int i = 0; i < tData.size(); i++ ){
            double y = new Double(tData.get(i).toString());
            series.add(plotCounter+1,y);
            plotCounter++;
        }
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(series);

        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        mRenderer.addSeriesRenderer(renderer);

        // Disable panning
        mRenderer.setPanEnabled(false);

        // Set Y-Axis range
        mRenderer.setYAxisMax(30);
        mRenderer.setYAxisMin(0);

        mRenderer.setXAxisMax(20);
        mRenderer.setXAxisMin(0);
        myChart2 = ChartFactory.getLineChartView(this, dataset, mRenderer);
    }

    private void sleepNow(){
        try {
            sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void callDataSendRecive(){
        String arg = "*t<cr><lf>";
        DataSendReceive dsr = new DataSendReceive();
        dsr.execute(arg);
    }

    private void createThreadOpenConnection(){
        client = new TcpClient();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.openConnection();

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                        }
                    });
                } catch (Exception e) {
                    Log.e("Message: ", e.getMessage());
                    client = null;
                }
            }
        }).start();
    }

    private class DataSendReceive extends AsyncTask<String, Void, String> {
        private String responce;
        private ResultContainer con = ResultContainer.getInstance();
        private ArrayList<Float> tData;

        public DataSendReceive() {
            tData = con.getResultData();
        }

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
            if (result != null) {
                String t = result.replace("t", "");
                tData.add(Float.valueOf(t));
                Log.d("Text to display : ", t);

                resMin.setText("Min temperature : " + Collections.min(tData).toString());
                resAvr.setText("Average temperature : " + getAvr());
                resMax.setText("Max temperature : " + Collections.max(tData).toString());
            }
        }

        private String getAvr() {
            float sum = 0;
            for (Float f : tData) {
                sum += f;
            }
            return String.valueOf(sum / tData.size());
        }
    }
}
