package com.ciandt.dragonfly.lens.ui;

import com.ciandt.dragonfly.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DragonflyLabelView extends LinearLayout {

    private TextView labelView;
    private TextView percentageView;

    public DragonflyLabelView(Context context) {
        super(context);
        initialize(context, null);
    }

    public DragonflyLabelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public DragonflyLabelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.dragonfly_label_view, this);

        this.labelView = (TextView) this.findViewById(R.id.labelTextView);
        this.percentageView = (TextView) this.findViewById(R.id.percentageTextView);
    }

    public void setInfo(String label, String percentage) {
        labelView.setText(label);
        percentageView.setText(percentage);
    }
}
