package g3dtest.light;

import com.g3d.app.SimpleApplication;
import com.g3d.material.Material;
import com.g3d.math.ColorRGBA;
import com.g3d.math.Quaternion;
import com.g3d.math.Vector3f;
import com.g3d.renderer.Camera;
import com.g3d.renderer.queue.RenderQueue.ShadowMode;
import com.g3d.scene.Geometry;
import com.g3d.scene.Spatial;
import com.g3d.scene.dbg.WireFrustum;
import com.g3d.scene.shape.Box;
import com.g3d.shadow.BasicShadowRenderer;
import com.g3d.shadow.ShadowUtil;

public class TestShadow extends SimpleApplication {

    float angle;
    Spatial lightMdl;
    Spatial teapot;
    Geometry frustumMdl;
    WireFrustum frustum;

    private BasicShadowRenderer bsr;
    private Vector3f[] points;

    {
        points = new Vector3f[8];
        for (int i = 0; i < points.length; i++) points[i] = new Vector3f();
    }

    public static void main(String[] args){
        TestShadow app = new TestShadow();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // put the camera in a bad position
        cam.setLocation(new Vector3f(0.7804813f, 1.7502685f, -2.1556435f));
        cam.setRotation(new Quaternion(0.1961598f, -0.7213164f, 0.2266092f, 0.6243975f));
        cam.setFrustumFar(10);

        Material mat = manager.loadMaterial("white_color.j3m");
        rootNode.setShadowMode(ShadowMode.Off);
        Box floor = new Box(Vector3f.ZERO, 3, 0.1f, 3);
        Geometry floorGeom = new Geometry("Floor", floor);
        floorGeom.setMaterial(mat);
        floorGeom.setLocalTranslation(0,-0.2f,0);
        floorGeom.updateModelBound();
        floorGeom.setShadowMode(ShadowMode.Recieve);
        rootNode.attachChild(floorGeom);

        teapot = manager.loadModel("teapot.obj");
        teapot.setLocalScale(2f);
        teapot.setMaterial(mat);
        teapot.setShadowMode(ShadowMode.CastAndRecieve);
        rootNode.attachChild(teapot);
//        lightMdl = new Geometry("Light", new Sphere(10, 10, 0.1f));
//        lightMdl.setMaterial(mat);
//        // disable shadowing for light representation
//        lightMdl.setShadowMode(ShadowMode.Off);
//        rootNode.attachChild(lightMdl);

        bsr = new BasicShadowRenderer(manager, 512);
        viewPort.addProcessor(bsr);

        frustum = new WireFrustum(bsr.getPoints());
        frustumMdl = new Geometry("f", frustum);
        frustumMdl.setCullHint(Spatial.CullHint.Never);
        frustumMdl.setShadowMode(ShadowMode.Off);
        frustumMdl.setMaterial(new Material(manager, "wire_color.j3md"));
        frustumMdl.getMaterial().setColor("m_Color", ColorRGBA.Red);
        rootNode.attachChild(frustumMdl);
    }

    @Override
    public void simpleUpdate(float tpf){
        Camera shadowCam = bsr.getShadowCamera();
        ShadowUtil.updateFrustumPoints2(shadowCam, shadowCam.getFrustumNear(),
                                       shadowCam.getFrustumFar(),
                                       1.0f,
                                       points);


        frustum.update(points);
        frustumMdl.updateModelBound();
        // rotate teapot around Y axis
        teapot.rotate(0, tpf * 0.25f, 0);
    }

}