/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.ImageUtils;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.pos.ticket.ProductInfoExt;

/**
 *
 * @author egil
 */
public class StockCurrentInfo {
    protected String location;
    protected String product;
    protected String attributesetinstance_id;
    double units;
    protected String location_name;
    public StockCurrentInfo(){
        location=null;
        product=null;
        attributesetinstance_id=null;
        units=0.0;
        location_name=null;
    }
    
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getAttributesetinstance_id() {
        return attributesetinstance_id;
    }

    public void setAttributesetinstance_id(String attributesetinstance_id) {
        this.attributesetinstance_id = attributesetinstance_id;
    }

    public double getUnits() {
        return units;
    }

    public void setUnits(double units) {
        this.units = units;
    }

    public String getLocation_name() {
        return location_name;
    }

    public void setLocation_name(String location_name) {
        this.location_name = location_name;
    }
    
    
    public static SerializerRead getSerializerRead() {
        return new SerializerRead() { public Object readValues(DataRead dr) throws BasicException {
            StockCurrentInfo stock = new StockCurrentInfo();
            stock.location = dr.getString(1);
            stock.product = dr.getString(2);
            stock.attributesetinstance_id = dr.getString(3);
            stock.units = dr.getDouble(4);
            stock.location_name = dr.getString(5);
            return stock;
        }};
    }
}
