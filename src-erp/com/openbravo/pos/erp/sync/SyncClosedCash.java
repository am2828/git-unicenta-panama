//    Openbravo POS is a point of sales application designed for touch screens.
//    http://www.openbravo.com/product/pos
//    Copyright (c) 2007 openTrends Solucions i Sistemes, S.L
//    Modified by Openbravo SL on March 22, 2007
//    These modifications are copyright Openbravo SL
//    Author/s: A. Romero 
//    You may contact Openbravo SL at: http://www.openbravo.com
//
//		Contributor: Redhuan D. Oon - ActiveMQ XML string creation for MClient.sendmessage()
//		Please refer to notes at http://red1.org/adempiere/viewtopic.php?f=29&t=1356
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

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;

import com.openbravo.pos.erp.possync.DataLogicIntegration;
import com.openbravo.pos.forms.AppLocal;

import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.JRootApp;

import com.openbravo.pos.erp.externalsales.ClosedCashInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import static java.lang.Thread.sleep;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import org.apache.activemq.ActiveMQConnectionFactory;


/**
 *
 * @author sergio
 */
public class SyncClosedCash extends Thread{
    private final Double  minutesSyncClosedCash;
    private final JRootApp app;
    private final DataLogicSystem dlsystem;
    private final DataLogicIntegration dlintegration;
    private final String queue;
    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private final String adClientId;
    private final String AD_Org_ID;
    private final String poslocator;
    private Connection connection;
    private Session session;
    
    /**Constructor of SyncClosedCash class
     * 
     * @param queue
     * @param rootApp
     * @param minuteSyncClosedCash 
     */
    public SyncClosedCash(String queue, JRootApp rootApp, Double minuteSyncClosedCash) {
         app = rootApp;
         this.minutesSyncClosedCash = minuteSyncClosedCash;   // indica el intervalo de tiempo que se enviarán los cierres     
         dlsystem = (DataLogicSystem) app.getBean("com.openbravo.pos.forms.DataLogicSystem");
         dlintegration = (DataLogicIntegration) app.getBean("com.openbravo.pos.erp.possync.DataLogicIntegration");
         this.queue =queue; 
         Properties activeMQProp = dlsystem.getResourceAsProperties("openbravo.properties");
         this.host = activeMQProp.getProperty("queue-host");
         this.port = Integer.parseInt(activeMQProp.getProperty("queue-port")); 
         this.user = activeMQProp.getProperty("user");
         this.password = activeMQProp.getProperty("password");
         poslocator = activeMQProp.getProperty("pos");
         this.adClientId = activeMQProp.getProperty("id");
         this.AD_Org_ID = activeMQProp.getProperty("org");
    }
    public SyncClosedCash(String queue, JRootApp rootApp, Double minuteSyncClosedCash,String userName, String password, String url) {
         app = rootApp;
         this.minutesSyncClosedCash = minuteSyncClosedCash;   // indica el intervalo de tiempo que se enviarán los cierres     
         dlsystem = (DataLogicSystem) app.getBean("com.openbravo.pos.forms.DataLogicSystem");
         dlintegration = (DataLogicIntegration) app.getBean("com.openbravo.pos.erp.possync.DataLogicIntegration");
         this.queue =queue; 
         Properties activeMQProp = dlsystem.getResourceAsProperties("openbravo.properties");
         this.host = url;
         this.port = Integer.parseInt(activeMQProp.getProperty("queue-port")); 
         this.user = userName;
         this.password = password;
         poslocator = activeMQProp.getProperty("pos");
         this.adClientId = activeMQProp.getProperty("id");
         this.AD_Org_ID = activeMQProp.getProperty("org");
    }

    @Override
    public void run(){
        boolean sent = true; // Define si el mensaje fué enviado a la cola
        Double stopLoop; // Tiempo que se detendrá el cilclo 
        int c =0;
        while (true) { 
             try {
               stopLoop = sent==true? minutesSyncClosedCash:0.25; 
                //si es la primera vez que entra no se detiene el ciclo
               if(c !=0)
                  sleep(converter(stopLoop));
               System.out.println(exportToERP().getMessageMsg());
               sent = true;
            }catch (BasicException ex) {
                Logger.getLogger(SyncClosedCash.class.getName()).log(Level.SEVERE, null, ex);
                 System.out.println("Cierre no enviado se intentará enviar nuevamente dentro 15 segundos");
                 sent = false;
            }catch (InterruptedException ex) {
                  Logger.getLogger(SyncClosedCash.class.getName()).log(Level.SEVERE, null, ex);
             } catch (JMSException ex) {
                Logger.getLogger(SyncClosedCash.class.getName()).log(Level.SEVERE, null, ex);
            }
             c++;
        }
    }
    /**Convert minutes in miliseconds
     * 
     * @param min
     * @return 
     */
    public long converter(Double min){
        long millis = (long) (min*60*1000);
        return millis;
    }

    /**Create the message and export the information via ActiveMQConnectionFactory
     * 
     * @return
     * @throws BasicException
     * @throws JMSException 
     */
    public MessageInf exportToERP() throws BasicException, JMSException {        
        // Get tickets
        try{
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, host);
            // Create a Connection
            connection = connectionFactory.createConnection();
            connection.start();
            // Create a Session
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            // Create the destination (Topic or Queue)
            Destination destination = session.createQueue(queue);
            MessageProducer producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            List<ClosedCashInfo> closedCashSync = dlintegration.getClosedCashSync();    
            if (closedCashSync.size()>0) {
                return new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.sendingclosedcash"));
            } else {
                dlintegration.setClosedCashInProcess();
                List<ClosedCashInfo> closedCashList = dlintegration.getClosedCash();
                if (closedCashList.isEmpty()) {
                    return new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.zeroclosedcash"));
                } else {
                    TextMessage message=session.createTextMessage(transformTickets(closedCashList));
                    producer.send(message);
                    dlintegration.execClosedCashUpdate();
                    return new MessageInf(MessageInf.SGN_SUCCESS, AppLocal.getIntString("message.syncclosedcashok"), AppLocal.getIntString("message.syncclosedcashinfo")+ closedCashList.size());
                }
            }
        }catch (JMSException ex) {
            Logger.getLogger(SyncClosedCash.class.getName()).log(Level.SEVERE, null, ex);
            dlintegration.execTicketUpdateError();
            return new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.syncclosedcashexception"));
        }finally{
            if(session!=null){
                session.close();
                connection.close();
            }
        }

    }  
        
    private String transformTickets(List<ClosedCashInfo> closedCashList) {
        //red1 - START XML inception for ActiveMQ
        try { 
            StringWriter res = new StringWriter(); 
            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(res); 
            writer.writeStartDocument();
            writer.writeStartElement("entityDetail");
            writer.writeStartElement("type");
            writer.writeCharacters("closedCash");
            writer.writeEndElement();   //type
            System.out.println(AppLocal.getIntString("message.syncclosedcashinfo")+": "+ closedCashList.size());
            for (int i = 0; i < closedCashList.size(); i++) {
                if (null != this) { 
	            ClosedCashInfo closedCash = closedCashList.get(i);
                    for (int j = 0; j < closedCashList.size(); j++){      	
	            	ClosedCashInfo line = closedCashList.get(j);
                        writer.writeStartElement("detail"); 
                        writer.writeStartElement("DocTypeName"); 
                        writer.writeCharacters("Closed Cash");
                        writer.writeEndElement();   
                        writer.writeStartElement("AD_Client_ID"); 
                        writer.writeCharacters(adClientId);
                        writer.writeEndElement();
                        writer.writeStartElement("AD_Org_ID"); 
                        writer.writeCharacters(AD_Org_ID);
                        writer.writeEndElement();
                        writer.writeStartElement("POSLocatorName");
                        writer.writeCharacters(poslocator);
                        writer.writeEndElement(); 	  		      	
                        writer.writeStartElement("DocumentNo");
                        writer.writeCharacters(line.getId()); 
                        writer.writeEndElement(); 
                        writer.writeStartElement("HostSequence");
                        writer.writeCharacters(Integer.toString(line.getHostSeguence())); 
                        writer.writeEndElement(); 
                        Calendar datestart=Calendar.getInstance();
                        datestart.setTimeInMillis(line.getDateStart().getTime());
                        writer.writeStartElement("DateStart");
                        writer.writeCharacters(Integer.toString(datestart.get(Calendar.YEAR))+"/"+
                                                    String.format("%02d",datestart.get(Calendar.MONTH)+1)+"/"+
                                                    String.format("%02d",datestart.get(Calendar.DAY_OF_MONTH)));
                        writer.writeEndElement();
                        writer.writeStartElement("MachineName");
                        writer.writeCharacters(getHostName());
                        writer.writeEndElement();
                        writer.writeStartElement("UserName");
                        writer.writeCharacters(line.getUserID());
                        writer.writeEndElement();
                        writer.writeStartElement("Difference");
                        writer.writeCharacters(Double.toString(line.getDifference()));
                        writer.writeEndElement();
                        writer.writeEndElement();   //detail
                    }
	          } 
       		}
            
            writer.writeEndElement();//entityDetail
            writer.writeEndDocument();
            return res.toString();
	      
    	 } catch (Exception ex) { 
            ex.printStackTrace(); 
            return "ERROR creating XML"; 
        }         
    }
    
     private String getHostName() {
       Properties m_propsconfig =  new Properties();    
       File file =  new File(new File(System.getProperty("user.home")), AppLocal.APP_ID + ".properties");
        try {
            InputStream in = new FileInputStream(file);
            if (in != null) {
                m_propsconfig.load(in);
                in.close();
            }
        } catch (IOException e){
           
        }
        return m_propsconfig.getProperty("machine.hostname");
    }
}
