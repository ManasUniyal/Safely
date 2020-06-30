package com.example.miniproject;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;

public class Common {

    private final static int JOURNEY_NOT_STARTED = 0;
    private final static int JOURNEY_STARTED = 1;
    private static Common instance = null;
    private static int journeyState = 0;

    public static synchronized Common getInstance() {
        if(instance == null) {
            instance = new Common();
        }
        return instance;
    }

    private Common() {
        journeyState = 0;
    }

    public int getJourneyState() {
        return journeyState;
    }

    public void toggleJourneyState() {
        journeyState ^= 1;
    }

    public void setJourneyStateButtonView(View view) {
        Button btn = (Button) view;
        if(getJourneyState() == JOURNEY_NOT_STARTED) {
            btn.setText(R.string.start_journey);
            btn.setBackgroundColor(Color.GREEN);
        } else {
            btn.setText(R.string.end_journey);
            btn.setBackgroundColor(Color.RED);
        }
    }
}
