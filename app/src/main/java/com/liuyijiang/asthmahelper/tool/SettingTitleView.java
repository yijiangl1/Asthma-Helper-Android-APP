package com.liuyijiang.asthmahelper.tool;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatTextView;

import com.liuyijiang.asthmahelper.R;

public class SettingTitleView extends AppCompatTextView {

    private int heightLeft;
    private int widthLeft;
    private int heightRight;
    private int widthRight;
    private int heightTop;
    private int widthTop;
    private int heightBottom;
    private int widthBottom;
    private Drawable drawableLeft;
    private Drawable drawableRight;
    private Drawable drawableTop;
    private Drawable drawableBottom;

    public SettingTitleView(Context context) {
        super(context);
    }

    public SettingTitleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
        drawDrawable();
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SettingTitleView);

        widthLeft = a.getDimensionPixelSize(R.styleable.SettingTitleView_drawable_width_left, 0);
        heightLeft = a.getDimensionPixelSize(R.styleable.SettingTitleView_drawable_height_left, 0);
        widthRight = a.getDimensionPixelSize(R.styleable.SettingTitleView_drawable_width_right, 0);
        heightRight = a.getDimensionPixelSize(R.styleable.SettingTitleView_drawable_height_right, 0);
        widthTop = a.getDimensionPixelSize(R.styleable.SettingTitleView_drawable_width_top, 0);
        heightTop = a.getDimensionPixelSize(R.styleable.SettingTitleView_drawable_height_top, 0);
        widthBottom = a.getDimensionPixelSize(R.styleable.SettingTitleView_drawable_width_bottom, 0);
        heightBottom = a.getDimensionPixelSize(R.styleable.SettingTitleView_drawable_height_bottom, 0);

        drawableLeft = a.getDrawable(R.styleable.SettingTitleView_drawable_src_left);
        drawableRight = a.getDrawable(R.styleable.SettingTitleView_drawable_src_right);
        drawableTop = a.getDrawable(R.styleable.SettingTitleView_drawable_src_top);
        drawableBottom = a.getDrawable(R.styleable.SettingTitleView_drawable_src_bottom);

        a.recycle();
    }

    private void drawDrawable() {
        drawableLeft = scaleDrawable(drawableLeft, widthLeft, heightLeft);
        drawableRight = scaleDrawable(drawableRight, widthRight, heightRight);
        drawableTop = scaleDrawable(drawableTop, widthTop, heightTop);
        drawableBottom = scaleDrawable(drawableBottom, widthBottom, heightBottom);
        this.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, drawableRight, drawableBottom);
    }

    private Drawable scaleDrawable(Drawable original, int newWidth, int newHeight) {
        if (original != null) {
            Bitmap bitmap = ((BitmapDrawable) original).getBitmap();
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Drawable drawable;
            if (newWidth != 0 && newHeight != 0) {
                float scaleWidth = (float) newWidth / width;
                float scaleHeight = (float) newHeight / height;
                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleHeight);
                Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                drawable = new BitmapDrawable(getResources(), scaledBitmap);
            } else {
                drawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, width, height, true));
            }
            return drawable;
        } else {
            return null;
        }
    }

    public void setDrawableRight(int newDrawableRightID) {
        drawableRight = getResources().getDrawable(newDrawableRightID);
        drawableRight = scaleDrawable(drawableRight, widthRight, heightRight);
        this.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, drawableRight, drawableBottom);
    }

    public void setDrawableTop(int newDrawableRightID) {
        drawableTop = getResources().getDrawable(newDrawableRightID);
        drawableTop = scaleDrawable(drawableTop, widthTop, heightTop);
        this.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, drawableRight, drawableBottom);
    }
}
