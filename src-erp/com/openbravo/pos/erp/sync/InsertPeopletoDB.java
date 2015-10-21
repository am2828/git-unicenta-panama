/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.erp.sync;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.erp.customers.User;
import com.openbravo.pos.forms.AppLocal;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.compiere.model.MUser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author sergio
 */
public class InsertPeopletoDB {

    private final DataLogicIntegration dlintegration;


    
    public InsertPeopletoDB(DataLogicIntegration dlintegration) {

        this.dlintegration = dlintegration;

    }
    
    public boolean Import(String usersXML){
        try {
            User[] users = importQueue(usersXML);
        
             if (users == null){
                throw new BasicException(AppLocal.getIntString("message.returnnull"));
             } 
             
              if (users.length > 0 ) {
                    
                    //dlintegration.syncCustomersBefore();
                    //Add RIF to field Taxid
                    for (User user : users) {    
                        System.out.println("Registrando Usuario: "+user.getName());
                        dlintegration.syncPeople(user);
                    }
                    return true;
                }
                
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

    private User[] importQueue(String usersXML) throws ParserConfigurationException, SAXException, IOException {
        if (usersXML.equals("")) return new User[0];
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
	Document doc = db.parse(new ByteArrayInputStream(usersXML.getBytes(Charset.defaultCharset())));
        Element docEle = doc.getDocumentElement();
        NodeList records = docEle.getElementsByTagName("detail");
        User[] user = new User[records.getLength()];
	
    	for(int i = 0 ; i < records.getLength();i++) {
    	    //if (!records.item(i).getFirstChild().getTextContent().equals("AD_Client_ID"))
    	    //	continue;
    		user[i] = new User();
	    	NodeList details = records.item(i).getChildNodes();
	    	for(int j = 0 ; j < details.getLength();j++) {
	    		Node n = details.item(j);
	    		String column = n.getNodeName();
	    		
	    		if (column.equals(MUser.COLUMNNAME_Name)) 
                            user[i].setName(URLDecoder.decode(n.getTextContent(),"UTF-8"));
	    		else if (column.equals(MUser.COLUMNNAME_AD_User_ID)) 
                            user[i].setId(n.getTextContent());
                        else if (column.equals("IsActive")){
                            if (n.getTextContent().compareTo("Y")==0)
                                user[i].setVisible(Boolean.TRUE);
                            else
                                user[i].setVisible(Boolean.FALSE);
                        }
	    		
	    	}
    	}
    	return user;
	} 
}
