package com.polly.example;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultRegistry;
import org.hl7.fhir.r4.model.Bundle;


/* 實作FHIR創建資料(POST)，資料來自MSSQL，資料參考花*病患資料欄位(多筆) */
public class FHIRCreatefromDBfromHeight {
	
	static PrivateData privateData = new PrivateData();		
	static Functions func = new Functions();
	static CamelProcessor cp = new CamelProcessor();

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		
		String dbURL = privateData.dbURL;
		
	    DataSource dataSource = func.setupDataSource(dbURL);
	    DefaultRegistry reg = new DefaultRegistry();
	    reg.bind("myDataSource", dataSource);
	        
	    CamelContext context = new DefaultCamelContext(reg);
	    
	    context.addRoutes(new RouteBuilder(){
			
			public void configure(){
				
				final List<String> IDlist = new ArrayList<String>();
				
				from("timer://foo?repeatCount=1")
				.setBody(constant("SELECT * FROM " + privateData.heightTable + " WHERE Fid is NULL"))
		        .to("jdbc:myDataSource")
		        .marshal().json(true)
		        .to("file:src/test?fileName=weightdb")
				.process(new Processor() {

					public void process(Exchange exchange) throws Exception {
						
						String body = exchange.getIn().getBody(String.class);				        
				        exchange.getIn().setBody(cp.getFHIRBundles(body, IDlist, "BH"));
						
					}
					
				})
				.to("fhir://transaction/withBundle?"
			    		+ "inBody=bundle&"
			    		+ "fhirVersion=R4&"
			    		+ "serverUrl=" + privateData.fhirserverurl)
		        .process(new Processor() {
					public void process(Exchange exchange) throws Exception {
						
						Bundle result = (Bundle) exchange.getIn().getBody();
						String sql = func.getUpdateSql(privateData.heightTable, "Id", result, IDlist);

						exchange.getIn().setBody(sql);
					}
						
					})
					.to("jdbc:myDataSource")
					.process(new Processor() {
						public void process(Exchange exchange) throws Exception {						
					        
							System.out.println("Created!");
							
						}
					});
			}
	});
	          
	    context.start();
	    Thread.sleep(10000);
	    context.stop();
	  }
	  
}
