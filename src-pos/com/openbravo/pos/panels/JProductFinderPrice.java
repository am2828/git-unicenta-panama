//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2007-2009 Openbravo, S.L.
//    http://www.openbravo.com/product/pos
//
//    This file is part of Openbravo POS.
//
//    Openbravo POS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Openbravo POS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Openbravo POS.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.panels;

import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.ProductRenderer;
import com.openbravo.pos.ticket.WarehouseStockRenderer;
import javax.swing.*;
import java.awt.*;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.basic.BasicException;
import com.openbravo.data.user.ListProvider;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.inventory.LocationInfo;
import com.openbravo.pos.inventory.StockCurrentInfo;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sergio Oropeza - Double Click Sistemas - Venezuela - soropeza@dcs.net.ve, info@dcs.net.ve
 */
public class JProductFinderPrice extends javax.swing.JDialog {

    private ProductInfoExt m_ReturnProduct;
    private ListProvider lpr;
    private DataLogicSales dlSales;
    
    public final static int PRODUCT_ALL = 0;
    public final static int PRODUCT_NORMAL = 1;
    public final static int PRODUCT_AUXILIAR = 2;
   
    
    /** Creates new form JProductFinder */
    private JProductFinderPrice(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
    }
    /** Creates new form JProductFinder */
    private JProductFinderPrice(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
    }    
    
       public void addKeyButton(){
      KeyStroke F3 = KeyStroke.getKeyStroke( KeyEvent.VK_F3,0 );
      KeyStroke Enter = KeyStroke.getKeyStroke( KeyEvent.VK_ENTER,0 );
      KeyStroke Esc = KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE,0 );

      btnSearch.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(F3, "actionF3");
      btnSearch.getActionMap().put("actionF3", Accion_F3()); 
      
      btnSearch.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(Enter, "actionEnter");
      btnSearch.getActionMap().put("actionEnter", Accion_Enter()); 
      
      jcmdCancel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(Esc, "actionCancel");
      jcmdCancel.getActionMap().put("actionCancel", Accion_Esc()); 
   
      
    }
public AbstractAction Accion_Esc(){
    return new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) { 
            jcmdCancelActionPerformed(null);
        }
    };
}
    
public AbstractAction Accion_Enter(){
    return new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) { 
            btnSearchActionPerformed(null);
        }
    };
}

public AbstractAction Accion_F3(){
    return new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) { 
            if(jcmdOK.isEnabled())
                 jcmdOKActionPerformed(null);
        }
    };
}

    
 private ProductInfoExt init(DataLogicSales dataLogicSales) {
        
        initComponents();
        
        jScrollPane1.getVerticalScrollBar().setPreferredSize(new Dimension(35, 35));
        m_jtxtFilterProduct.addEditorKeys(m_jKeys);
        m_jtxtFilterProduct.activate();
        jListProducts.setCellRenderer(new ProductRenderer());
        dlSales = dataLogicSales;
        jListWarehouseStock.setCellRenderer(new WarehouseStockRenderer());

        m_ReturnProduct = null;
        addKeyButton();
        setVisible(true);

        return m_ReturnProduct;
    }
    

    private static Window getWindow(Component parent) {
        if (parent == null) {
            return new JFrame();
        } else if (parent instanceof Frame || parent instanceof Dialog) {
            return (Window)parent;
        } else {
            return getWindow(parent.getParent());
        }
    }    
    
    public static ProductInfoExt showMessage(Component parent, DataLogicSales dlSales) {
        return showMessage(parent, dlSales, PRODUCT_ALL);
    }

    public static ProductInfoExt showMessage(Component parent, DataLogicSales dlSales, int productsType) {

        Window window = getWindow(parent);

        JProductFinderPrice myMsg;
        if (window instanceof Frame) {
            myMsg = new JProductFinderPrice((Frame) window, true);
        } else {
            myMsg = new JProductFinderPrice((Dialog) window, true);
        }
        return myMsg.init(dlSales);
        
    }

    
    
    private static class MyListData extends javax.swing.AbstractListModel {
        
        private java.util.List m_data;
        
        public MyListData(java.util.List data) {
            m_data = data;
        }
        
        public Object getElementAt(int index) {
            return m_data.get(index);
        }
        
        public int getSize() {
            return m_data.size();
        } 
    }   
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel4 = new javax.swing.JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListWarehouseStock = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListProducts = new javax.swing.JList();
        m_jProductSelect = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        m_jtxtFilterProduct = new com.openbravo.editor.JEditorString();
        jPanel3 = new javax.swing.JPanel();
        btnSearch = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jcmdOK = new javax.swing.JButton();
        jcmdCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(AppLocal.getIntString("form.productslist")); // NOI18N
        setMinimumSize(new java.awt.Dimension(518, 280));

        jPanel4.setLayout(new java.awt.BorderLayout());

        m_jKeys.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jKeysActionPerformed(evt);
            }
        });
        jPanel4.add(m_jKeys, java.awt.BorderLayout.NORTH);

        jListWarehouseStock.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(jListWarehouseStock);

        jPanel4.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel4, java.awt.BorderLayout.LINE_END);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel5.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel5.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jListProducts.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListProducts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListProductsMouseClicked(evt);
            }
        });
        jListProducts.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListProductsValueChanged(evt);
            }
        });
        jListProducts.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jListProductsKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(jListProducts);

        jPanel5.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        m_jProductSelect.setLayout(new java.awt.BorderLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jLabel1.setText(bundle.getString("label.product")); // NOI18N
        m_jProductSelect.add(jLabel1, java.awt.BorderLayout.LINE_START);
        m_jProductSelect.add(m_jtxtFilterProduct, java.awt.BorderLayout.CENTER);

        btnSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/launch.png"))); // NOI18N
        btnSearch.setText(AppLocal.getIntString("button.executefilter")); // NOI18N
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });
        jPanel3.add(btnSearch);

        m_jProductSelect.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        jPanel5.add(m_jProductSelect, java.awt.BorderLayout.PAGE_START);

        jPanel2.add(jPanel5, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jcmdOK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/button_ok.png"))); // NOI18N
        jcmdOK.setText("OK (F3)");
        jcmdOK.setEnabled(false);
        jcmdOK.setMargin(new java.awt.Insets(8, 16, 8, 16));
        jcmdOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdOKActionPerformed(evt);
            }
        });
        jPanel1.add(jcmdOK);

        jcmdCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/button_cancel.png"))); // NOI18N
        jcmdCancel.setText(AppLocal.getIntString("Button.Cancel")); // NOI18N
        jcmdCancel.setMargin(new java.awt.Insets(8, 16, 8, 16));
        jcmdCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdCancelActionPerformed(evt);
            }
        });
        jPanel1.add(jcmdCancel);

        jPanel2.add(jPanel1, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        setSize(new java.awt.Dimension(915, 491));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jListProductsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListProductsValueChanged

        jcmdOK.setEnabled(jListProducts.getSelectedValue() != null);

    }//GEN-LAST:event_jListProductsValueChanged

    private List<ProductInfoExt> searchProductByNameOrCodeBar(){
        List<ProductInfoExt> product = null;
        try {
         
           return  product= dlSales.searchProductByNameOrCodeBar(m_jtxtFilterProduct.getText());
                   
        } catch (BasicException ex) {
            Logger.getLogger(JProductFinderPrice.class.getName()).log(Level.SEVERE, null, ex);
        }
        return product;
    }
    private void jListProductsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListProductsMouseClicked
        if (evt.getClickCount() == 1) {
            searchStock((ProductInfoExt) jListProducts.getSelectedValue());
        }
        if (evt.getClickCount() == 2) {
            m_ReturnProduct = (ProductInfoExt) jListProducts.getSelectedValue();
            dispose();
        }

    }//GEN-LAST:event_jListProductsMouseClicked

    private void jcmdCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdCancelActionPerformed

        dispose();

    }//GEN-LAST:event_jcmdCancelActionPerformed

    private void jcmdOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdOKActionPerformed

        m_ReturnProduct = (ProductInfoExt) jListProducts.getSelectedValue();
        dispose();

    }//GEN-LAST:event_jcmdOKActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
            

        if(m_jtxtFilterProduct.getText()!= null){
                 buscarProducto();
            }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void m_jKeysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jKeysActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_m_jKeysActionPerformed

    private void jListProductsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jListProductsKeyReleased
        // TODO add your handling code here:
        if (evt.getKeyCode() == 13)
            ;
    }//GEN-LAST:event_jListProductsKeyReleased
    
    private void buscarProducto(){
    
            jListProducts.setModel(new MyListData(searchProductByNameOrCodeBar()));
            if (jListProducts.getModel().getSize() > 0) {
                jListProducts.setSelectedIndex(0);
                
            }
            m_jtxtFilterProduct.activate();
    }
    private void searchStock(ProductInfoExt product){
    
            jListWarehouseStock.setModel(new MyListData(searchStockOfProduct(product.getID())));
            
            
    }
    private List searchStockOfProduct(String product) {
        List<StockCurrentInfo> stock = null;
        try {
            stock= dlSales.searchStockOfProduct(product);
            return  stock;
           
                   
        } catch (BasicException ex) {
            Logger.getLogger(JProductFinderPrice.class.getName()).log(Level.SEVERE, null, ex);
        }
        return stock;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSearch;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jListProducts;
    private javax.swing.JList jListWarehouseStock;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton jcmdCancel;
    private javax.swing.JButton jcmdOK;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private javax.swing.JPanel m_jProductSelect;
    private com.openbravo.editor.JEditorString m_jtxtFilterProduct;
    // End of variables declaration//GEN-END:variables
    
}
