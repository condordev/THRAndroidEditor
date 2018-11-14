/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.sgrad.yamahathreditor;

import java.util.Locale;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


/**
 * KnobView is a widget for setting a real value by turning a virtual "knob".
 */
public class KnobView extends View {
  /** Basic constructor for an Android widget. */
  public KnobView(Context context, AttributeSet attrs) {
    super(context, attrs);

    // Load the xml attributes.
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.KnobView);
    knobValue_ = a.getFloat(R.styleable.KnobView_value, 0.5f);
    min_ = a.getFloat(R.styleable.KnobView_min, 0.0f);
    max_ = a.getFloat(R.styleable.KnobView_max, 1.0f);
    label_ = a.getString(R.styleable.KnobView_label);
    String numberFormat = a.getString(R.styleable.KnobView_numberformat);
    numberFormat_ = numberFormat != null ? numberFormat : "%.2f";
    horizontal_ = a.getBoolean(R.styleable.KnobView_horizontal, false);
    additionalText = a.getString(R.styleable.KnobView_additionalText);
    a.recycle();

    // Set up the drawing structures.
    paint_ = new Paint();
    paint_.setAntiAlias(true);
    paint_.setColor(Color.WHITE);
    float density = getResources().getDisplayMetrics().density;
    sensitivity_ = .005f / density;  // TODO: should be configurable
    strokeWidth_ = 1.0f * density;
    rect_ = new Rect();
    rectF_ = new RectF();
    innerRectF_ = new RectF();
    textHeight_ = 14f * density;  // probably should be set by XML parameter
    paint_.setTextSize(textHeight_);
    format = 0;  // default -> byte

    // The listener has to be set later.
    listener_ = null;
  }

  /**
   * Touch event handler.
   */
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    int action = event.getAction();
    switch (action) {
      case MotionEvent.ACTION_DOWN: {
        // Just record the current finger position.
        xyAtTouch_ = event.getX() - event.getY();
        valueAtTouch_ = knobValue_;
        getDrawingRect(rect_);
        break;
      }

      case MotionEvent.ACTION_MOVE: {
        float xyDelta = event.getX() - event.getY() - xyAtTouch_;
        knobValue_ = valueAtTouch_ + sensitivity_ * xyDelta;
        knobValue_ = Math.min(knobValue_, 1.0f);
        knobValue_ = Math.max(knobValue_, 0.0f);

        // Notify listener and redraw.
        if (listener_ != null) {
          listener_.onKnobChanged(getValue());
        }
        invalidate();

        break;
      }

      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP: {
        if (listenerUp_ != null) {
          listenerUp_.onKnobChanged(getValue());
        }
        break;
      }
    }
    return true;
  }

  /**
   * Sets the listener to receive events when the knob's value changes.
   */
  public void setKnobListener(KnobListener listener) {
    listener_ = listener;
  }

  /**
   * Sets the listener to receive events when the knob's value finalizes (on touch up).
   */
  public void setKnobListenerUp(KnobListener listener) {
    listenerUp_ = listener;
  }

  /**
   * Sets the value for the knob when it is turned all the way counter-clockwise.
   */
  public void setMin(double min) {
    min_ = min;
    invalidate();
  }

  /**
   * Sets the value for the knob when it is turned all the way clockwise.
   */
  public void setMax(double max) {
    max_ = max;
    invalidate();
  }

  /**
   * Sets the current value of the knob. Note that this call does not
   * invoke the listener. The assumption is that any caller will also
   * update the client.
   */
  public void setValue(double value) {
    if (value < min_) {
      knobValue_ = 0.0;
    } else if (value > max_) {
      knobValue_ = 1.0;
    } else {
      knobValue_ = (value - min_) / (max_ - min_);
    }
    invalidate();
  }

  /**
   * Returns the current value of the knob.
   */
  public double getValue() {
    return min_ + (knobValue_ * (max_ - min_));
  }
  
  /**
   * set if drawing optins of the value text
   */
  public void setValueDrawingOptions(DRAW_TEXT_OPTIONS valueOptions){
	  valueDrawingOptions = valueOptions;
  }
  
  /**
   * set the drawing options of the label text.
   *<br> Note: if you draw above knob, the label and the value will overlap, use setValueDrawingOptions() to specify value position
   */
  public void setLabelDrawingOptions(DRAW_TEXT_OPTIONS labelOptions){
	  labelDrawingOptions = labelOptions;
  }
  
  public void setAdditionalText(String text){
	  additionalText = text;  
  }
  
  public void setNumberFormat(String text){
	  if(text.equals("SHORT"))
		  format = 1;
	  else if(text.equals("BYTE"))
		  format = 0;
  }
  
  /**
   * Drawing handler.
   */
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    getDrawingRect(rect_);
    rectF_.set(rect_);
    // Apply padding.
    rectF_.left += getPaddingLeft();
    rectF_.right -= getPaddingRight();
    rectF_.top += getPaddingTop();
    rectF_.bottom -= getPaddingBottom();

    // Make it square.
    if (rectF_.height() > rectF_.width()) {
      float center = rectF_.centerY();
      rectF_.top = center - rectF_.width() / 2;
      rectF_.bottom = center + rectF_.width() / 2;
    } else {
      float center = rectF_.centerX();
      rectF_.left = center - rectF_.height() / 2;
      rectF_.right = center + rectF_.height() / 2;
    }

    float border = (horizontal_ ? 0.5f * textHeight_ : textHeight_) + rectF_.width() * 0.05f;

    // Draw indicator.
    innerRectF_.set(rectF_);
    float inner = border;
    innerRectF_.inset(inner, inner);
    paint_.setShader(null);
    paint_.setColor(Color.LTGRAY);
    paint_.setStyle(Style.STROKE);
    paint_.setStrokeWidth(rectF_.width() * 0.1f);
    float startAngle = startAngle_ + 90f;
    float range = 360f - 2 * startAngle_ - arcWidth_;
    float sweepAngle = (float)knobValue_ * range;
    canvas.drawArc(innerRectF_,startAngle,sweepAngle + 0.5f * arcWidth_,false,paint_);
    paint_.setColor(Color.WHITE);
    canvas.drawArc(innerRectF_, startAngle + sweepAngle, arcWidth_, false, paint_);

    // Draw knob circle.
    paint_.setColor(Color.WHITE);
    paint_.setStyle(Paint.Style.STROKE);
    paint_.setStrokeWidth(strokeWidth_);
    canvas.drawCircle(rectF_.centerX(), rectF_.centerY(), rectF_.width() * 0.45f - border, paint_);
	
    // Draw text.
    //String knobValueString = String.format(Locale.getDefault(), numberFormat_, getValue());
    String knobValueString = "";
    if(format == 0){
    	knobValueString = additionalText + Byte.toString((byte)getValue());
    } else{
    	knobValueString = additionalText + Short.toString((short)getValue());
    }
    Typeface typeface = Typeface.DEFAULT_BOLD;
    paint_.setColor(Color.WHITE);
    paint_.setTypeface(typeface);
    paint_.setTextAlign(horizontal_ ? Align.RIGHT : Align.CENTER);
    paint_.setSubpixelText(true);
    paint_.setStyle(Style.FILL);
    
    if(drawValue){   
    	float x = horizontal_ ? rectF_.left: rectF_.centerX();
    	float y = horizontal_ ? rectF_.centerY()  + 0.4f * textHeight_ : rectF_.top + 0.8f * textHeight_;
	    canvas.drawText(knobValueString, x, y, paint_);
    }
    
    switch (labelDrawingOptions) {
    	case DO_NOT_DRAW:
    		break;
    	case DRAW_BELOW_KNOB:
    		if (label_ != null) {
    			paint_.setTypeface(Typeface.DEFAULT);
    			canvas.drawText(label_, rect_.centerX(), rectF_.bottom - 0.2f * textHeight_, paint_);
    		}
    	break;
    	case DRAW_ABOVE_KNOB:
    		if (label_ != null) {
    			paint_.setTypeface(Typeface.DEFAULT);
    	    	float x = horizontal_ ? rectF_.left: rectF_.centerX();
    	    	float y = horizontal_ ? rectF_.centerY()  + 0.4f * textHeight_ : rectF_.top + 0.8f * textHeight_;
    			canvas.drawText(label_, x, y, paint_);
    		}    		
    	break;
		case DRAW_INSIDE:
			break;
		default:
		break;
    }
    
	switch (valueDrawingOptions) {
		case DO_NOT_DRAW:
			break;
		case DRAW_BELOW_KNOB:
	    	float y = horizontal_ ? rectF_.bottom - 0.2f * textHeight_ : rectF_.bottom - 0.4f * textHeight_;
		    canvas.drawText(knobValueString, rect_.centerX(), y, paint_);
			break;
		case DRAW_ABOVE_KNOB:
	    	float x = horizontal_ ? rectF_.left: rectF_.centerX();
	    	y = horizontal_ ? rectF_.centerY()  + 0.4f * textHeight_ : rectF_.top + 0.8f * textHeight_;
		    canvas.drawText(knobValueString, x, y, paint_);
			break;
		case DRAW_INSIDE:
			break;
		default:
			break;
	}
  }

  /**
   * Controls how the knob is sized;  it is square, and prefers to be 100x100 pixels.
   */
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
    int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
    if (!horizontal_) {
      // Make it square
      width = height = Math.min(width, height);
    }
    setMeasuredDimension(width, height);
  }
  
  public float calculateExponetialMovement(float x) {
	  float v = (float) ((Math.exp(2.77258872 * x) - 1) / 15);
	  v *= max_;
	 
	  // Check boundary conditions as Math.exp
	  // may round of too much.
	  if (x == 0) return 0;
	  if (x == 1) return (float) max_;
	  if (v < 0) v = 0;   if (v > max_) v = (float) max_;
	 
	  return (float) Math.ceil(v);
	}
  

  // Knob's current value, ranges from 0 - 1.0.
  private double knobValue_;
  private double min_;
  private double max_;
  private String numberFormat_;
  private int format;
  private boolean horizontal_;
  private String additionalText;

  // Structures used in drawing that we don't want to reallocate every time we draw.
  private Paint paint_;
  private Rect rect_;
  private RectF rectF_;
  private RectF innerRectF_;

  // Appearance and behavior parameters
  private final float textHeight_;
  private final float arcWidth_ = 4.0f;
  private final float startAngle_ = 36f;  // relative to bottom
  private final float strokeWidth_;
  private float sensitivity_;
  private String label_ = "labelg";
  private boolean drawValue = true;
  private DRAW_TEXT_OPTIONS labelDrawingOptions = DRAW_TEXT_OPTIONS.DRAW_BELOW_KNOB;
  private DRAW_TEXT_OPTIONS valueDrawingOptions = DRAW_TEXT_OPTIONS.DRAW_ABOVE_KNOB;
  
  public enum DRAW_TEXT_OPTIONS{
	  DO_NOT_DRAW, DRAW_BELOW_KNOB, DRAW_ABOVE_KNOB, DRAW_INSIDE;
  }
  
  // State of drag gesture
  private float xyAtTouch_;
  private double valueAtTouch_;

  // Object listening for events when the knob's value changes.
  private KnobListener listener_;
  private KnobListener listenerUp_;
  
}
