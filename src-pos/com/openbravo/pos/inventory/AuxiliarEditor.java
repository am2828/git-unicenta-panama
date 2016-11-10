//    uniCenta oPOS  - Touch Friendly Point Of Sale
//    Copyright (c) 2009-2014 uniCenta & previous Openbravo POS works
//    http://www.unicenta.com
//
//    This file is part of uniCenta oPOS
//
//    uniCenta oPOS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//   uniCenta oPOS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with uniCenta oPOS.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.panels.JProductFinder;
import com.openbravo.pos.ticket.ProductInfoExt;
import java.awt.Component;
import java.awt.Toolkit;
import java.util.UUID;

/**
 *
 * @author jaroslawwozniak
 */
public class AuxiliarEditor extends javax.swing.JPanel implements EditorRecord {

    private DataLogicSales m_dlSales;
    
    private Object id;
    private Object product;
    private Object product2;
    private Object name;
    private Object recipe;
    private Object qty;
    
    private Object insertproduct;

    /** Creates new form AuxiliarEditor
     * @param app
     * @param dirty */
    public AuxiliarEditor(AppView app, DirtyManager dirty) {

        m_dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");

        initComponents();
     
        m_jProduct.getDocument().addDocumentListener(dirty);
        m_jQty.getDocument().addDocumentListener(dirty);
        jRecipe.addActionListener(dirty);
        
    }
    
    /**
     *
     * @param prod
     */
    public void setInsertProduct(ProductInfoExt prod) {
        
        if (prod == null) {
            insertproduct = null;
        } else {
            insertproduct = prod.getID();
        }
    }

    /**
     *
     */
    @Override
    public void refresh() {
    }

    /**
     *
     */
    @Override
    public void writeValueEOF() {
        
        id = null;
        product = null;
        product2 = null;
        name = null;
        m_jReference.setText(null);
        m_jBarcode.setText(null);
        m_jProduct.setText(null);

        m_jReference.setEnabled(false);
        m_jBarcode.setEnabled(false);
        m_jProduct.setEnabled(false);
        m_jEnter1.setEnabled(false);
        m_jEnter2.setEnabled(false);
        m_jSearch.setEnabled(false);
    
        recipe =null;
        qty = null;
        jRecipe.setEnabled(false);
        m_jQty.setEnabled(false);
        
    }

    /**
     *
     */
    @Override
    public void writeValueInsert() {
        
        id = UUID.randomUUID().toString();
        product = insertproduct;
        product2 = null;
        name = null;
        m_jReference.setText(null);
        m_jBarcode.setText(null);
        m_jProduct.setText(null);

        m_jReference.setEnabled(true);
        m_jBarcode.setEnabled(true);
        m_jProduct.setEnabled(true);
        m_jEnter1.setEnabled(true);
        m_jEnter2.setEnabled(true);
        m_jSearch.setEnabled(true);
        
        jRecipe.setSelected(false);
        jRecipe.setEnabled(true);
        m_jQty.setText(null);
        m_jQty.setEnabled(true);
    
    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueEdit(Object value) {
        Object[] obj = (Object[]) value;
        
        id = obj[0];
        product = obj[1];
        product2 = obj[2];
        name = obj[7];
        m_jReference.setText(Formats.STRING.formatValue(obj[5]));
        m_jBarcode.setText(Formats.STRING.formatValue(obj[6]));
        m_jProduct.setText(Formats.STRING.formatValue(obj[5]) + " - " + Formats.STRING.formatValue(obj[7]));        

        m_jReference.setEnabled(true);
        m_jBarcode.setEnabled(true);
        m_jProduct.setEnabled(true);
        m_jEnter1.setEnabled(true);
        m_jEnter2.setEnabled(true);
        m_jSearch.setEnabled(true);
        
        jRecipe.setSelected((Boolean)obj[3]);
        jRecipe.setEnabled(true);
        m_jQty.setText(Formats.DOUBLE.formatValue(obj[4]));
        m_jQty.setEnabled(true);
        
    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueDelete(Object value) {
        Object[] obj = (Object[]) value;
        
        id = obj[0];
        product = obj[1];
        product2 = obj[2];
        name = obj[7];
        m_jReference.setText(Formats.STRING.formatValue(obj[5]));
        m_jBarcode.setText(Formats.STRING.formatValue(obj[6]));
        m_jProduct.setText(Formats.STRING.formatValue(obj[5]) + " - " + Formats.STRING.formatValue(obj[7]));        

        
        m_jReference.setEnabled(false);
        m_jBarcode.setEnabled(false);
        m_jProduct.setEnabled(false);
        m_jEnter1.setEnabled(false);
        m_jEnter2.setEnabled(false);
        m_jSearch.setEnabled(false);       
        m_jQty.setText(Formats.DOUBLE.formatValue(obj[4]));
        m_jQty.setEnabled(false);
        jRecipe.setSelected((Boolean)obj[3]);
        jRecipe.setEnabled(false);
    }

    /**
     *
     * @return
     * @throws BasicException
     */
    @Override
    public Object createValue() throws BasicException {
        
        return new Object[] {
            id, 
            product, 
            product2,
            jRecipe.isSelected(),
            Formats.DOUBLE.parseValue(m_jQty.getText()),
            m_jReference.getText(),
            m_jBarcode.getText(),
            name
            
        };
    }

    /**
     *
     * @return
     */
    @Override
    public Component getComponent() {
        return this;
    }

    private void assignProduct(ProductInfoExt prod) {

        if (m_jSearch.isEnabled()) {
            if (prod == null) {
                product2 = null;
                m_jReference.setText(null);
                m_jBarcode.setText(null);
                m_jProduct.setText(null);
                name = null;
                recipe = null;
                qty = null;
            } else {
                product2 = prod.getID();
                m_jReference.setText(prod.getReference());
                m_jBarcode.setText(prod.getCode());
                m_jProduct.setText(prod.getReference() + " - " + prod.getName());
                name = prod.getName();
                recipe = false;
                qty = null;
            }
        }

    }

    private void assignProductByCode() {
        try {
            ProductInfoExt prod = m_dlSales.getProductInfoByCode(m_jBarcode.getText());
            assignProduct(prod);
            if (prod == null) {
                Toolkit.getDefaultToolkit().beep();       
            }
        } catch (BasicException eData) {
            assignProduct(null);
            MessageInf msg = new MessageInf(eData);
            msg.show(this);
        }
    }


    private void assignProductByReference() {
        try {
            ProductInfoExt prod = m_dlSales.getProductInfoByReference(m_jReference.getText());
            assignProduct(prod);
            if (prod == null) {
                Toolkit.getDefaultToolkit().beep();       
            }
        } catch (BasicException eData) {
            assignProduct(null);
            MessageInf msg = new MessageInf(eData);
            msg.show(this);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        m_jReference = new javax.swing.JTextField();
        m_jEnter1 = new javax.swing.JButton();
        m_jEnter2 = new javax.swing.JButton();
        m_jSearch = new javax.swing.JButton();
        m_jProduct = new javax.swing.JTextField();
        m_jBarcode = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        m_jQty = new javax.swing.JTextField();
        jRecipe = new javax.swing.JCheckBox();

        setPreferredSize(new java.awt.Dimension(700, 100));
        setLayout(null);

        jLabel3.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.prodref")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(70, 25));
        add(jLabel3);
        jLabel3.setBounds(10, 11, 70, 25);

        m_jReference.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jReference.setPreferredSize(new java.awt.Dimension(150, 25));
        m_jReference.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jReferenceActionPerformed(evt);
            }
        });
        add(m_jReference);
        m_jReference.setBounds(90, 11, 150, 25);

        m_jEnter1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        m_jEnter1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jEnter1ActionPerformed(evt);
            }
        });
        add(m_jEnter1);
        m_jEnter1.setBounds(250, 11, 36, 36);

        m_jEnter2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/barcode.png"))); // NOI18N
        m_jEnter2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jEnter2ActionPerformed(evt);
            }
        });
        add(m_jEnter2);
        m_jEnter2.setBounds(557, 11, 34, 34);

        m_jSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/search24.png"))); // NOI18N
        m_jSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jSearchActionPerformed(evt);
            }
        });
        add(m_jSearch);
        m_jSearch.setBounds(10, 50, 36, 36);

        m_jProduct.setEditable(false);
        m_jProduct.setPreferredSize(new java.awt.Dimension(200, 25));
        m_jProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jProductActionPerformed(evt);
            }
        });
        add(m_jProduct);
        m_jProduct.setBounds(90, 50, 217, 25);

        m_jBarcode.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jBarcode.setPreferredSize(new java.awt.Dimension(150, 25));
        m_jBarcode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jBarcodeActionPerformed(evt);
            }
        });
        add(m_jBarcode);
        m_jBarcode.setBounds(380, 10, 150, 25);

        jLabel4.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel4.setText(AppLocal.getIntString("label.qty")); // NOI18N
        jLabel4.setPreferredSize(new java.awt.Dimension(70, 25));
        add(jLabel4);
        jLabel4.setBounds(320, 50, 40, 25);

        jLabel5.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel5.setText(AppLocal.getIntString("label.prodbarcode")); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(70, 25));
        add(jLabel5);
        jLabel5.setBounds(317, 11, 70, 25);

        m_jQty.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jQty.setPreferredSize(new java.awt.Dimension(150, 25));
        m_jQty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jQtyActionPerformed(evt);
            }
        });
        add(m_jQty);
        m_jQty.setBounds(360, 50, 80, 25);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jRecipe.setText(bundle.getString("label.recipe")); // NOI18N
        add(jRecipe);
        jRecipe.setBounds(490, 50, 130, 25);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jSearchActionPerformed
        
        assignProduct(JProductFinder.showMessage(this, m_dlSales, JProductFinder.PRODUCT_AUXILIAR));
        
}//GEN-LAST:event_m_jSearchActionPerformed

    private void m_jReferenceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jReferenceActionPerformed
        this.assignProductByReference();
    }//GEN-LAST:event_m_jReferenceActionPerformed

    private void m_jEnter2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jEnter2ActionPerformed
        this.assignProductByCode();
    }//GEN-LAST:event_m_jEnter2ActionPerformed

    private void m_jEnter1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jEnter1ActionPerformed
        this.assignProductByReference();
    }//GEN-LAST:event_m_jEnter1ActionPerformed

    private void m_jBarcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jBarcodeActionPerformed
        this.assignProductByCode();
    }//GEN-LAST:event_m_jBarcodeActionPerformed

    private void m_jProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jProductActionPerformed

    }//GEN-LAST:event_m_jProductActionPerformed

    private void m_jQtyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jQtyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_m_jQtyActionPerformed

  


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JCheckBox jRecipe;
    private javax.swing.JTextField m_jBarcode;
    private javax.swing.JButton m_jEnter1;
    private javax.swing.JButton m_jEnter2;
    private javax.swing.JTextField m_jProduct;
    private javax.swing.JTextField m_jQty;
    private javax.swing.JTextField m_jReference;
    private javax.swing.JButton m_jSearch;
    // End of variables declaration//GEN-END:variables

}
