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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * @author Sergio Oropeza - Double Click Sistemas - Venezuela - soropeza@dcs.net.ve, info@dcs.net.ve
 */
public class SyncERPQueue extends Thread implements MessageListener{
    private final String userName;
    private final String password;
    private final String url;
    private final SimpleDateFormat sdf;
    private int numbOnQueue;
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;
    private int count; // varible que cuenta las veces que se llama al metodo onMessage
    private final String queueMQ;
    private final DataLogicSystem dlSystem;
    private final DataLogicIntegration dli;
    private final DataLogicSales dlsales;
    private final InsertProductstoDB productsQueueSync;
    private final InsertCustomerstoDB customersQueueSyn;
    private final InsertPeopletoDB peopleQueueSyn;
    private final InsertCreditmemotoDB creditmemoQueueSyn;
    private final UpdateOrdersToResend resendordersQueueSyn;
    private final String lastUpdate;
    
    /**
     *
     * @param userName
     * @param password
     * @param url
     * @param lastUpdate
     * @param queueMQ
     * @param app
     */
    public SyncERPQueue(String userName, String password, String url, String lastUpdate, String queueMQ,JRootApp app){
        sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        this.userName = userName;
        this.password = password;
        this.url = url;
        this.lastUpdate = lastUpdate;
        count = 0;
        this.queueMQ = queueMQ;
        dlSystem = (DataLogicSystem) app.getBean("com.openbravo.pos.forms.DataLogicSystem");
        dli = (DataLogicIntegration) app.getBean("com.openbravo.pos.erp.sync.DataLogicIntegration");
        dlsales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        productsQueueSync = new InsertProductstoDB(dlSystem, dli, dlsales, app.getInventoryLocation());
        customersQueueSyn =  new InsertCustomerstoDB(dli);
        peopleQueueSyn =  new InsertPeopletoDB(dli);
        creditmemoQueueSyn =  new InsertCreditmemotoDB(dli);
        resendordersQueueSyn =  new UpdateOrdersToResend(dli);
    }
    @Override
    public void run(){
        // Envía las ordenes a la cola de manera automática
        boolean receive = true; // Define si el mensaje fué enviado a la cola
        Double stopLoop; // Tiempo que se detendrá el cilclo 
        int c =0;
        boolean keep=true;
        while (keep) { 
            try {
                stopLoop = receive==true? (1.0):0.25; 
                //si es la primera vez que entra no se detiene el ciclo
                if(c != 0){
                    sleep(converter(stopLoop));
                }

                System.out.println("-Sync with Queue "+queueMQ);
                if(connection!=null || session!=null || consumer!=null)
                    closedConnection(session, consumer, connection);

                ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(userName, password, url);
                connection = connectionFactory.createConnection();           
                session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
                Destination destination = session.createQueue(queueMQ);
                consumer = session.createConsumer(destination);
                consumer.setMessageListener(this);
                connection.start();    
            } catch (JMSException ex) {
                Logger.getLogger(SyncERPQueue.class.getName()).log(Level.SEVERE, null, ex);
                receive = false;
                closedConnection(session,consumer,connection);
            }catch (InterruptedException ex) {
                   Logger.getLogger(SyncERPQueue.class.getName()).log(Level.SEVERE, null, ex);
                receive = false;
                closedConnection(session,consumer,connection);
            }
            c++;
        }
    }
    
    public long converter(Double min){
        long millis = (long) (min*60*1000);
        return millis;
    }

    @Override
    public void onMessage(Message msg) {
        try{
            validarMensaje(msg);
            msg.acknowledge();
        } catch (JMSException ex) {
            Logger.getLogger(SyncERPQueue.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            closedConnection(session,consumer,connection);
        }
    }
    
    // validamos si el mensaje recibido ya no halla sido registrado en el sistema
    public void validarMensaje(Message message){  
        try {
            Calendar messageTime = Calendar.getInstance();
            messageTime.setTimeInMillis(message.getJMSTimestamp());
            System.out.println("Registering "+queueMQ);
            TextMessage tMsg = (TextMessage)message;
            importToDataBase(tMsg.getText(),messageTime);
        } catch (JMSException ex) {
            Logger.getLogger(SyncERPQueue.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public void importToDataBase(String xml, Calendar messageTime){
        if(queueMQ.toUpperCase().contains("PRODUCT")){
            if(productsQueueSync.ImportProducts(xml)) // si se registra al menos un productos se guarda la fecha de los Registros
                writeLastDateDB(messageTime);
        }else if(queueMQ.toUpperCase().contains("CUSTOMER")){
            if(customersQueueSyn.ImportCustomers(xml))
                writeLastDateDB(messageTime);           
        }else if(queueMQ.toUpperCase().contains("USERS")){
               if(peopleQueueSyn.Import(xml))
                   writeLastDateDB(messageTime);           
        }else if(queueMQ.toUpperCase().contains("CREDITMEMO")){
            if(creditmemoQueueSyn.Import(xml))
                writeLastDateDB(messageTime);           
        }else if(queueMQ.toUpperCase().contains("RESENDORDERS")){
            if(resendordersQueueSyn.Import(xml))
                writeLastDateDB(messageTime);           
        }
    }

  //leemos la fecha en que se realizó la ultima actualización
   public Calendar readLastDateDB(){
        try {
             if(lastUpdate != null && !"".equals(lastUpdate)){
                Calendar date = Calendar.getInstance();
                date.setTime(sdf.parse(lastUpdate));
            return date ;
            } else{
                Calendar dateini = Calendar.getInstance();
                dateini.setTime(sdf.parse("02/08/2000 12:17:06.795"));
              return dateini;
            }
        } catch (ParseException ex) {
            Logger.getLogger(SyncERPQueue.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;    
    }
    
   // Escribe en la base de datos la fecha en que se registraron los datos
    public void writeLastDateDB(Calendar calendar) {
            dlSystem.setResource("queue.lastUpdate"+queueMQ, 0, sdf.format(calendar.getTime()).getBytes());
    }

    //Cuenta el número de mensajes en la cola
    private int numberMsgOnQueue(Session session) {
        try {
            Queue queue  = new Queue() {
            @Override
            public String getQueueName() throws JMSException {
                return queueMQ;
            }
        };
            QueueBrowser inQBrowser = session.createBrowser(queue);  
            Enumeration messagesOnQ = inQBrowser.getEnumeration();  
            int numOnQueue = 0;
            while (messagesOnQ.hasMoreElements()) {  
                messagesOnQ.nextElement();
                numOnQueue++;  
            }  
            inQBrowser.close();
            return numOnQueue;
        } catch (JMSException ex) {
            Logger.getLogger(SyncERPQueue.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    //Cierra la conexión con la cola para dejar disponible la cola a otra Cliente.
    private void closedConnection(Session session, MessageConsumer consumer, Connection connection) {
        try {
            System.out.println(" Closing Conecction Queue " +queueMQ);
            if(consumer!=null)
                consumer.close();
            
            if(session!=null)
                session.close();
            
            if(connection!=null)
                connection.close();
            
        } catch (JMSException ex) {
            Logger.getLogger(SyncERPQueue.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
