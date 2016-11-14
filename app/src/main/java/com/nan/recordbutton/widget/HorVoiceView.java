package com.nan.recordbutton.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.nan.recordbutton.R;

import java.util.LinkedList;

public class HorVoiceView extends View {

    private Paint paint;
    private int color;
    private float lineHeight = 8;
    private float maxLineheight;
    private float lineWidth;
    private float textSize;
    private String text = " 15 ";
    private int textColor;
    private Thread mThread;
    private int milliSeconds;
    private boolean isStart = false;
    private Runnable mRunable;

    LinkedList<Integer> list = new LinkedList<>();

    public HorVoiceView(Context context) {
        super(context);
    }

    public HorVoiceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorVoiceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        for (int i = 0; i < 10; i++) {
            list.add(1);
        }
        paint = new Paint();
        mRunable = new Runnable() {
            @Override
            public void run() {
                while (isStart) {
                    milliSeconds += 200;
                    text = milliSeconds / 1000 < 10 ? "0" + milliSeconds / 1000 : milliSeconds / 1000 + "";
                    Log.e("horvoiceview", "text " + text);
                    setVoice(1);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    postInvalidate();
                }
            }
        };
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.HorVoiceView);
        color = mTypedArray.getColor(R.styleable.HorVoiceView_voiceLineColor, Color.BLACK);
        lineWidth = mTypedArray.getDimension(R.styleable.HorVoiceView_voiceLineWidth, 35);
        lineHeight = mTypedArray.getDimension(R.styleable.HorVoiceView_voiceLineHeight, 8);
        maxLineheight = mTypedArray.getDimension(R.styleable.HorVoiceView_voiceLineHeight, 32);
        textSize = mTypedArray.getDimension(R.styleable.HorVoiceView_voiceTextSize, 45);
        textColor = mTypedArray.getColor(R.styleable.HorVoiceView_voiceTextColor, Color.BLACK);
        mTypedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int widthcentre = getWidth() / 2;
        int heightcentre = getHeight() / 2;

        paint.setStrokeWidth(0);
        paint.setColor(textColor);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        float textWidth = paint.measureText(text);
        canvas.drawText(text, widthcentre - textWidth / 2, heightcentre - (paint.ascent() + paint.descent()) / 2, paint);

//        Log.e("Voice", text);

        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(lineWidth);
        paint.setAntiAlias(true);
        for (int i = 0; i < 10; i++) {
            RectF rect = new RectF(widthcentre + 2 * i * lineHeight + textWidth / 2 + lineHeight, heightcentre - list.get(i) * lineHeight / 2, widthcentre + 2 * i * lineHeight + 2 * lineHeight + textWidth / 2, heightcentre + list.get(i) * lineHeight / 2);
            RectF rect2 = new RectF(widthcentre - (2 * i * lineHeight + 2 * lineHeight + textWidth / 2), heightcentre - list.get(i) * lineHeight / 2, widthcentre - (2 * i * lineHeight + textWidth / 2 + lineHeight), heightcentre + list.get(i) * lineHeight / 2);
            canvas.drawRect(rect, paint);
            canvas.drawRect(rect2, paint);
        }
    }

    public synchronized void setVoice(Integer height) {
        for (int i = 0; i <= height / 30; i++) {
            list.remove(9 - i);
            list.add(i, (height / 20 - i) < 1 ? 1 : height / 20 - i);
        }
//        Log.e("波峰", "height" + height);
        postInvalidate();
    }

    public synchronized void setText(String text) {
        this.text = text;
        postInvalidate();
    }

    public synchronized void startRecording() {
        milliSeconds = 0;
        isStart = true;
        new Thread(mRunable).start();
    }

    public synchronized void stopRecord() {
        isStart = false;
        list.clear();
        for (int i = 0; i < 10; i++) {
            list.add(1);
        }
        text = "00";
        postInvalidate();
    }
}