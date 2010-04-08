package jme3test.post;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * This test renders a scene to an offscreen framebuffer, then copies
 * the contents to a Swing JFrame. Note that some parts are done inefficently,
 * this is done to make the code more readable.
 */
public class TestRenderToMemory extends SimpleApplication implements SceneProcessor {

    private Geometry offBox;
    private float angle = 0;

    private FrameBuffer offBuffer;
    private ViewPort offView;
    private Texture2D offTex;
    private Camera offCamera;
    private ImageDisplay display;

    private static final int width = 800, height = 600;

    private final ByteBuffer cpuBuf = BufferUtils.createByteBuffer(width * height * 4);
    private final byte[] cpuArray = new byte[width * height * 4];
    private final BufferedImage image = new BufferedImage(width, height,
                                            BufferedImage.TYPE_4BYTE_ABGR);

    private class ImageDisplay extends JPanel {
        @Override
        public void paintComponent(Graphics gfx) {
            super.paintComponent(gfx);
            Graphics2D g2d = (Graphics2D) gfx;

            synchronized (image){
                g2d.drawImage(image, null, 0, 0);
            }
        }
    }

    public static void main(String[] args){
        TestRenderToMemory app = new TestRenderToMemory();
        app.setPauseOnLostFocus(false);
        app.start();
    }

    public void createDisplayFrame(){
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                JFrame frame = new JFrame("Render Display");
                display = new ImageDisplay();
                display.setPreferredSize(new Dimension(width, height));
                frame.getContentPane().add(display);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.pack();
                frame.setResizable(false);
                frame.setVisible(true);
            }
        });
    }

    public void updateImageContents(){
        cpuBuf.clear();
        renderer.readFrameBuffer(offBuffer, cpuBuf);

        // copy native memory to java memory
        cpuBuf.clear();
        cpuBuf.get(cpuArray);
        cpuBuf.clear();

        // flip the components the way AWT likes them
        for (int i = 0; i < width * height * 4; i+=4){
            byte b = cpuArray[i+0];
            byte g = cpuArray[i+1];
            byte r = cpuArray[i+2];
            byte a = cpuArray[i+3];

            cpuArray[i+0] = a;
            cpuArray[i+1] = b;
            cpuArray[i+2] = g;
            cpuArray[i+3] = r;
        }

        synchronized (image) {
            WritableRaster wr = image.getRaster();
            DataBufferByte db = (DataBufferByte) wr.getDataBuffer();
            System.arraycopy(cpuArray, 0, db.getData(), 0, cpuArray.length);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (display != null)
                    display.repaint();
            }
        });
    }

    public void setupOffscreenView(){
        offCamera = new Camera(width, height);

        // create a pre-view. a view that is rendered before the main view
        offView = renderManager.createPreView("Offscreen View", offCamera);
        offView.setBackgroundColor(ColorRGBA.DarkGray);
        
        // this will let us know when the scene has been rendered to the 
        // frame buffer
        offView.addProcessor(this);

        // create offscreen framebuffer
        offBuffer = new FrameBuffer(width, height, 0);

        //setup framebuffer's cam
        offCamera.setFrustumPerspective(45f, 1f, 1f, 1000f);
        offCamera.setLocation(new Vector3f(0f, 0f, -5f));
        offCamera.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);

        //setup framebuffer's texture
//        offTex = new Texture2D(width, height, Format.RGBA8);

        //setup framebuffer to use renderbuffer
        // this is faster for gpu -> cpu copies
        offBuffer.setDepthBuffer(Format.Depth);
        offBuffer.setColorBuffer(Format.RGBA8);
//        offBuffer.setColorTexture(offTex);
        
        //set viewport to render to offscreen framebuffer
        offView.setOutputFrameBuffer(offBuffer);

        // setup framebuffer's scene
        Box boxMesh = new Box(Vector3f.ZERO, 1,1,1);
        Material material = (Material) manager.loadContent("jme_logo.j3m");
        offBox = new Geometry("box", boxMesh);
        offBox.setMaterial(material);

        // attach the scene to the viewport to be rendered
        offView.attachScene(offBox);
    }

    @Override
    public void simpleInitApp() {
        setupOffscreenView();
        createDisplayFrame();
    }

    @Override
    public void simpleUpdate(float tpf){
        Quaternion q = new Quaternion();
        angle += tpf;
        angle %= FastMath.TWO_PI;
        q.fromAngles(angle, 0, angle);

        offBox.setLocalRotation(q);
        offBox.updateLogicalState(tpf);
        offBox.updateGeometricState();
    }

    public void initialize(RenderManager rm, ViewPort vp) {
    }

    public void reshape(ViewPort vp, int w, int h) {
    }

    public boolean isInitialized() {
        return true;
    }

    public void preFrame(float tpf) {
    }

    public void postQueue(RenderQueue rq) {
    }

    /**
     * Update the CPU image's contents after the scene has
     * been rendered to the framebuffer.
     */
    public void postFrame(FrameBuffer out) {
        updateImageContents();
    }

    public void cleanup() {
    }


}
