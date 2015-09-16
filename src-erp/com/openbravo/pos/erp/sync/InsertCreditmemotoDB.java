/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.erp.sync;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.erp.customers.Customer;

import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.erp.possync.DataLogicIntegration;
import com.openbravo.pos.customers.CustomerInfoExt;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.compiere.model.MBPartner;
import org.compiere.model.MUser;
import org.postgresql.core.Encoding;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author sergio
 */
public class InsertCreditmemotoDB {

    private final DataLogicIntegration dlintegration;


    
    public InsertCreditmemotoDB(DataLogicIntegration dlintegration) {

        this.dlintegration = dlintegration;

    }
    
    public boolean Import(String creditmemoXML){
        try {
            
        if (creditmemoXML.equals("")) return false;
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
	Document doc = db.parse(new ByteArrayInputStream(creditmemoXML.getBytes(Charset.defaultCharset())));
        Element docEle = doc.getDocumentElement();
        NodeList records = docEle.getElementsByTagName("detail");
        //String[] creditmemo = new String[records.getLength()];
        String BPValue=null;
        String OpenAmount=null;
        //recorremos la cola para actualizar a los clientes que vienen con notas de credito 
	
    	for(int i = 0 ; i < records.getLength();i++) {
    	    //if (!records.item(i).getFirstChild().getTextContent().equals("AD_Client_ID"))
    	    //	continue;
            
	    	NodeList details = records.item(i).getChildNodes();
	    	for(int j = 0 ; j < details.getLength();j++) {
	    		Node n = details.item(j);
	    		String column = n.getNodeName();
	    		
	    		if (column.equals("BPValue")) 
                            BPValue=URLDecoder.decode(n.getTextContent(),"UTF-8");
	    		else if (column.equals("OpenAmount")) 
                            OpenAmount=n.getTextContent();	
	    	}
                //search for id form bpvalue
                dlintegration.syncCreditmemo(BPValue,(Double.parseDouble(OpenAmount)*-1));
    	}
    	return true;
	
    } catch (ParserConfigurationException ex) {
            Logger.getLogger(InsertPeopletoDB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(InsertPeopletoDB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(InsertPeopletoDB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BasicException ex) {
            Logger.getLogger(InsertPeopletoDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}
