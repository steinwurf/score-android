package com.steinwurf.score;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.util.Locale;

public class SeekBarHelper {

    public interface onProgressChangedListener {
        void onProgressChanged(double value);
    }

    private final OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (onProgressChangedListener != null)
            {
                double percentage = i / (double)seekBar.getMax();
                double value = (percentage * (max - min) + min);

                onProgressChangedListener.onProgressChanged(value);
                if (SeekBarHelper.this.floatingPoint)
                {
                    SeekBarHelper.this.textView.setText(String.format(Locale.getDefault(), "%.2f", value));
                }
                else
                {
                    SeekBarHelper.this.textView.setText(String.format(Locale.getDefault(), "%d", (int)value));
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private final SeekBar seekBar;
    private final TextView textView;
    private final boolean floatingPoint;

    private onProgressChangedListener onProgressChangedListener;
    private int max = 100;
    private int min = 0;

    public SeekBarHelper(SeekBar seekBar, TextView textView, boolean floatingPoint) {
        this.seekBar = seekBar;
        this.textView = textView;
        this.floatingPoint = floatingPoint;

        this.seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    public void setOnProgressChangedListener(onProgressChangedListener onProgressChangedListener) {
        this.onProgressChangedListener = onProgressChangedListener;
    }

    public int valueToProgress(double value)
    {
        return (int) ((value / max) * seekBar.getMax());
    }

    public int getProgress() {
        return seekBar.getProgress();
    }

    public void setProgress(int progress) {
        /// onProgressChangedListener is only called when the progress is different from last.
        /// This will circumvent this.
        if (seekBar.getProgress() == progress)
        {
            onSeekBarChangeListener.onProgressChanged(seekBar, progress, false);
        }
        else
        {
            seekBar.setProgress(progress);
        }
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setMin(int min) {
        this.min = min;
    }
}
