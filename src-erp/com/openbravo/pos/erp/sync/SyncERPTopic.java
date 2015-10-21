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


import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.JRootApp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;


/**
 *
 * @author Sergio Oropeza - Double Click Sistemas - Venezuela - soropeza@dcs.net.ve, info@dcs.net.ve
 */
public class SyncERPTopic extends Thread implements MessageListener{
    private  String userName;
    private  String password;
    private  String url;
    private  String topicMQ;
    private  DataLogicSystem dlSystem;
    private  DataLogicIntegration dli;
    private  DataLogicSales dlsales;
    private InsertProductstoDB productsQueueSync;
    private InsertCustomerstoDB customersQueueSyn;
    private InsertPeopletoDB peopleQueueSyn;
  
    @Override
    public void run(){
        connection();
    }
    public SyncERPTopic(String userName, String password, String url, String lastUpdate, String topicMQ,JRootApp app){
        this.userName = userName;
        this.password = password;
        this.url = url;
        this.topicMQ = topicMQ;
        dlSystem = (DataLogicSystem) app.getBean("com.openbravo.pos.forms.DataLogicSystem");
        dli = (DataLogicIntegration) app.getBean("com.openbravo.pos.erp.possync.DataLogicIntegration");
        dlsales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        productsQueueSync = new InsertProductstoDB(dlSystem, dli, dlsales, app.getInventoryLocation());
        customersQueueSyn =  new InsertCustomerstoDB(dli);
        peopleQueueSyn =  new InsertPeopletoDB(dli);

    }
    public void connection(){
        try {
            System.out.println("Listening Topic: "+ topicMQ);
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(userName, password, url);
            Connection connection = connectionFactory.createConnection();
            Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(topicMQ);
            connection.start();         
            MessageConsumer consumer = session.createConsumer(destination);
            consumer.setMessageListener(this);
        } catch (JMSException ex) {
            Logger.getLogger(SyncERPTopic.class.getName()).log(Level.SEVERE, null, ex);
        }      
    }
    
     @Override
    public void onMessage(Message message) {
        try {
           Calendar timeStampMessage = Calendar.getInstance();
           timeStampMessage.setTimeInMillis(message.getJMSTimestamp());
           System.out.println("New "+topicMQ+"(s): ");            
           TextMessage tMsg = (TextMessage)message;
           importToDataBase(tMsg.getText(),timeStampMessage);         
        } catch (JMSException ex) {
            Logger.getLogger(SyncERPTopic.class.getName()).log(Level.SEVERE, null, ex);
        }     
    }
    // Importa en la base de datos los datos recibidos por en el Servidor
    public void importToDataBase(String xml,Calendar timeStampMessage){  
          if(topicMQ.toUpperCase().contains("PRODUCT"))
              if(productsQueueSync.ImportProducts(xml))
                writeLastUpdateDB(timeStampMessage); 
          else if(topicMQ.toUpperCase().contains("CUSTOMER")){
               if(customersQueueSyn.ImportCustomers(xml))
                   writeLastUpdateDB(timeStampMessage);             
          }
          else if(topicMQ.toUpperCase().contains("USER")){
               if(customersQueueSyn.ImportCustomers(xml))
                   writeLastUpdateDB(timeStampMessage);             
          }
    }
    
     // Escribe en la base de datos la fecha en que se registraron los datos
    public void writeLastUpdateDB(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
         dlSystem.setResource("queue.lastUpdate"+topicMQ, 0, sdf.format(calendar.getTime()).getBytes());
    }
    
}
