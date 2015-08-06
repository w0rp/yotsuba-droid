package com.w0rp.yotsubadroid;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

public class SpanBuilder {
    public static ForegroundColorSpan fg(int color) {
        return new ForegroundColorSpan(color);
    }

    public static ForegroundColorSpan fg(int r, int g, int b) {
        return fg(Color.rgb(r, g, b));
    }

    public static BackgroundColorSpan bg(int color) {
        return new BackgroundColorSpan(color);
    }

    public static BackgroundColorSpan bg(int r, int g, int b) {
        return bg(Color.rgb(r, g, b));
    }

    public static UnderlineSpan underline() {
        return new UnderlineSpan();
    }

    public static StyleSpan bold() {
        return new StyleSpan(Typeface.BOLD);
    }

    public static StyleSpan italic() {
        return new StyleSpan(Typeface.ITALIC);
    }

    public static StrikethroughSpan strike() {
        return new StrikethroughSpan();
    }

    private final SpannableStringBuilder sb;

    public SpanBuilder() {
        sb = new SpannableStringBuilder();
    }

    public void append(String text, Object... spanList) {
        if (text == null || text.length() == 0) {
            return;
        }

        int start = sb.length();
        int end = start + text.length();

        sb.append(text);

        for (Object span : spanList) {
            sb.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public Spanned span() {
        return sb;
    }
}
