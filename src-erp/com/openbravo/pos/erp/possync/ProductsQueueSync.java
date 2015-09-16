//    Openbravo POS is a point of sales application designed for touch screens.
//    http://www.openbravo.com/product/pos
//    Copyright (c) 2007 openTrends Solucions i Sistemes, S.L
//    Modified by Openbravo SL on March 22, 2007
//    These modifications are copyright Openbravo SL
//    Author/s: A. Romero
//    You may contact Openbravo SL at: http://www.openbravo.com
//
//		Contributor: Redhuan D. Oon - ActiveMQ XML read from MClient.readmessage()
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

package com.openbravo.pos.erp.possync;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;

import org.compiere.model.MProduct;
import org.compiere.model.MProductPrice;
import org.compiere.model.MBPartner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.ImageUtils;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.ProcessAction;
import com.openbravo.pos.inventory.MovementReason;
import com.openbravo.pos.inventory.TaxCategoryInfo;
import com.openbravo.pos.ticket.CategoryInfo;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.erp.customers.Customer;
import com.openbravo.pos.erp.externalsales.Category;
import com.openbravo.pos.erp.externalsales.Product;
import com.openbravo.pos.erp.externalsales.ProductPlus;
import com.openbravo.pos.erp.externalsales.Tax;


/*
 * @contributor Sergio Oropeza - Double Click Sistemas - Venezuela - soropeza@dcs.net.ve, info@dcs.net.ve
 */


public class ProductsQueueSync implements ProcessAction {
        
    private DataLogicSystem dlsystem;
    private DataLogicIntegration dlintegration;
    private DataLogicSales dlsales;
    private String warehouse;
    private ExternalSalesHelper externalsales;
	private String productsXML  = "";
	private String customersXML = "";
    
    /** Creates a new instance of ProductsSync */
    public ProductsQueueSync(DataLogicSystem dlsystem, DataLogicIntegration dlintegration, DataLogicSales dlsales, String warehouse) {
        this.dlsystem = dlsystem;
        this.dlintegration = dlintegration;
        this.dlsales = dlsales;
        this.warehouse = warehouse;
        externalsales = null;
    }
    
    public MessageInf execute() throws BasicException {
        
        try {
        
            if (externalsales == null) {
                externalsales = new ExternalSalesHelper(dlsystem);
            }
            
            //red1 using XML string from ActiveMQ -- automatically check either path (so do not use same if u want to synch customers also)
            //red1 convert string to models deprecated approach below
             //productsXML = MQClient.receiveMessage(ExternalSalesHelper.MQueueHost, ExternalSalesHelper.MQueuePORT, ExternalSalesHelper.ProductsQueue);           
            productsXML = MQClient.receiveMessage(ExternalSalesHelper.MQueueHost, ExternalSalesHelper.MQueuePORT, ExternalSalesHelper.ProductsQueue);           
             ProductPlus[] products = importQueue2Products(productsXML);
             
             customersXML = MQClient.receiveMessage(ExternalSalesHelper.MQueueHost, ExternalSalesHelper.MQueuePORT, ExternalSalesHelper.CustomersQueue);           
             Customer[]	customers = importQueue2Customers(customersXML);
            
 //          Product[] products = externalsales.getProductsCatalog();
 //          Customer[] customers = externalsales.getCustomers();
            if (products == null || customers == null){
                throw new BasicException(AppLocal.getIntString("message.returnnull"));
            }     
            
            if (products.length > 0){
                dlintegration.syncProductsBefore();                
                Date now = new Date();
                
                for (Product product : products) {
                	if (product==null) break;
                    // Synchonization of taxcategories
                    TaxCategoryInfo tc = new TaxCategoryInfo(product.getTax().getId(), product.getTax().getName());
                    dlintegration.syncTaxCategory(tc);
                    
                    // Synchonization of taxes
                    TaxInfo t = new TaxInfo(
                            product.getTax().getId(),
                            product.getTax().getName(),
                            tc.getID(),
                            //new Date(Long.MIN_VALUE),
                            null,
                            null,
                            product.getTax().getPercentage() / 100,
                            false,
                            0);
                    dlintegration.syncTax(t);
                   
                    // Synchonization of categories
                    CategoryInfo c = new CategoryInfo(product.getCategory().getId(), product.getCategory().getName(), null,null,null);
                    dlintegration.syncCategory(c);

                    // Synchonization of products
                    ProductInfoExt p = new ProductInfoExt();
                    p.setID(product.getId());
                    p.setReference(product.getReference());
                    p.setCode(product.getEan() == null || product.getEan().equals("") ? product.getId() : product.getEan());
                    p.setName(product.getName());
                    p.setCom(false);
                    p.setScale(false);
                    p.setPriceBuy(product.getPurchasePrice());
                    p.setPriceSell(product.getListPrice());
                    p.setCategoryID(c.getID());
                    p.setTaxCategoryID(tc.getID());
                    p.setImage(ImageUtils.readImage(product.getImageUrl()));
                    dlintegration.syncProduct(p);  
                    
                    // Synchronization of stock          
                    if (product instanceof ProductPlus) {
                        
                        ProductPlus productplus = (ProductPlus) product;
                        
                        double diff = productplus.getQtyonhand() - dlsales.findProductStock(warehouse, p.getID(), null);
                        
                        Object[] diary = new Object[8];
                        diary[0] = UUID.randomUUID().toString();
                        diary[1] = now;
                        diary[2] = diff > 0.0 
                                ? MovementReason.IN_MOVEMENT.getKey()
                                : MovementReason.OUT_MOVEMENT.getKey();
                        diary[3] = warehouse;
                        diary[4] = p.getID();
                        diary[5] = null; ///TODO find out where to get AttributeInstanceID -- red1
                        diary[6] = new Double(diff);
                        diary[7] = new Double(p.getPriceBuy());                                
                        dlsales.getStockDiaryInsert().exec(diary);   
                    }
                }
                
                // datalogic.syncProductsAfter();
            }
            
            if (customers.length > 0 ) {
                
                dlintegration. syncCustomersBefore();
				//Add RIF to field Taxid
                for (Customer customer : customers) {                    
                    CustomerInfoExt cinfo = new CustomerInfoExt(customer.getId());
                    cinfo.setTaxid(fixRif(customer.getSearchKey()));
                    cinfo.setSearchkey(customer.getName());
                    cinfo.setName(customer.getName());          
                    cinfo.setNotes(customer.getDescription());
                    // TODO: Finish the integration of all fields.
                    dlintegration.syncCustomer(cinfo);
                }
            }
            
            if (products.length == 0 && customers.length == 0) {
                return new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.zeroproducts"));               
            } else {
                return new MessageInf(MessageInf.SGN_SUCCESS, AppLocal.getIntString("message.syncproductsok"), AppLocal.getIntString("message.syncproductsinfo", products.length, customers.length));
            }
                
        } catch (ServiceException e) {            
            throw new BasicException(AppLocal.getIntString("message.serviceexception"), e);
        } catch (MalformedURLException e){
            throw new BasicException(AppLocal.getIntString("message.malformedurlexception"), e);
        } catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return new MessageInf(MessageInf.SGN_CAUTION, AppLocal.getIntString("message.zerocustomers"));
        
    }
    // Remove hyphens and spaces of RIF
    private String fixRif(String rif){
    String RIF = rif;
        if(rif != null){          
           RIF =  rif.replace("-","" );
        }
     return RIF.toUpperCase();
    }

	private ProductPlus[] importQueue2Products(String productsXML) throws SAXException, IOException, ParserConfigurationException {
		//uncomment for testing, together with above
//		message = "<?xml version=\"1.0\" ?><entityDetail><type>MProduct</type><detail>..</detail></entityDetail>";
		if (productsXML.equals("")) 
			return new ProductPlus[0];
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new ByteArrayInputStream(productsXML.getBytes()));
                Element docEle = doc.getDocumentElement();
                NodeList records = docEle.getElementsByTagName("detail");
		ProductPlus[] product = new ProductPlus[records.getLength()];
		
		Tax newtax = new Tax();
		newtax.setId("99");//will be replaced later by XML tags later
		newtax.setName("ADTax"); //will be replaced later
		newtax.setPercentage(0.0d);
		
		Category cate = new Category();
		cate.setId("99");//will be replaced later by XML tags later
		cate.setName("ADTax"); //will be replaced later

		int cnt = -1;	
	    	for(int i = 0 ; i < records.getLength();i++) {
	
	    		//Checks to disallow certain detail records...
	    	    //check if right POS Name (within the loop, as all Orgs are together)
	
	    		//check if XML type is about Products
	    	    if (!records.item(i).getFirstChild().getTextContent().equals(MProduct.Table_Name))
	    	    	continue;
    	    
		    	NodeList details = records.item(i).getChildNodes();
		    	for(int j = 0 ; j < details.getLength();j++) {
		    		Node n = details.item(j);
		    		String column = n.getNodeName();
		    		
		    		if (column.equals("POSLocatorName")) 
		    		{
		    			//checking if right POS Name
		    			if (!ExternalSalesHelper.m_iERPPos.equals(n.getTextContent()))
		    				break; 
		    			cnt++;
			    		product[cnt] = new ProductPlus();
		    		}
		    			
		    		
		    		else if (column.equals("ProductName")) 
		    			product[cnt].setName(n.getTextContent());
		    		
		    		else if (column.equals(MProduct.COLUMNNAME_M_Product_Category_ID)) 
	    			{
	    				cate.setId(n.getTextContent());
		    			product[cnt].setCategory(cate);
	    			}		    		
		    		else if (column.equals("CategoryName")) 
	    			{
	    				cate.setName(n.getTextContent());
		    			product[cnt].setCategory(cate);
	    			}		
		    		
		    		else if (column.equals(MProduct.COLUMNNAME_M_Product_ID)) 
		    			product[cnt].setId(n.getTextContent());
		    		
		    		else if (column.equals(MProduct.COLUMNNAME_C_TaxCategory_ID)) 
		    			{
		    				newtax.setId(n.getTextContent());
 		    				product[cnt].setTax(newtax);
		    			}
		    		else if (column.equals("TaxName")) 
	    				{
	    					newtax.setName(n.getTextContent());
	    					product[cnt].setTax(newtax);
	    				}
                                 // Add Reference XML file            
                                else if (column.equals("Reference")) 
                                        {
	    					product[cnt].setReference(n.getTextContent());
                                        }
		    		
		    		
		    		else if (column.equals("QtyOnHand")) 
		    			product[cnt].setQtyonhand(Double.parseDouble(n.getTextContent()));
		    		
		    		else if (column.equals(MProductPrice.COLUMNNAME_PriceList)) 
		    			product[cnt].setListPrice(Double.parseDouble(n.getTextContent()));
		    		else if (column.equals(MProductPrice.COLUMNNAME_PriceLimit)) 
		    			product[cnt].setPurchasePrice(Double.parseDouble(n.getTextContent()));
		    		else if (column.equals(MProduct.COLUMNNAME_UPC)) 
		    			product[cnt].setEan(n.getTextContent());

		    	}
	    	}
            // need to truncate products from nulls;
	    	ProductPlus[] nettProduct = new ProductPlus[cnt+1]; 
	    	for (int i=0;i <= cnt; i++){
	    		nettProduct[i] = product[i];
	    	}
	    	return nettProduct;
		}
	
	private Customer[] importQueue2Customers(String customersXML) throws ParserConfigurationException, SAXException, IOException {
		if (customersXML.equals("")) return new Customer[0];
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
	    Document doc = db.parse(new ByteArrayInputStream(customersXML.getBytes()));
	    Element docEle = doc.getDocumentElement();
	    NodeList records = docEle.getElementsByTagName("detail");
		Customer[] customer = new Customer[records.getLength()];
		
		int cnt = 0;	
    	for(int i = 0 ; i < records.getLength();i++) {
    	    if (!records.item(i).getFirstChild().getTextContent().equals(MBPartner.Table_Name))
    	    	continue;
    		customer[i] = new Customer();
	    	NodeList details = records.item(i).getChildNodes();
	    	for(int j = 0 ; j < details.getLength();j++) {
	    		Node n = details.item(j);
	    		String column = n.getNodeName();
	    		
	    		if (column.equals("CustomerName")) 
	    			customer[i].setName(n.getTextContent());
	    		else if (column.equals(MBPartner.COLUMNNAME_Value)) 
	    			customer[i].setSearchKey(n.getTextContent());
	    		else if (column.equals(MBPartner.COLUMNNAME_C_BPartner_ID)) 
	    			customer[i].setId(n.getTextContent());
	    		else if (column.equals(MBPartner.COLUMNNAME_Description)) 
	    			customer[i].setDescription(n.getTextContent());
	    		
	    	}
    	}
    	return customer;
	} 
}
	
	 