/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.loader.ImageUtils;
import com.openbravo.data.loader.SerializableRead;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.pos.ticket.CategoryInfo;
import java.io.Serializable;

/**
 *
 * @author egil
 */
public class AuxiliarInfo implements SerializableRead, IKeyed {
    private String Product2;
    private double Qty;
    
    
    public AuxiliarInfo(String pProduct2,double pQty){
        Product2 = pProduct2;
        Qty = pQty;
    }
    
    @Override
    public Object getKey() {
        return Qty;
    }

    public String getProduct2() {
        return Product2;
    }

    public void setProduct2(String Product2) {
        this.Product2 = Product2;
    }

    public double getQty() {
        return Qty;
    }

    public void setQty(double Qty) {
        this.Qty = Qty;
    }

    @Override
    public void readValues(DataRead dr) throws BasicException {
        Product2 = dr.getString(1);
        Qty = dr.getDouble(2);
    }
    
    public static SerializerRead getSerializerRead() {
        return new SerializerRead() {@Override
 public Object readValues(DataRead dr) throws BasicException {
            return new AuxiliarInfo(dr.getString(1), dr.getDouble(2));
        }};
    }
    
    
}
