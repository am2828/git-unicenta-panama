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

package com.openbravo.pos.ticket;

import javax.swing.*;
import java.awt.*;

import com.openbravo.pos.util.ThumbNailBuilder;
import com.openbravo.format.Formats;
import com.openbravo.pos.inventory.StockCurrentInfo;

/**
 *
 * @author adrianromero
 *
 */
public class WarehouseStockRenderer extends DefaultListCellRenderer {
                
    ThumbNailBuilder tnbprod;

    /** Creates a new instance of ProductRenderer */
    public WarehouseStockRenderer() {   
        tnbprod = new ThumbNailBuilder(64, 32, "com/openbravo/images/package.png");
    }

    //@Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
        StockCurrentInfo stock= (StockCurrentInfo) value;
        if (stock != null) {
          setText("<html><font color=\"blue\">" + stock.getLocation_name() + "</font> <br> " + Formats.DOUBLE.formatValue(new Double(stock.getUnits())));
          //setText("<html>Prueba");
            //Image img = tnbprod.getThumbNail(tnbprod.getImage());
            //setIcon(img == null ? null :new ImageIcon(img));
        }
        return this;
    }      
    /**
     *
     * @return
     */
    public String hola(){
        return "Hola";    
    }


}

