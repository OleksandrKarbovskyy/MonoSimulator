package dk.a1037855ucn.monosimulator;

import java.util.ArrayList;

/**
 * Created by martin on 03-03-2017.
 * Singleton container to store results from the mono device.
 */

public class ResultContainer {
    private ArrayList<Float> resultData = new ArrayList<>();
    private static ResultContainer instance = null;

    private ResultContainer() {
    }

    public static ResultContainer getInstance(){
        if (instance == null) {
            instance = new ResultContainer();
        }
        return instance;
    }

    public ArrayList<Float> getResultData() {
        return resultData;
    }

    public void setResultData(ArrayList<Float> resultData) {
        this.resultData = resultData;
    }
}
