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
import com.openbravo.pos.erp.externalsales.Order;
import com.openbravo.pos.erp.externalsales.OrderIdentifier;
import com.openbravo.pos.erp.externalsales.OrderLine;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.JRootApp;
import com.openbravo.pos.payment.PaymentInfo;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
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
import javax.jms.JMSException;
import javax.jms.Session;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.compiere.model.I_I_Order;

/**
 *
 * @author sergio
 */
public class SyncERPOrders extends Thread{
    private  Double  minutesSyncOrders;
    private JRootApp app;
    private final DataLogicSystem dlsystem;
    private final DataLogicIntegration dlintegration;
    private final String queueOrders;
    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private final String adClientId;
    private final String poslocator;
    private final String AD_Org_ID;
    private final String country;
    private final String city;
    private Connection connection;
    private Session session;
    
    
    public SyncERPOrders(String queueOrders, JRootApp rootApp, Double minuteSyncOrders) {
         app = rootApp;
         this.minutesSyncOrders = minuteSyncOrders;   // indica el intervalo de tiempo que se enviarán las ordenes     
         dlsystem = (DataLogicSystem) app.getBean("com.openbravo.pos.forms.DataLogicSystem");
         dlintegration = (DataLogicIntegration) app.getBean("com.openbravo.pos.erp.sync.DataLogicIntegration");
         this.queueOrders =queueOrders;
         Properties activeMQProp = dlsystem.getResourceAsProperties("openbravo.properties");
         this.host = activeMQProp.getProperty("queue-host");
         this.port = Integer.parseInt(activeMQProp.getProperty("queue-port")); 
         this.user = activeMQProp.getProperty("user");
         this.password = activeMQProp.getProperty("password");
         poslocator = activeMQProp.getProperty("pos");
         this.adClientId = activeMQProp.getProperty("id");
         this.AD_Org_ID = activeMQProp.getProperty("org");
         this.country= activeMQProp.getProperty("country");
         this.city= activeMQProp.getProperty("city");
    
    }
    public SyncERPOrders(String queueOrders, JRootApp rootApp, Double minuteSyncOrders,String userName, String password, String url) {
         app = rootApp;
         this.minutesSyncOrders = minuteSyncOrders;   // indica el intervalo de tiempo que se enviarán las ordenes     
         dlsystem = (DataLogicSystem) app.getBean("com.openbravo.pos.forms.DataLogicSystem");
         dlintegration = (DataLogicIntegration) app.getBean("com.openbravo.pos.erp.sync.DataLogicIntegration");
         this.queueOrders =queueOrders;
         Properties activeMQProp = dlsystem.getResourceAsProperties("openbravo.properties");
         this.host = url;
         this.port = Integer.parseInt(activeMQProp.getProperty("queue-port")); 
         this.user = userName;
         this.password = password;
         poslocator = activeMQProp.getProperty("pos");
         this.adClientId = activeMQProp.getProperty("id");
         this.AD_Org_ID = activeMQProp.getProperty("org");
         this.country= activeMQProp.getProperty("country");
         this.city= activeMQProp.getProperty("city");
    }
    
    
    
    @Override
    public void run(){
        // Envía las ordenes a la cola de manera automática
        boolean sent = true; // Define si el mensaje fué enviado a la cola
        Double stopLoop; // Tiempo que se detendrá el cilclo 
        int c =0;
    
        while (true) { 
             try {
                 /* si el mensaje fue enviado las ordenes de enviaran al tiempo definido por el usuario;
                  si no se intentará a enviar dentro de un minuto */
               stopLoop = sent==true? minutesSyncOrders:0.25; 
                //si es la primera vez que entra no se detiene el ciclo
               if(c !=0){
                  sleep(converter(stopLoop));
               }
               System.out.println(exportToERP().getMessageMsg());
               sent = true;
            }catch (BasicException ex) {
                Logger.getLogger(SyncERPOrders.class.getName()).log(Level.SEVERE, null, ex);
                sent = false;
            }catch (InterruptedException ex) {
                Logger.getLogger(SyncERPOrders.class.getName()).log(Level.SEVERE, null, ex);            
            } catch (JMSException ex) {
                Logger.getLogger(SyncERPOrders.class.getName()).log(Level.SEVERE, null, ex);
            }
            c++;
        }
        
    }
    //convierte de minuto a milisegundos
    public long converter(Double min){
        long millis = (long) (min*60*1000);
        return millis;
    }

    // Metodos basados de la sincronización de Red1
    public MessageInf exportToERP() throws BasicException, JMSException {  
        try{
            
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, host);
            // Create a Connection
            connection = connectionFactory.createConnection();
            connection.start();
            // Create a Session
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            // Create the destination (Topic or Queue)
            Destination destination = session.createQueue(queueOrders);
            MessageProducer producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            // Get tickets for sync
            List<TicketInfo> ticketlistSync = dlintegration.getTicketsSync();
            int i=0;
            if(ticketlistSync.size()>0){
                return new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.sendingorders"));
            }
            //if there is not tickets in process update the list of tickets not sync
            dlintegration.setTicketsInProcess();
            
            List<TicketInfo> ticketlist = dlintegration.getTickets();
            for (TicketInfo ticket : ticketlist) {
                ticket.setLines(dlintegration.getTicketLines(ticket.getId()));
                ticket.setPayments(dlintegration.getTicketPayments(ticket.getId()));
            }
            if (ticketlist.isEmpty()) {    
                return new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.zeroorders"));
            } else {
                TextMessage message=session.createTextMessage(transformTickets(ticketlist));
                producer.send(message);
                dlintegration.execTicketUpdate();
                return new MessageInf(MessageInf.SGN_SUCCESS, AppLocal.getIntString("message.syncordersok"), AppLocal.getIntString("message.syncordersinfo")+ticketlist.size());
                
            }
        }catch (JMSException ex) {
            Logger.getLogger(SyncERPOrders.class.getName()).log(Level.SEVERE, null, ex);
            dlintegration.execTicketUpdateError();
            return new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.syncordersexception"));
        }finally{
            if(session!=null){
                session.close();
                connection.close();
            }
        }

    }  
    
    private String transformTickets(List<TicketInfo> ticketlist) {
        //red1 - START XML inception for ActiveMQ
        try { 
            StringWriter res = new StringWriter(); 
            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(res); 
            writer.writeStartDocument();
            writer.writeStartElement("entityDetail");
                writer.writeStartElement("type");
                writer.writeCharacters(I_I_Order.Table_Name);
                writer.writeEndElement();   //type
            
                // Transforming tickets to orders
                Order[] orders = new Order[ticketlist.size()];
                System.out.println("Cantidad de ticket para enviar: "+ ticketlist.size());
                for (int i = 0; i < ticketlist.size(); i++) {
                    if (null != this) { 
                        TicketInfo ticket = ticketlist.get(i);
                        orders[i] = new Order();
                        OrderIdentifier orderid = new OrderIdentifier();
                        orderid.setDocumentNo(Integer.toString(ticket.getTicketId()));
                        orders[i].setOrderId(orderid);
                        Calendar datenew = Calendar.getInstance();
                        datenew.setTime(ticket.getDate());
                        orderid.setDateNew(datenew);
                        orders[i].setState(800175);         
                        if (ticket.getCustomerId() != null) 
                            ticket.setCustomer(dlintegration.getTicketCustomer(ticket.getId()));
                        OrderLine[] orderLine = new OrderLine[ticket.getLines().size()];
                        for (int j = 0; j < ticket.getLines().size(); j++){
                            //red1 - convert to XML for ActiveMQ
                            TicketLineInfo line = ticket.getLines().get(j);
                            orderLine[j] = new OrderLine();
                            orderLine[j].setOrderLineId(String.valueOf(line.getTicketLine()));// or simply "j"
                            //writer.writeEndElement(); 
                            writer.writeStartElement("detail"); 
                            writer.writeStartElement(I_I_Order.COLUMNNAME_DocTypeName); 
                            writer.writeCharacters("POS Order");
                            writer.writeEndElement();   
                            writer.writeStartElement(I_I_Order.COLUMNNAME_AD_Client_ID); 
                            writer.writeCharacters(adClientId);
                            writer.writeEndElement();
                            writer.writeStartElement(I_I_Order.COLUMNNAME_AD_Org_ID); 
                            writer.writeCharacters(AD_Org_ID);
                            writer.writeEndElement();
                            writer.writeStartElement("POSLocatorName");
                            writer.writeCharacters(poslocator);
                            writer.writeEndElement(); 	  		      	
                            writer.writeStartElement(I_I_Order.COLUMNNAME_DocumentNo);
                            writer.writeCharacters(Integer.toString(ticket.getTicketId())); 
                            writer.writeEndElement(); 
                            writer.writeStartElement(I_I_Order.COLUMNNAME_DateOrdered);
                            writer.writeCharacters(new java.sql.Timestamp(datenew.getTime().getTime()).toString());
                            writer.writeEndElement();
                            writer.writeStartElement("MachineName");
                            writer.writeCharacters(getHostName());
                            writer.writeEndElement();
                            writer.writeStartElement("UserName");
                            writer.writeCharacters(ticket.getUser().getName());
                            writer.writeEndElement();
                            writer.writeStartElement("BPartner");
                            if (ticket.getCustomerId() != null) {
                                writer.writeStartElement(I_I_Order.COLUMNNAME_BPartnerValue);
                                writer.writeCharacters(ticket.getCustomer().getTaxid());
                                writer.writeEndElement();
                                writer.writeStartElement("BPartnerName");
                                writer.writeCharacters(ticket.getCustomer().getName());
                                writer.writeEndElement();
                                writer.writeStartElement("BPartnerAddress");
                                writer.writeCharacters(ticket.getCustomer().getAddress());
                                writer.writeEndElement();
                                writer.writeStartElement("BPartnerCity");
                                writer.writeCharacters(ticket.getCustomer().getCity());
                                writer.writeEndElement();
                            }	
                            writer.writeEndElement(); 
                            if (line.getProductID() == null) {
                                orderLine[j].setProductId("0");
                            } else {
                                orderLine[j].setProductId(line.getProductID());
                                writer.writeStartElement(I_I_Order.COLUMNNAME_ProductValue);
                                writer.writeCharacters(line.getProductID());
                                writer.writeEndElement();     
                            }
                            //red1 - convert to XML
                            orderLine[j].setUnits(line.getMultiply());
                            orderLine[j].setPrice(line.getPrice());
                            orderLine[j].setTaxId(line.getTaxInfo().getId());  	                
                            //red1 - convert to XML these 3 items
                            writer.writeStartElement(I_I_Order.COLUMNNAME_QtyOrdered);
                            writer.writeCharacters(Double.toString(line.getMultiply()));
                            writer.writeEndElement();         
                            writer.writeStartElement(I_I_Order.COLUMNNAME_PriceActual);
                            writer.writeCharacters(Double.toString(line.getPrice()));
                            writer.writeEndElement();
                            writer.writeStartElement(I_I_Order.COLUMNNAME_C_Tax_ID);
                            writer.writeCharacters(line.getTaxInfo().getId());
                            writer.writeEndElement();
                            writer.writeStartElement(I_I_Order.COLUMNNAME_TaxAmt);
                            writer.writeCharacters(Double.toString(line.getTax()));
                            writer.writeEndElement();
                            //TODO get User Code as SalesRep_ID (see ImportQueue2AD)
                            String paymentType = "POS Order";
                            Double creditnoteAmount = 0.0;
                            writer.writeStartElement("paymentType");
                            if (ticket.getTotal() >= 0) {
                                List<PaymentInfo> payments = ticket.getPayments();
                                for (PaymentInfo payment : payments) {
                                    if(payment.getName().equals("debt")) {
                                        paymentType = payment.getName();
                                        creditnoteAmount = payment.getTotal();
                                        break;
                                    }
                                }
                            }
                            else {
                                PaymentInfo payments = ticket.getPayments().get(0);
                                paymentType = payments.getName();
                                creditnoteAmount = payments.getTotal();
                            }
                            writer.writeCharacters(paymentType);
                            writer.writeEndElement();
                            writer.writeStartElement("creditnoteAmount");
                            writer.writeCharacters(String.valueOf(creditnoteAmount));
                            writer.writeEndElement();

                            writer.writeStartElement("paymentAmount");
                            writer.writeCharacters(String.valueOf(ticket.getTotal()));
                            writer.writeEndElement();
                            //send fiscal information commented for standard
                            /*
                            writer.writeStartElement("fiscalprint_serial");
                            writer.writeCharacters(ticket.getFiscalprint_serial());
                            writer.writeEndElement();
                            writer.writeStartElement("fiscal_invoicenumber");
                            writer.writeCharacters(ticket.getFiscal_invoicenumber());
                            writer.writeEndElement();
                            writer.writeStartElement("fiscal_zreport");
                            writer.writeCharacters(ticket.getFiscal_zreport());
                            writer.writeEndElement();
                                    */
                            writer.writeStartElement("numberoflines");
                            writer.writeCharacters(Integer.toString(ticket.getLines().size()));
                            writer.writeEndElement();
                            writer.writeStartElement("line");
                            writer.writeCharacters(Integer.toString(line.getTicketLine()));
                            writer.writeEndElement();
                            
                            writer.writeStartElement("C_Country_ID");
                            writer.writeCharacters(country);
                            writer.writeEndElement();
                            writer.writeStartElement("C_City_ID");
                            writer.writeCharacters(city);
                            writer.writeEndElement();
                            writer.writeEndElement();   //detail  
                        }
                        orders[i].setLines(orderLine);
                      } 
                    }
  		    //red1
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
