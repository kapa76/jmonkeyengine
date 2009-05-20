package com.g3d.renderer.queue;

import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.renderer.Renderer;
import com.g3d.scene.Spatial;
import com.g3d.util.TempVars;
import java.util.Comparator;

public class OpaqueComparator implements Comparator<Spatial> {

    private Renderer renderer;

    public OpaqueComparator(Renderer renderer){
        this.renderer = renderer;
    }

    public float distanceToCam(Spatial spat, Vector3f tempVec){
        if (spat == null)
            return Float.NEGATIVE_INFINITY;

        if (spat.queueDistance != Float.NEGATIVE_INFINITY)
                return spat.queueDistance;

        Camera cam = renderer.getCamera();

        Vector3f camPosition = cam.getLocation();
        Vector3f spatPosition = null;

        if (spat.getWorldBound() != null){
            spatPosition = spat.getWorldBound().getCenter();
        }else{
            spatPosition = spat.getWorldTranslation();
        }

        spatPosition.subtract(camPosition, tempVec);
        spat.queueDistance = tempVec.dot(tempVec);

//        float retval = Math.abs(tempVec.dot(viewVector)
//                / viewVector.dot(viewVector));
//        tempVec = viewVector.mult(retval, tempVec);
//
//        spat.queueDistance = tempVec.length();

        return spat.queueDistance;
    }

    public int compare(Spatial o1, Spatial o2) {
//        Camera cam = renderer.getCamera();

        Vector3f tempVec = TempVars.get().vect1;
        float d1 = distanceToCam(o1, tempVec);
        float d2 = distanceToCam(o2, tempVec);

        if (d1 == d2)
            return 0;
        else if (d1 < d2)
            return 1;
        else
            return -1;
    }
}
