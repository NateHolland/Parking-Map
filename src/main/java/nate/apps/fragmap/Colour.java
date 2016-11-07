package nate.apps.fragmap;

import android.content.Context;
import android.os.Build;

/**
 * Created by nathan on 28/10/2016.
 * class to get colour value from resources
 */
public class Colour {
    public static int from_id(Context context, int id) {
        //different calls depending on api version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getResources().getColor(id,null);
        }else{
            return context.getResources().getColor(id);
        }
    }
}
