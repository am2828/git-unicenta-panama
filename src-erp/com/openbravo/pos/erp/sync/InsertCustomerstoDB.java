/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.erp.sync;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.erp.customers.Customer;
import com.openbravo.pos.erp.customers.Location;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.compiere.model.MBPartner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author sergio
 */
public class InsertCustomerstoDB {

    private final DataLogicIntegration dlintegration;


    
    public InsertCustomerstoDB(DataLogicIntegration dlintegration) {

        this.dlintegration = dlintegration;

    }
    
    public boolean ImportCustomers(String customersXML){
        try {
            Customer[] customers = importQueue2Customers(customersXML);
        
             if (customers == null){
                throw new BasicException(AppLocal.getIntString("message.returnnull"));
             } 
             
              if (customers.length > 0 ) {
                    
                    //dlintegration. syncCustomersBefore();
                                    //Add RIF to field Taxid
                    for (Customer customer : customers) {    
                        System.out.println("Registrando Cliente: "+customer.getName());
                        CustomerInfoExt cinfo = new CustomerInfoExt(customer.getId());
                        cinfo.setTaxid(fixRif(customer.getSearchKey()));
                        cinfo.setSearchkey(customer.getSearchKey());
                        cinfo.setName(customer.getName());          
                        cinfo.setNotes(customer.getDescription());
                        Location loc[]=new Location[1];
                        loc=customer.getLocations();
                        cinfo.setAddress(loc[0].getAddress1());
                        cinfo.setVisible(customer.getVisible());
                        cinfo.setMaxdebt(customer.getMaxdebt());
                        // TODO: Finish the integration of all fields.
                        dlintegration.syncCustomer(cinfo);
                    }
                    return true;
                }
                
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(InsertCustomerstoDB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(InsertCustomerstoDB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(InsertCustomerstoDB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BasicException ex) {
            Logger.getLogger(InsertCustomerstoDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    
    }
    private String fixRif(String rif){
        String RIF = rif;
            if(rif != null){          
               RIF =  rif.replace("-","" );
            }
        return RIF.toUpperCase();
    }
    
    private Customer[] importQueue2Customers(String customersXML) throws ParserConfigurationException, SAXException, IOException {
        if (customersXML.equals("")) return new Customer[0];
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
	Document doc = db.parse(new ByteArrayInputStream(customersXML.getBytes()));
        Element docEle = doc.getDocumentElement();
        NodeList records = docEle.getElementsByTagName("detail");
        Customer[] customer = new Customer[records.getLength()];
	
    	for(int i = 0 ; i < records.getLength();i++) {
    	    if (!records.item(i).getFirstChild().getTextContent().equals(MBPartner.Table_Name))
    	    	continue;
            customer[i] = new Customer();
	    NodeList details = records.item(i).getChildNodes();
	    for(int j = 0 ; j < details.getLength();j++) {
	    	Node n = details.item(j);
	    	String column = n.getNodeName();
	    	if (column.equals("CustomerName")) {
                    customer[i].setName(URLDecoder.decode(n.getTextContent(),"UTF-8"));
                }else if (column.equals(MBPartner.COLUMNNAME_Value)) 
                    customer[i].setSearchKey(n.getTextContent());
	    	else if (column.equals(MBPartner.COLUMNNAME_C_BPartner_ID)) 
                    customer[i].setId(n.getTextContent());
	    	else if (column.equals(MBPartner.COLUMNNAME_Description)) 
                    customer[i].setDescription(URLDecoder.decode(n.getTextContent(),"UTF-8"));
                else if (column.equals("Address1")){ 
                    Location[] loc= new Location[1];
                    loc[0]= new Location();
                    loc[0].setAddress1(URLDecoder.decode(n.getTextContent(),"UTF-8"));
                    customer[i].setLocations(loc);
                }else if(column.equals("IsActive")){
                    if (n.getTextContent().compareTo("Y")==0)
                        customer[i].setVisible(Boolean.TRUE);
                    else
                        customer[i].setVisible(Boolean.FALSE);
                
                }else if(column.equals("TotalOpenBalance")){
                    customer[i].setMaxdebt(Double.parseDouble(n.getTextContent()));
                }
	    }
    	}
    	return customer;
	} 
}
