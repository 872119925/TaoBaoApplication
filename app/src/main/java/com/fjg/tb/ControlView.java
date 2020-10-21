package com.fjg.tb;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class ControlView extends FrameLayout implements View.OnClickListener {

    private Context mContext;

    private ControlCallback mCallback;

    public ControlView(@NonNull Context context, ControlCallback callback) {
        super(context);
        mContext = context;
        mCallback = callback;
        init();
    }

    private void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_control_layout, null);
        TextView tv_start = view.findViewById(R.id.tv_start_control);
        TextView tv_stop = view.findViewById(R.id.tv_stop_control);
        tv_start.setOnClickListener(this);
        tv_stop.setOnClickListener(this);
        addView(view);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_start_control:
                if (mCallback != null) {
                    mCallback.start();
                }
                break;
            case R.id.tv_stop_control:
                if (mCallback != null) {
                    mCallback.stop();
                }
                break;
        }
    }

    public interface ControlCallback {
        void start();

        void stop();
    }
}
