package com.jme.scene.model.XMLparser;

import com.jme.scene.*;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.TextureState;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.math.Vector2f;
import com.jme.renderer.ColorRGBA;
import com.jme.animation.VertexKeyframeController;

import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.*;
import java.net.URL;


/**
 * Started Date: Jun 25, 2004<br><br>
 *
 * This class converts a scenegraph to jME binary format.  Even though this
 * class's name ends with Writer, it does not extend Writer
 *
 * @author Jack Lindamood
 */
public class JmeBinaryWriter {
    DataOutputStream myOut;
    private final static boolean DEBUG=false;
    private IdentityHashMap sharedObjects=new IdentityHashMap(20);
    private IdentityHashMap entireScene=new IdentityHashMap(256);

    private static final Quaternion DEFAULT_ROTATION=new Quaternion();
    private static final Vector3f DEFAULT_TRANSLATION = new Vector3f();
    private static final Vector3f DEFAULT_SCALE = new Vector3f(1,1,1);

    /**
     * Creates a new Binary Writer
     */
    public JmeBinaryWriter(){

    }

    /**
     * Converts a given node to jME's binary format
     * @param scene The node to save
     * @param bin The OutputStream that will store the binary format
     * @throws IOException If anything wierd happens.
     */
    public void writeScene(Node scene,OutputStream bin) throws IOException {
        myOut=new DataOutputStream(bin);
        sharedObjects.clear();
        entireScene.clear();
        writeHeader();
        findDuplicates(scene);
        writeDuplicates();
        writeNode(scene);
        writeClosing();
        myOut.close();
    }

    private void writeDuplicates() throws IOException {
        if (sharedObjects.size()==0)
            return;

        IdentityHashMap temp=sharedObjects;
        sharedObjects=new IdentityHashMap();

        writeTag("sharedtypes",null);
        List l=new ArrayList(temp.keySet());
        for (int i=0;i<l.size();i++){
            String name=(String) temp.get(l.get(i));
            HashMap atts=new HashMap();
            atts.put("ident",name);
            if (l.get(i) instanceof RenderState){
                writeTag("sharedrenderstate",atts);
                writeRenderState((RenderState) l.get(i));
                writeEndTag("sharedrenderstate");
            }
        }
        writeEndTag("sharedtypes");
        sharedObjects=temp;
    }

    private void findDuplicates(Spatial n) {
        if (n==null) return;
        if (entireScene.containsKey(n)){
            sharedObjects.put(n,entireScene.get(n));
            return;
        }
        entireScene.put(n,n.getName());
        evaluateSpatialChildren(n);
        if (n instanceof Node){
            Node newN=(Node)n;
            for (int i=0;i<newN.getQuantity();i++)
                findDuplicates(newN.getChild(i));
        }
    }

    private void evaluateSpatialChildren(Spatial s) {
        if (s==null) return;
        for (int i=0;i<s.getControllers().size();i++){
            Controller evaluCont=s.getController(i);
            if (evaluCont==null) continue;
            if (entireScene.containsKey(evaluCont))
                sharedObjects.put(evaluCont,entireScene.get(evaluCont));
            else
                entireScene.put(evaluCont,"controller"+(s.hashCode()*evaluCont.hashCode()));
        }

        for (int i=0;i<s.getRenderStateList().length;i++){
            RenderState evaluRend=s.getRenderStateList()[i];
            if (evaluRend==null) continue;
            if (entireScene.containsKey(evaluRend))
                sharedObjects.put(evaluRend,entireScene.get(evaluRend));
            else
                entireScene.put(evaluRend,"RenderState"+(s.hashCode()*evaluRend.hashCode()));
        }
    }

    /**
     * Converts a given Geometry to jME's binary format
     * @param geo The Geometry to save
     * @param bin The OutputStream that will store the binary format
     * @throws IOException If anything wierd happens.
     */
    public void writeScene(Geometry geo,OutputStream bin) throws IOException {
        myOut=new DataOutputStream(bin);
        sharedObjects.clear();
        entireScene.clear();
        writeHeader();
        findDuplicates(geo);
        writeSpatial(geo);
        writeClosing();
        myOut.close();
    }

    /**
     * Writes a node to binary format
     * @param node The node to write
     * @throws IOException If anything bad happens.
     */
    private void writeNode(Node node) throws IOException {
        HashMap atts=new HashMap();
        atts.clear();
        putSpatialAtts(node,atts);
        writeTag("node",atts);
        writeChildren(node);
        writeSpatialChildren(node);
        writeEndTag("node");
    }

    /**
     * Writes a Node's children.  writeSpatial is called on each child
     * @param node The node who's children you want to write
     * @throws IOException
     */
    private void writeChildren(Node node) throws IOException {
        for (int i=0;i<node.getQuantity();i++)
            writeSpatial(node.getChild(i));
    }

    /**
     * Writes a Spatial to binary format
     * @param s The spatial to write
     * @throws IOException
     */
    private void writeSpatial(Spatial s) throws IOException {
        if (s instanceof XMLloadable)
            writeXMLloadable((XMLloadable)s);
        else if (s instanceof JointMesh)
            writeJointMesh((JointMesh)s);
        else if (s instanceof Node)
            writeNode((Node) s);
        else if (s instanceof TriMesh)
            writeMesh((TriMesh)s);
    }

    /**
     * Writes a mesh to binary format
     * @param triMesh The mesh to write
     * @throws IOException
     */
    private void writeMesh(TriMesh triMesh) throws IOException {
        HashMap atts=new HashMap();
        atts.clear();
        putSpatialAtts(triMesh,atts);
        writeTag("mesh",atts);
        writeTriMeshTags(triMesh);
        writeSpatialChildren(triMesh);
        writeEndTag("mesh");
    }

    /**
     * Writes a JointMesh to binary format
     * @param jointMesh
     * @throws IOException
     */
    private void writeJointMesh(JointMesh jointMesh) throws IOException {
        HashMap atts=new HashMap();
        atts.clear();
        putSpatialAtts(jointMesh,atts);
        writeTag("jointmesh",atts);
        writeJointMeshTags(jointMesh);
        writeSpatialChildren(jointMesh);
        writeEndTag("jointmesh");
    }

    /**
     * Writes the inner tags of a JointMesh to binary format
     * @param jointMesh Mesh who's tags are to be written
     * @throws IOException
     */
    private void writeJointMeshTags(JointMesh jointMesh) throws IOException {
        HashMap atts=new HashMap();
        atts.clear();
        atts.put("data",jointMesh.jointIndex);
        writeTag("jointindex",atts);
        writeEndTag("jointindex");

        atts.clear();
        atts.put("data",jointMesh.originalVertex);
        writeTag("origvertex",atts);
        writeEndTag("origvertex");

        atts.clear();
        atts.put("data",jointMesh.originalNormal);
        writeTag("orignormal",atts);
        writeEndTag("orignormal");
        writeTriMeshTags(jointMesh);
    }

    /**
     * Writes an XMLloadable class to binary format
     * @param xmlloadable The class to write
     * @throws IOException
     */
    private void writeXMLloadable(XMLloadable xmlloadable) throws IOException {
        HashMap atts=new HashMap();
        atts.put("class",xmlloadable.getClass().getName());
        if (xmlloadable instanceof Spatial)
            putSpatialAtts((Spatial) xmlloadable,atts);
        atts.put("args",xmlloadable.writeToXML());
        writeTag("xmlloadable",atts);
        if (xmlloadable instanceof Spatial)
            writeSpatialChildren((Spatial) xmlloadable);
        if (xmlloadable instanceof Node)
            writeChildren((Node) xmlloadable);
        writeEndTag("xmlloadable");
    }

    /**
     * Writes a spatial's children (RenderStates and Controllers) to binary format
     * @param spatial Spatial to write
     * @throws IOException
     */
    private void writeSpatialChildren(Spatial spatial) throws IOException {
        writeRenderStates(spatial);
        writeControllers(spatial);
    }

    /**
     * Writes a Controller acording to which type of controller it is.  Only writes
     * known controllers
     * @param spatial The spatial who's controllers need to be written
     * @throws IOException
     */
    private void writeControllers(Spatial spatial) throws IOException {
        ArrayList conts=spatial.getControllers();
        if (conts==null) return;
        for (int i=0;i<conts.size();i++){
            Controller r=(Controller) conts.get(i);
            if (r instanceof JointController){
                writeJointController((JointController)r);
            } else if (r instanceof VertexKeyframeController){

            } else if (r instanceof KeyframeController){
                writeKeyframeController((KeyframeController)r);
            }
        }
    }

    /**
     * Writes a KeyframeController to binary format
     * @param kc KeyframeControlelr to write
     * @throws IOException
     */
    private void writeKeyframeController(KeyframeController kc) throws IOException {
        // Assume that morphMesh is keyframeController's parent
        writeTag("keyframecontroller",null);
        ArrayList keyframes=kc.keyframes;
        for (int i=0;i<keyframes.size();i++)
            writeKeyFramePointInTime((KeyframeController.PointInTime)keyframes.get(i));

        writeEndTag("keyframecontroller");
    }

    /**
     * Writes a PointInTime for a KeyframeController
     * @param pointInTime Which point in time to write
     * @throws IOException
     */
    private void writeKeyFramePointInTime(KeyframeController.PointInTime pointInTime) throws IOException {
        HashMap atts=new HashMap();
        atts.clear();
        atts.put("time",new Float(pointInTime.time));
        writeTag("keyframepointintime",atts);
        writeTriMeshTags(pointInTime.newShape);
        writeEndTag("keyframepointintime");
    }

    /**
     * Writes the inner tags of a TriMesh (Verticies, Normals, ect) to binary format
     * @param triMesh The TriMesh whos tags are to be written
     * @throws IOException
     */
    private void writeTriMeshTags(TriMesh triMesh) throws IOException {
        if (triMesh==null) return;
        HashMap atts=new HashMap();
        atts.clear();
        if (triMesh.getVertices()!=null)
            atts.put("data",triMesh.getVertices());
        writeTag("vertex",atts);
        writeEndTag("vertex");

        atts.clear();
        if (triMesh.getNormals()!=null)
            atts.put("data",triMesh.getNormals());
        writeTag("normal",atts);
        writeEndTag("normal");

        atts.clear();
        if (triMesh.getColors()!=null)
            atts.put("data",triMesh.getColors());
        writeTag("color",atts);
        writeEndTag("color");

        atts.clear();
        if (triMesh.getTextures()!=null)
            atts.put("data",triMesh.getTextures());
        writeTag("texturecoords",atts);
        writeEndTag("texturecoords");

        atts.clear();
        if (triMesh.getIndices()!=null)
            atts.put("data",triMesh.getIndices());
        writeTag("index",atts);
        writeEndTag("index");
    }

    private void writeJointController(JointController jc) throws IOException{
        HashMap atts=new HashMap();
        atts.clear();
        atts.put("numJoints",new Integer(jc.numJoints));
        writeTag("jointcontroller",atts);
        Object[] o=jc.movementInfo.toArray();
        Vector3f tempV=new Vector3f();
        Quaternion tempQ=new Quaternion();
        for (int j=0;j<jc.numJoints;j++){
            atts.clear();
            atts.put("index",new Integer(j));
            atts.put("parentindex",new Integer(jc.parentIndex[j]));
            jc.localRefMatrix[j].getRotation(tempQ);
            jc.localRefMatrix[j].getTranslation(tempV);
            atts.put("localrot",tempQ);
            atts.put("localvec",tempV);

            writeTag("joint",atts);
            for (int i=0;i<o.length;i++){
                JointController.PointInTime jp=(JointController.PointInTime) o[i];
                if (jp.usedTrans.get(j) || jp.usedRot.get(j)){
                    atts.clear();
                    atts.put("time",new Float(jp.time));
                    if (jp.usedTrans.get(j))
                        atts.put("trans",jp.jointTranslation[j]);

                    if (jp.usedRot.get(j))
                        atts.put("rot",jp.jointRotation[j]);
                    writeTag("keyframe",atts);
                    writeEndTag("keyframe");
                }
            }
            writeEndTag("joint");
        }
        writeEndTag("jointcontroller");
    }

    /**
     * Writes a spatial's RenderStates to binary format.
     * @param spatial The spatial to look at.
     * @throws IOException
     */
    private void writeRenderStates(Spatial spatial) throws IOException {
        RenderState[] states=spatial.getRenderStateList();
        if (states==null) return;
        for (int i=0;i<states.length;i++)
            writeRenderState(states[i]);
    }

    /**
     * Writes a single render state to binary format.  Only writes known RenderStates
     * @param renderState The state to write
     * @throws IOException
     */
    private void writeRenderState(RenderState renderState) throws IOException {
        if (renderState==null) return;
        if (sharedObjects.containsKey(renderState))
            writePublicObject(renderState);
        else if (renderState instanceof MaterialState)
            writeMaterialState((MaterialState) renderState);
        else if (renderState instanceof TextureState)
            writeTextureState((TextureState)renderState);
    }

    private void writePublicObject(Object o) throws IOException {
        String ident=(String) sharedObjects.get(o);
        HashMap atts=new HashMap();
        atts.clear();
        atts.put("ident",ident);
        writeTag("publicobject",atts);
        writeEndTag("publicobject");
    }

    /**
     * Writes a TextureState to binary format.  An attempt is made to look at the
     * TextureState's ImageLocation to determine how to point the TextureState's information
     * @param state The state to write
     * @throws IOException
     */
    private void writeTextureState(TextureState state) throws IOException{
        if (state.getTexture()==null || state.getTexture().getImageLocation()==null) return;
        String s=state.getTexture().getImageLocation();
        HashMap atts=new HashMap();
        atts.clear();
        if ("file:/".equals(s.substring(0,6)))
            atts.put("file",replaceSpecialsForFile(new StringBuffer(s.substring(6))).toString());
        else
            atts.put("URL",new URL(s));
        writeTag("texturestate",atts);
        writeEndTag("texturestate");
    }

    /**
     * Replaces "%20" with " " to convert from a URL to a file
     * @param s String to look at
     * @return A replaced string.
     */
    private static StringBuffer replaceSpecialsForFile(StringBuffer s) {
        int i=s.indexOf("%20");
        if (i==-1) return s; else return replaceSpecialsForFile(s.replace(i,i+3," "));
    }

    /**
     * Writes a MaterialState to binary format
     * @param state The state to write
     * @throws IOException
     */
    private void writeMaterialState(MaterialState state) throws IOException {
        if (state==null) return;
        HashMap atts=new HashMap();
        atts.clear();
        atts.put("emissive",state.getEmissive());
        atts.put("ambient",state.getAmbient());
        atts.put("diffuse",state.getDiffuse());
        atts.put("specular",state.getSpecular());
        atts.put("alpha",new Float(state.getAlpha()));
        atts.put("shiny",new Float(state.getShininess()));
        writeTag("materialstate",atts);
        writeEndTag("materialstate");
    }

    /**
     * Writes an END_TAG flag for the given tag
     * @param name The name of the tag whos end has come
     * @throws IOException
     */
    private void writeEndTag(String name) throws IOException{
        if (DEBUG) System.out.println("Writting end tag for *" + name + "*");
        myOut.writeByte(BinaryFormatConstants.END_TAG);
        myOut.writeUTF(name);
    }

    /**
     * Given the tag's name and it's attributes, the tag is written to the file
     * @param name The name of the tag
     * @param atts The tag's attributes
     * @throws IOException
     */
    private void writeTag(String name, HashMap atts) throws IOException {
        if (DEBUG) System.out.println("Writting begining tag for *" + name + "*");
        myOut.writeByte(BinaryFormatConstants.BEGIN_TAG);
        myOut.writeUTF(name);
        if (atts==null){    // no attributes
            myOut.writeByte(0);
            return;
        }
        myOut.writeByte(atts.size());

        Iterator i=atts.keySet().iterator();
        while (i.hasNext()){
            String attName=(String) i.next();
            Object attrib=atts.get(attName);
            myOut.writeUTF(attName);
            if (attrib instanceof Vector3f[])
                writeVec3fArray((Vector3f[]) attrib);
            else if (attrib instanceof Vector2f[])
                writeVec2fArray((Vector2f[]) attrib);
            else if (attrib instanceof ColorRGBA[])
                writeColorArray((ColorRGBA[]) attrib);
            else if (attrib instanceof String)
                writeString((String) attrib);
            else if (attrib instanceof int[])
                writeIntArray((int[]) attrib);
            else if (attrib instanceof Vector3f)
                writeVec3f((Vector3f) attrib);
            else if (attrib instanceof Quaternion)
                writeQuat((Quaternion) attrib);
            else if (attrib instanceof Float)
                writeFloat((Float) attrib);
            else if (attrib instanceof ColorRGBA)
                writeColor((ColorRGBA) attrib);
            else if (attrib instanceof URL)
                writeURL((URL) attrib);
            else if (attrib instanceof Integer)
                writeInt((Integer) attrib);
            else{
                throw new IOException("unknown class type for " + attrib + " of " + attrib.getClass());
            }
            i.remove();
        }
    }

    private void writeInt(Integer i) throws IOException {
        myOut.writeByte(BinaryFormatConstants.DATA_INT);
        myOut.writeInt(i.intValue());
    }

    private void writeURL(URL url) throws IOException {
        myOut.writeByte(BinaryFormatConstants.DATA_URL);
        myOut.writeUTF(url.toString());
    }

    private void writeColor(ColorRGBA c) throws IOException {
        myOut.writeByte(BinaryFormatConstants.DATA_COLOR);
        myOut.writeFloat(c.r);
        myOut.writeFloat(c.g);
        myOut.writeFloat(c.b);
        myOut.writeFloat(c.a);
    }

    private void writeFloat(Float f) throws IOException {
        myOut.writeByte(BinaryFormatConstants.DATA_FLOAT);
        myOut.writeFloat(f.floatValue());
    }

    private void writeQuat(Quaternion q) throws IOException {
        myOut.writeByte(BinaryFormatConstants.DATA_QUAT);
        myOut.writeFloat(q.x);
        myOut.writeFloat(q.y);
        myOut.writeFloat(q.z);
        myOut.writeFloat(q.w);
    }

    private void writeVec3f(Vector3f v) throws IOException {
        myOut.writeByte(BinaryFormatConstants.DATA_V3F);
        myOut.writeFloat(v.x);
        myOut.writeFloat(v.y);
        myOut.writeFloat(v.z);
    }

    private void writeIntArray(int[] array) throws IOException {
        myOut.writeByte(BinaryFormatConstants.DATA_INTARRAY);
        myOut.writeInt(array.length);
        for (int i=0;i<array.length;i++)
            myOut.writeInt(array[i]);
    }

    private void writeString(String s) throws IOException {
        myOut.writeByte(BinaryFormatConstants.DATA_STRING);
        myOut.writeUTF(s);
    }

    private void writeColorArray(ColorRGBA[] array) throws IOException {
        myOut.writeByte(BinaryFormatConstants.DATA_COLORARRAY);
        myOut.writeInt(array.length);
        for (int i=0;i<array.length;i++){
            myOut.writeFloat(array[i].r);
            myOut.writeFloat(array[i].g);
            myOut.writeFloat(array[i].b);
            myOut.writeFloat(array[i].a);
        }
    }

    private void writeVec2fArray(Vector2f[] array) throws IOException {
        myOut.writeByte(BinaryFormatConstants.DATA_V2FARRAY);
        myOut.writeInt(array.length);
        for (int i=0;i<array.length;i++){
            myOut.writeFloat(array[i].x);
            myOut.writeFloat(array[i].y);
        }
    }

    private void writeVec3fArray(Vector3f[] array) throws IOException {
        myOut.writeByte(BinaryFormatConstants.DATA_V3FARRAY);
        myOut.writeInt(array.length);
        for (int i=0;i<array.length;i++){
            if (array[i]==null){
                myOut.writeFloat(Float.NaN);
                myOut.writeFloat(Float.NaN);
                myOut.writeFloat(Float.NaN);
            } else{
                myOut.writeFloat(array[i].x);
                myOut.writeFloat(array[i].y);
                myOut.writeFloat(array[i].z);
            }
        }
    }

    /**
     * Looks at a spatial and puts its attributes into the HashMap
     * @param spatial The spatial to look at
     * @param atts The HashMap to put the attributes into
     */
    private void putSpatialAtts(Spatial spatial, HashMap atts) {
        atts.put("name",spatial.getName());
        if (!spatial.getLocalScale().equals(DEFAULT_SCALE))
            atts.put("scale",spatial.getLocalScale());
        if (!spatial.getLocalRotation().equals(DEFAULT_ROTATION))
            atts.put("rotation",spatial.getLocalRotation());
        if (!spatial.getLocalTranslation().equals(DEFAULT_TRANSLATION))
            atts.put("translation",spatial.getLocalTranslation());
    }

    /**
     * Writes the end of the file by writting the end of scene, then the END_FILE flag
     * @throws IOException
     */
    private void writeClosing() throws IOException {
        writeEndTag("scene");
        if (DEBUG) System.out.println("Writting file close");
        myOut.writeByte(BinaryFormatConstants.END_FILE);
    }

    /**
     * Writes the be BEGIN_FILE tag to a file, then the scene tag
     * @throws IOException
     */
    private void writeHeader() throws IOException {
        if (DEBUG) System.out.println("Writting file begin");
        myOut.writeLong(BinaryFormatConstants.BEGIN_FILE);
        writeTag("scene",null);
    }
}