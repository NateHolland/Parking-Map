package nate.apps.fragmap;

import android.app.Fragment;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by nathan on 29/10/2016.
 */
public class DataStoreFragment extends Fragment {

    // camera frame
    private float zoom;
    private LatLng target = null;

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    public void setCamera(float zoom, LatLng target) {
        this.zoom = zoom;
        this.target = target;
    }

    public LatLng getTarget() {
        return target;
    }

    public float getZoom() {
        return zoom;
    }
}
