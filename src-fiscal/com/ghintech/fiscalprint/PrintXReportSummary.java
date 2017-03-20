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

package com.ghintech.fiscalprint;

import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryCache;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.DataLogicSystem;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * @author Eduardo Gil
 */
public class PrintXReportSummary extends BeanFactoryCache {
    
    private DataLogicFiscal dlFiscal;
    @Override
    public Object constructBean(AppView app) throws BeanFactoryException {
        DataLogicSystem dlSystem = (DataLogicSystem) app.getBean("com.openbravo.pos.forms.DataLogicSystem");
        dlFiscal = (DataLogicFiscal) app.getBean("com.ghintech.fiscalprint.DataLogicFiscal");
        String startDate=JOptionPane.showInputDialog("Introduzca una fecha en el formato dd/MM/aaaa",new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTimeInMillis()));
        
        DateFormat helper = new SimpleDateFormat("dd/MM/yyyy");
        Date datetmp = null;
        try {
            datetmp = helper.parse(startDate);
        } catch (ParseException ex) {
            Logger.getLogger(PrintXReportSummary.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Valor de fecha incorrecto", "POS", JOptionPane.PLAIN_MESSAGE);
            
        }
        PrintReport bean = new PrintReport("I0X\n"+createSummary(new SimpleDateFormat("yyyy-MM-dd").format(datetmp)),dlSystem);
        
        return bean;
    }

    private String createSummary(String startDate) {
        
        String rep ;
        rep="800\n"+dlFiscal.salesByProduct(startDate);
        rep+=dlFiscal.salesByCategory(startDate);
        rep+=dlFiscal.salesByTaxes(startDate);
        rep+=dlFiscal.salesByHour(startDate);
        rep+=dlFiscal.totalTips(startDate);
        rep+="810\n";
        
        return rep;
    }

    
   
}
