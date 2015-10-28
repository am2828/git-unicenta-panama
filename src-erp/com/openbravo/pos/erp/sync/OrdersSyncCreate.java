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

package com.openbravo.pos.erp.sync;

import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryCache;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.JRootApp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import javax.swing.JOptionPane;

/**
 *
 * @author adrian
 */
public class OrdersSyncCreate extends BeanFactoryCache {
    
    public Object constructBean(AppView app) throws BeanFactoryException {
        
        
        DataLogicSystem dlSystem = (DataLogicSystem) app.getBean("com.openbravo.pos.forms.DataLogicSystem");
        DataLogicIntegration dli = (DataLogicIntegration) app.getBean("com.openbravo.pos.erp.sync.DataLogicIntegration");
        Properties activeMQProp = dlSystem.getResourceAsProperties("openbravo.properties");
        
        String userName = activeMQProp.getProperty("user");
        String password = activeMQProp.getProperty("password");
        String host = activeMQProp.getProperty("queue-host");
        int port = Integer.parseInt(activeMQProp.getProperty("queue-port"));
        String topicOrder = activeMQProp.getProperty("orders-queue");
        
        Double minuteSyncOrders;
        try {
            minuteSyncOrders = Double.parseDouble(activeMQProp.getProperty("syncOrders.minutes"));
        } catch (Exception e) {
            minuteSyncOrders= 1.0;
        }
        String OrdersSync=JOptionPane.showInputDialog("Introduzca una fecha en el formato dd/mm/aaaa",new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTimeInMillis()));
        if (OrdersSync == null || OrdersSync.length()<=0){
            host="-";
            
        }
        String url = "tcp://"+host+":"+port+"?jms.optimizeAcknowledge=true&keepAlive=true&jms.prefetchPolicy.queuePrefetch=1";
        OrdersSync bean= new OrdersSync(topicOrder,(JRootApp)app, minuteSyncOrders,userName, password, url,OrdersSync);
        return bean;
        
    }
    
    
}
