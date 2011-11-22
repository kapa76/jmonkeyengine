/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.assetpack.project.wizards;

import com.jme3.gde.assetpack.AssetPackLoader;
import com.jme3.gde.assetpack.project.AssetPackProject;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileChooserBuilder;

public final class ImportVisualPanel2 extends JPanel {

    List<FileDescription> list = new LinkedList<FileDescription>();
    WizardDescriptor wiz;
    SelectExistingAsset existingSelector;

    /** Creates new form ImportVisualPanel2 */
    public ImportVisualPanel2() {
        initComponents();
    }

    @Override
    public String getName() {
        return "Asset Files";
    }

    public void applySettings(WizardDescriptor wiz) {
        wiz.putProperty("filelist", list);
    }

    public void loadSettings(WizardDescriptor wiz) {
        this.wiz = wiz;
        updateList();
        existingSelector = new SelectExistingAsset(new JFrame(), true, ((AssetPackProject) wiz.getProperty("project")).getAssetsFolder(), ((AssetPackProject) wiz.getProperty("project")), list);
    }

    public void updateList() {
        jPanel1.removeAll();
        for (Iterator<FileDescription> it = list.iterator(); it.hasNext();) {
            FileDescription fileDescription = it.next();
            SingleAssetFilePanel panel = new SingleAssetFilePanel(fileDescription);
            jPanel1.add(panel);
        }
        updateModelSelection();
        revalidate();
        repaint();
    }

    public void updateModelSelection() {
        LinkedList<String> strings = new LinkedList<String>();
        for (Iterator<FileDescription> it = list.iterator(); it.hasNext();) {
            FileDescription fileDescription = it.next();
            if ("material".equals(fileDescription.getType())) {
                strings.add(fileDescription.getPath() + fileDescription.getName());
            }
        }
        for (int i = 0; i < jPanel1.getComponents().length; i++) {
            SingleAssetFilePanel component = (SingleAssetFilePanel) jPanel1.getComponents()[i];
            component.setModelList(strings);
        }
    }

    private void selectFile() {
        FileChooserBuilder builder = new FileChooserBuilder(this.getClass());
        builder.setFilesOnly(true);
        builder.setTitle("Select Asset File");
        File[] file = builder.showMultiOpenDialog();
        if (file != null) {
            for (int i = 0; i < file.length; i++) {
                File file1 = file[i];
                FileDescription description = AssetPackLoader.getFileDescription(file1);
                description.setPath(pathString());
                list.add(description);
            }
            updateList();
        }
    }

    private String pathString() {
        String string = "";
        String type = (String) wiz.getProperty("type");
        if (type.equals("model")) {
            string += "/Models/";
        } else if (type.equals("scene")) {
            string += "/Scenes/";
        } else if (type.equals("texture")) {
            string += "/Textures/";
        } else if (type.equals("sound")) {
            string += "/Sounds/";
        } else if (type.equals("shader")) {
            string += "/Shaders/";
        } else if (type.equals("other")) {
            string += "/Misc/";
        }
        String category = ((String) wiz.getProperty("categories")).split(",")[0].trim();
        string += category + "/";
        string += (String) wiz.getProperty("name") + "/";
        return string;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ImportVisualPanel2.class, "ImportVisualPanel2.jLabel1.text")); // NOI18N

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.PAGE_AXIS));
        jScrollPane1.setViewportView(jPanel1);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(ImportVisualPanel2.class, "ImportVisualPanel2.jButton1.text")); // NOI18N
        jButton1.setToolTipText(org.openide.util.NbBundle.getMessage(ImportVisualPanel2.class, "ImportVisualPanel2.jButton1.toolTipText")); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(ImportVisualPanel2.class, "ImportVisualPanel2.jButton2.text")); // NOI18N
        jButton2.setToolTipText(org.openide.util.NbBundle.getMessage(ImportVisualPanel2.class, "ImportVisualPanel2.jButton2.toolTipText")); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                    .addComponent(jLabel1))
                .addContainerGap())
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        selectFile();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        existingSelector.setVisible(true);
        updateList();
    }//GEN-LAST:event_jButton2ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables
}