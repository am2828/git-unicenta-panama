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

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.ImageUtils;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.inventory.MovementReason;
import com.openbravo.pos.inventory.TaxCategoryInfo;
import com.openbravo.pos.ticket.CategoryInfo;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.erp.externalsales.Category;
import com.openbravo.pos.erp.externalsales.Product;
import com.openbravo.pos.erp.externalsales.ProductPlus;
import com.openbravo.pos.erp.externalsales.Tax;
import com.openbravo.pos.erp.possync.DataLogicIntegration;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPrice;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Sergio Oropeza - Double Click Sistemas - Venezuela - soropeza@dcs.net.ve, info@dcs.net.ve
 */
public class InsertProductstoDB {
    private final DataLogicSystem dlsystem;
    private final DataLogicIntegration dlintegration;
    private final DataLogicSales dlsales;
    private final String warehouse;
    private Object poslocator;

   //Basado en las clases del POS red1
    public InsertProductstoDB(DataLogicSystem dlsystem, DataLogicIntegration dlintegration, DataLogicSales dlsales, String warehouse) {
        this.dlsystem = dlsystem;
        this.dlintegration = dlintegration;
        this.dlsales = dlsales;
        this.warehouse = warehouse;
         Properties activeMQProp = dlsystem.getResourceAsProperties("openbravo.properties");
         poslocator = activeMQProp.getProperty("pos");

    }
    
    
    public boolean ImportProducts(String productsXML){
        try {
            ProductPlus[] products = importQueue2Products(productsXML);
            if (products == null){
                    throw new BasicException(AppLocal.getIntString("message.returnnull"));
                }
                
                if (products.length > 0){
                    dlintegration.syncProductsBefore();                
                    Date now = new Date();
                    System.out.println(products.length);
                    for (Product product : products) {
                        System.out.println("Registering Product: "+product.getName());
                            if (product==null) break;
                        // Synchonization of taxcategories
                        TaxCategoryInfo tc = new TaxCategoryInfo(product.getTax().getId(), URLDecoder.decode(product.getTax().getName(),"UTF-8"));
                        dlintegration.syncTaxCategory(tc);
                        
                        // Synchonization of taxes
                        TaxInfo t = new TaxInfo(
                                product.getTax().getId(),
                                URLDecoder.decode(product.getTax().getName(),"UTF-8"),
                                tc.getID(),
                                //new Date(Long.MIN_VALUE),
                                null,
                                null,
                                product.getTax().getPercentage() / 100,
                                false,
                                0);
                        dlintegration.syncTax(t);
                       
                        // Synchonization of categories
                        CategoryInfo c = new CategoryInfo(product.getCategory().getId(), URLDecoder.decode(product.getCategory().getName(),"UTF-8"), null,null,null);
                        dlintegration.syncCategory(c);

                        // Synchonization of products
                        ProductInfoExt p = new ProductInfoExt();
                        p.setID(product.getId());
                        p.setReference(product.getReference());
                        p.setCode(product.getEan() == null || product.getEan().equals("") ? product.getId() : product.getEan());
                       // String auxname=product.getName().replaceAll("%(?![0-9a-fA-F]{2})", "%25");
                       // auxname=auxname.replaceAll("\\+", "%2B");
                        System.out.println(product.getName());
                        p.setName(URLDecoder.decode(product.getName(),"UTF-8"));
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
                    return true;
                }
        } catch (SAXException ex) {
            Logger.getLogger(InsertProductstoDB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(InsertProductstoDB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(InsertProductstoDB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BasicException ex) {
            Logger.getLogger(InsertProductstoDB.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return false;
    
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
		
		int cnt = -1;	
	    	for(int i = 0 ; i < records.getLength();i++) {
	
	    		//Checks to disallow certain detail records...
	    	    //check if right POS Name (within the loop, as all Orgs are together)
	
	    		//check if XML type is about Products
	    	    if (!records.item(i).getFirstChild().getTextContent().equals(MProduct.Table_Name))
	    	    	continue;

                        Category cate = null; 
                        Tax newtax = null;

		    	NodeList details = records.item(i).getChildNodes();
		    	for(int j = 0 ; j < details.getLength();j++) {
		    		Node n = details.item(j);
		    		String column = n.getNodeName();
                                
                                //cate.setId("99");//will be replaced later by XML tags later
                                //cate.setName("ADTax"); //will be replaced later
                                
		    		if (column.equals("POSLocatorName")) 
		    		{
		    			//checking if right POS Name
		    			if (!poslocator.equals(n.getTextContent()))
		    				break; 
                                    	cnt++;
			    		product[cnt] = new ProductPlus();
                                        cate = new Category();
                                        newtax = new Tax();
		    		}

                                else if (column.equals("ProductName")) 
		    			product[cnt].setName(URLDecoder.decode(n.getTextContent(),"UTF-8"));
		    		
		    		else if (column.equals(MProduct.COLUMNNAME_M_Product_Category_ID)) 
	    			{
	    				cate.setId(n.getTextContent());
                                        if (cate.getName() != null)
                                            product[cnt].setCategory(cate);
	    			}		    		

                                else if (column.equals("CategoryName")) 
	    			{
	    				cate.setName(URLDecoder.decode(n.getTextContent(),"UTF-8"));
                                        if (cate.getName() != null)
                                            product[cnt].setCategory(cate);
	    			}		
		    		
		    		else if (column.equals(MProduct.COLUMNNAME_M_Product_ID)) 
		    			product[cnt].setId(n.getTextContent());
		    		
		    		else if (column.equals(MProduct.COLUMNNAME_C_TaxCategory_ID)) 
		    			{
		    				newtax.setId(n.getTextContent());
                                                if (newtax.getName() != null)
                	    				product[cnt].setTax(newtax);
		    			}
		    		else if (column.equals("TaxName")) 
	    				{
	    					newtax.setName(n.getTextContent());
                                                if (newtax.getId() != null)
        	    					product[cnt].setTax(newtax);
    	    				}
		    		else if (column.equals("TaxRate")) 
	    				{
	    					newtax.setPercentage(Double.parseDouble(n.getTextContent()));
                                                if (newtax.getId() != null || newtax.getName() != null)
                                                    product[cnt].setTax(newtax);
	    				}
                                 // Add Reference XML file            
                                //else if (column.equals("Reference")) 
                                //        {
	    			//		product[cnt].setReference(n.getTextContent());
                                //        }
		    		
		    		else if (column.equals("QtyOnHand")) 
		    			product[cnt].setQtyonhand(Double.parseDouble(n.getTextContent()));
		    		
		    		else if (column.equals(MProductPrice.COLUMNNAME_PriceList)) 
		    			product[cnt].setListPrice(Double.parseDouble(n.getTextContent()));
		    		else if (column.equals(MProductPrice.COLUMNNAME_PriceLimit)) 
		    			product[cnt].setPurchasePrice(Double.parseDouble(n.getTextContent()));
		    		else if (column.equals(MProduct.COLUMNNAME_UPC)) 
                                        {
                                            product[cnt].setEan(n.getTextContent());
                                            // Reference
                                            product[cnt].setReference(n.getTextContent());
                                        }

		    	}
	    	}
            // need to truncate products from nulls;
	    	ProductPlus[] nettProduct = new ProductPlus[cnt+1]; 
	    	for (int i=0;i <= cnt; i++){
	    		nettProduct[i] = product[i];
	    	}	    	return nettProduct;
		}

}
