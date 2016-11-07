package nate.apps.fragmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.geometry.Point;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

/**
 * Created by nathan on 28/10/2016.
 * zone class containing data for zone and the polygon
 */
public class Zone extends JSONObject{
    private Polygon polygon;
    private Bitmap marker;
    private Context context;
    public Zone(JSONObject zone, Polygon polygon,Context context) throws JSONException {
        super(zone.toString());
        this.polygon = polygon;
        this.context = context;
        //set polygon colour
        reset();
        //generate marker to use over this zone
        setmarker();
    }

    public void reset() throws JSONException {
        boolean payment_allowed = getInt(MapsActivity.PAYMENT_ALLOWED)>0;
        polygon.setFillColor(Colour.from_id(context,payment_allowed?R.color.zone_pay_allowed:R.color.zone_pay_not_allowed));
        polygon.setStrokeColor(Colour.from_id(context,payment_allowed?R.color.zone_pay_allowed_stroke:R.color.zone_pay_not_allowed_stroke));
    }

    //method to produce a bitmap for marker
    private void setmarker() throws JSONException {
        //get price from json
        double price = getDouble(MapsActivity.PRICE);
        //format price with currency from json but seperator style accrding to device preference
        final String formatted_price = new DecimalFormat(getString(MapsActivity.CURRENCY)+"##,##0.00").format(price);
        //create text paint
        final Paint textpaint = new Paint();
        //set colour from resources
        textpaint.setColor(Colour.from_id(context,R.color.marker_text));
        textpaint.setTextSize(25f);
        //calculate size of text
        double text_height = Math.abs(textpaint.ascent()) + Math.abs(textpaint.descent());
        double text_width = textpaint.measureText(formatted_price);
        final int tailheight = 40;
        final int radius = 5;
        //calculate bitmap size
        final int bitmap_height = (int) Math.ceil(text_height + tailheight + 2*radius);
        final int bitmap_width = (int) Math.ceil(text_width + 2*radius);
        // create bitmap
        marker = Bitmap.createBitmap(bitmap_width,bitmap_height, Bitmap.Config.ARGB_8888);
        //draw marker in background to avoid holding up activity
        Thread drawmarker = new Thread(new Runnable() {
            public void run() {
                //create canvas from bitmap
                Canvas canvas = new Canvas(marker);
                //create marker paint
                Paint markerpaint = new Paint();
                //set colour from resources
                markerpaint.setShader(new LinearGradient(0, 2*radius, 0, bitmap_height-2*radius, Colour.from_id(context,R.color.marker_colour_a), Colour.from_id(context,R.color.marker_colour_b), Shader.TileMode.CLAMP));
                markerpaint.setStyle(Paint.Style.FILL);
                //create shadow paint
                Paint shadowpaint = new Paint();
                //set colour from resources
                shadowpaint.setColor(Colour.from_id(context,R.color.shadow_colour));
                shadowpaint.setStyle(Paint.Style.FILL);
                //draw shadow
                draw_pointer(canvas,shadowpaint,bitmap_width,bitmap_height,tailheight,radius,0);
                //draw pointer
                draw_pointer(canvas,markerpaint,bitmap_width,bitmap_height,tailheight,radius,1.5f);
                //draw price
                canvas.drawText(formatted_price,5,bitmap_height-55,textpaint);
            }
        });
        drawmarker.start();

    }

    //method to draw pointer shape
    private void draw_pointer(Canvas canvas, Paint paint, int bitmap_width, int bitmap_height, int tailheight, int radius, float padding) {
        //calculate path of pointer
        final RectF rightoval = new RectF(bitmap_width/2,bitmap_height-tailheight-radius-padding,3*bitmap_width/2-2*radius-padding,bitmap_height+tailheight+radius-padding);
        final RectF lefttoval = new RectF(2*radius+padding-bitmap_width/2,bitmap_height-tailheight-radius-padding,bitmap_width/2,bitmap_height+tailheight+radius-padding);
        Path path = new Path();
        //canvas.drawOval(lefttoval,markerpaint);
        //path.setFillType(Path.FillType.EVEN_ODD);
        path.arcTo(lefttoval,270,90,true);
        path.arcTo(rightoval,180,90,false);
        path.lineTo(radius,bitmap_height-tailheight-radius-padding);
        path.close();
        //draw marker
        canvas.drawRoundRect(padding,padding,bitmap_width-padding,bitmap_height-tailheight-radius-padding,radius*2,radius*2,paint);
        canvas.drawPath(path,paint);
    }


    public Polygon getPolygon() {
        return polygon;
    }

    public Bitmap getMarker() {
        return marker;
    }

    public void highlight() {
        polygon.setFillColor(Colour.from_id(context,R.color.zone_highlighted));
    }
}
