package com.openbravo.pos.erp.possync;
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

import java.util.HashMap;

import org.apache.activemq.transport.stomp.StompConnection; 
import org.apache.activemq.transport.stomp.Stomp.Headers.Subscribe; 
import org.apache.activemq.transport.stomp.StompFrame;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppLocal;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MQClient { 
	private static final String PERSISTENT = "persistent";
	private static String URLhost ;
	private static int port;
	private static String queuePath = "/queue/test";
        private static String user;
        private static String password;
        	
	public static boolean sendMessage(String messageText) { 
            try {
                String url = "tcp://"+URLhost+":"+port;
                ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
                Connection connection = connectionFactory.createConnection();
                connection.start();
                Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
                session.createTopic(queuePath);
                Queue queue = session.createQueue(queuePath);
                MessageProducer producer = session.createProducer(queue);
                producer.setDeliveryMode(DeliveryMode.PERSISTENT);
                TextMessage message = session.createTextMessage();
                message.setText(messageText);
                producer.send(message);
                session.commit();
                connection.close();
                
                /* StompConnection connection = new StompConnection(); 
                HashMap<String, String> header = new HashMap<String, String>();
                header.put(PERSISTENT, "true");
                connection.open(URLhost, port); 
                connection.connect(user, password); 
                connection.begin("MQClient");
                connection.send("/queue/"+queuePath, messageText, "MQClient", header);
                connection.send("/topic/"+queuePath, messageText, "MQClient", header);
                connection.commit("MQClient");
                connection.disconnect();  */
            } catch (JMSException ex) {
                Logger.getLogger(MQClient.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            } 
            return true;
	} 

        public static String receiveMessage() {
            try {
                StompConnection connection = new StompConnection(); 
                connection.open(URLhost, port); 
                connection.connect(user, password); 
                connection.subscribe(queuePath, Subscribe.AckModeValues.CLIENT); 
                connection.begin("MQClient");
                Thread.sleep(1000);//below not a good NO DATA test .. worked by making thread sleep a while
              //	      if (connection.getStompSocket().getInputStream().available() > 1) 
              {
                    StompFrame message = connection.receive(); 
    //		      connection.ack(message, "MQClient"); -- red1 -- POS do not ACK for other POS to read. 
                    //ACK is done on server side.
                        connection.commit("MQClient");
                        connection.disconnect();
                        return message.getBody();
                }
    //	      else
    //	    	  return "";

                } catch (Exception e) { 
                }
                return "";
	}
	  
	  public static String receiveMessage(String URL, int no, String queue) {
		  URLhost = URL;
		  port = no;
		  queuePath = queue;
		  return receiveMessage();
	  }
	  
	  public static void setURLpath(String url) {
		  URLhost = url;
	  }
	  public static void setQueuePath(String path){
		  queuePath = path;
	  }
	  public static void setPort(int no){
		  port = no;
	  }

        public static String getUser() {
            return user;
        }

        public static void setUser(String userName) {
            user = userName;
        }

        public static String getPassword() {
            return password;
        }

        public static void setPassword(String Password) {
           password = Password;
        }
        public static void setParams(String host, int no, String path){
            setURLpath(host);
            setPort(no);
            setQueuePath(path);
            setPassword("");
            setUser("");
        
        }
          
	public static void setParams(String host, int no, String path, String user, String password) {
		setURLpath(host);
		setPort(no);
		setQueuePath(path);
                setUser(user);
                setPassword(password);
 	}
	  
} 


