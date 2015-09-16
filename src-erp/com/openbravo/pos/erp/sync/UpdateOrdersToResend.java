/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.erp.sync;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.erp.possync.DataLogicIntegration;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author sergio
 */
public class UpdateOrdersToResend {

    private final DataLogicIntegration dlintegration;


    
    public UpdateOrdersToResend(DataLogicIntegration dlintegration) {

        this.dlintegration = dlintegration;

    }
    
    public boolean Import(String resendordersXML){
        try {
            
        if (resendordersXML.equals("")) return false;
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
	Document doc = db.parse(new ByteArrayInputStream(resendordersXML.getBytes(Charset.defaultCharset())));
        Element docEle = doc.getDocumentElement();
        NodeList records = docEle.getElementsByTagName("detail");
        //String[] creditmemo = new String[records.getLength()];
        String AD_Org_ID=null;
        String ticketid=null;
        //recorremos la cola para actualizar a los clientes que vienen con notas de credito 
	
    	for(int i = 0 ; i < records.getLength();i++) {
    	    //if (!records.item(i).getFirstChild().getTextContent().equals("AD_Client_ID"))
    	    //	continue;
            
	    	NodeList details = records.item(i).getChildNodes();
	    	for(int j = 0 ; j < details.getLength();j++) {
	    		Node n = details.item(j);
	    		String column = n.getNodeName();
	    		
	    		if (column.equals("AD_OrgID")) 
                            AD_Org_ID=n.getTextContent();
	    		else if (column.equals("ticketid")) 
                            ticketid=n.getTextContent();	
	    	}
                //search for id form bpvalue
                dlintegration.syncResendOrders(Integer.parseInt(ticketid));
    	}
    	return true;
	
    } catch (ParserConfigurationException ex) {
            Logger.getLogger(UpdateOrdersToResend.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(UpdateOrdersToResend.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UpdateOrdersToResend.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BasicException ex) {
            Logger.getLogger(UpdateOrdersToResend.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}
