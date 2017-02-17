/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ghintech.sync;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.ImageUtils;
import com.openbravo.pos.erp.externalsales.Category;
import com.openbravo.pos.erp.externalsales.Product;
import com.openbravo.pos.erp.externalsales.ProductPlus;
import com.openbravo.pos.erp.externalsales.Tax;
import com.openbravo.pos.erp.sync.InsertProductstoDB;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.JRootApp;
import com.openbravo.pos.inventory.MovementReason;
import com.openbravo.pos.inventory.TaxCategoryInfo;
import com.openbravo.pos.ticket.CategoryInfo;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TaxInfo;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPrice;
import org.idempiere.webservice.client.base.Enums.WebServiceResponseStatus;
import org.idempiere.webservice.client.base.Field;
import org.idempiere.webservice.client.exceptions.WebServiceException;
import org.idempiere.webservice.client.net.WebServiceConnection;
import org.idempiere.webservice.client.request.QueryDataRequest;
import org.idempiere.webservice.client.response.WindowTabDataResponse;




/**
 *
 * @author egil
 */
public final class SyncProducts extends Sync{

    public SyncProducts(JRootApp app) {
        super(app);
        QueryDataRequest ws = new QueryDataRequest();
        ws.setWebServiceType("Productos");
        ws.setLogin(getLogin());
        //ws.setLimit(3);
        ws.setOffset(3);
        ws.setTableName("M_Product_WS_Queue_V");
        //DataRow data = new DataRow();
        //data.addField("Name", "%Store%");
       // ws.setDataRow(data);
 
        WebServiceConnection client = getClient();
 
        try {
            WindowTabDataResponse response = client.sendRequest(ws);
            if (response.getStatus() == WebServiceResponseStatus.Error) {
                System.out.println(response.getErrorMessage());
            } else {
                System.out.println("Total rows: " + response.getTotalRows());
                System.out.println("Num rows: " + response.getNumRows());
                System.out.println("Start row: " + response.getStartRow());
                
                ProductPlus[] product = new ProductPlus[response.getNumRows()];
                for (int i = 0; i < response.getDataSet().getRowsCount(); i++) {
                    System.out.println("Row: " + (i + 1));
                    product[i] = new ProductPlus();
                    Category cate = new Category();
                    Tax newtax = new Tax();
                    for (int j = 0; j < response.getDataSet().getRow(i).getFieldsCount(); j++) {
                        Field field = response.getDataSet().getRow(i).getFields().get(j);
                        System.out.println("Column: " + field.getColumn() + " = " + field.getValue());
                        String fieldValue = String.valueOf(field.getValue());
                        if (field.getColumn().equals("POSLocatorName")) 
                            product[i].setLocation_name(fieldValue);
                        
                        else if (field.getColumn().equals("M_Warehouse_ID")) 
                            product[i].setLocation_id(fieldValue);

                        else if (field.getColumn().equals("ProductName")) 
                            product[i].setName(URLDecoder.decode(fieldValue,"UTF-8"));
		    		
		    	else if (field.getColumn().equals(MProduct.COLUMNNAME_M_Product_Category_ID)) 
	    		{
                            cate.setId(fieldValue);
                            if (cate.getName() != null)
                                product[i].setCategory(cate);
                        }
                        else if (field.getColumn().equals("CategoryName")) 
	    		{
                            cate.setName(URLDecoder.decode(fieldValue,"UTF-8"));
                            if (cate.getName() != null)
                                product[i].setCategory(cate);
	    		}		
		    	else if (field.getColumn().equals(MProduct.COLUMNNAME_M_Product_ID)) 
                            product[i].setId(fieldValue);
		    	else if (field.getColumn().equals(MProduct.COLUMNNAME_C_TaxCategory_ID)) 
		    	{
                            newtax.setId(fieldValue);
                            if (newtax.getName() != null)
                                product[i].setTax(newtax);
		    	}
		    	else if (field.getColumn().equals("TaxName")) 
	    		{
                            newtax.setName(fieldValue);
                            if (newtax.getId() != null)
        	    		product[i].setTax(newtax);
    	    		}
		    	else if (field.getColumn().equals("TaxRate")) 
	    		{
                            newtax.setPercentage(Double.parseDouble(fieldValue));
                            if (newtax.getId() != null || newtax.getName() != null)
                                product[i].setTax(newtax);
	    		}
                        else if (field.getColumn().equals("QtyOnHand")) 
                            product[i].setQtyonhand(Double.parseDouble(fieldValue));
		    	else if (field.getColumn().equals(MProductPrice.COLUMNNAME_PriceList)) 
                            product[i].setListPrice(Double.parseDouble(fieldValue));
		    	else if (field.getColumn().equals(MProductPrice.COLUMNNAME_PriceLimit)) 
                            product[i].setPurchasePrice(Double.parseDouble(fieldValue));
		    	else if (field.getColumn().equals(MProduct.COLUMNNAME_UPC)) 
                        {
                            product[i].setEan(fieldValue);
                                            // Reference
                            product[i].setReference(fieldValue);
                        }
                    }
                    
                }
                ImportProducts(product);
            }   
        } catch (WebServiceException | UnsupportedEncodingException | NumberFormatException e) {
        }
    }
    
    public boolean ImportProducts(ProductPlus[] products){
        try {
            
            if (products == null){
                    throw new BasicException(AppLocal.getIntString("message.returnnull"));
                }
                
                if (products.length > 0){
                    dlintegration.syncProductsBefore();                
                    Date now = new Date();
                    System.out.println("Cantidad de productos" + products.length);
                    for (Product product : products) {
                        System.out.println("Registering Product: "+product.getName());
                            if (product==null) break;
                        // Synchonization of taxcategories
                        TaxCategoryInfo tc = new TaxCategoryInfo(product.getTax().getId(), URLDecoder.decode(product.getTax().getName(),"UTF-8"));
                        dlintegration.syncTaxCategory(tc);
                        TaxCategoryInfo tcaux = dlintegration.getTaxCategoryInfoByName(tc.getName());
                        // Synchonization of taxes
                        TaxInfo t = new TaxInfo(
                                product.getTax().getId(),
                                URLDecoder.decode(product.getTax().getName(),"UTF-8"),
                                tcaux.getID(),
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
                        CategoryInfo caux = dlintegration.getCategoryInfoByName(c.getName());
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
                        p.setCategoryID(caux.getID());
                        p.setTaxCategoryID(tcaux.getID());
                        p.setImage(ImageUtils.readImage(product.getImageUrl()));
                        // build html display like <html><font size=-2>MIRACLE NOIR<br> MASK</font>
                        
                        p.setDisplay("<html><font size=-2>"+product.getName().substring(0, product.getName().length()>15?15:product.getName().length()) 
                                    + "<br>" +((product.getName().length()>15)? product.getName().substring(15, product.getName().length()>30?30:product.getName().length()):"")
                                    + "</font>");
                        dlintegration.syncProduct(p);
                        
                        // Synchronization of stock          
                        if (product instanceof ProductPlus) {
                            
                            ProductPlus productplus = (ProductPlus) product;
                            ProductInfoExt productaux=dlsales.getProductInfoByCode(productplus.getReference());
                            //  Synchonization of locations
                            dlintegration.syncLocations(productplus.getLocation_id(),productplus.getLocation_name());
                            double diff = productplus.getQtyonhand() - dlsales.findProductStock(productplus.getLocation_id(), p.getID(), null);
                            
			    Object[] diary = new Object[9];
                            diary[0] = UUID.randomUUID().toString();
                            diary[1] = now;
                            diary[2] = diff > 0.0 
                                    ? MovementReason.IN_MOVEMENT.getKey()
                                    : MovementReason.OUT_MOVEMENT.getKey();
                            //diary[3] = warehouse;
                            diary[3] = productplus.getLocation_id();
                            String pid=p.getID();
                            if (productaux.getID()!=null)
                                   pid=productaux.getID();
                            diary[4] = pid;
                            diary[5] = null; ///TODO find out where to get AttributeInstanceID -- red1
                            diary[6] = new Double(diff);
                            diary[7] = new Double(p.getPriceBuy());
                            diary[8] = dlsystem.getUser();
                            dlsales.getStockDiaryInsert().exec(diary);   
                        }
                    }
                    
                    dlintegration.syncProductsAfter();
                    return true;
                }
        } catch (IOException ex) {
            Logger.getLogger(InsertProductstoDB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BasicException ex) {
            Logger.getLogger(InsertProductstoDB.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return false;
    }
}

