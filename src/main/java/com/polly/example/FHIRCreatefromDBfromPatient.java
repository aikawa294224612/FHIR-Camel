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
public class FHIRCreatefromDBfromPatient {
	
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
				.setBody(constant("SELECT * FROM " + 
				privateData.patientTable + " WHERE Fid is NULL"))
		        .to("jdbc:myDataSource")
		        .marshal().json(true)
		        .to("file:src/test?fileName=patientdb")
				.process(new Processor() {

					public void process(Exchange exchange) throws Exception {
					
						String body = exchange.getIn().getBody(String.class);
						
//						JSONArray jsonArr = new JSONArray(body);
//						
//						Bundle bundle = new Bundle();
//						List<BundleEntryComponent> becs = new ArrayList<BundleEntryComponent>();
//						
//						for(int i=0; i< jsonArr.length(); i++) {
//							
//							JSONObject jsonObj = jsonArr.getJSONObject(i);	
//							
//							String pid = jsonObj.get("Pid").toString();
//							String uid = jsonObj.get("Uid").toString();
//							String name = jsonObj.get("Name").toString();
//							String gender = jsonObj.get("Sex").toString();
//							String birth = jsonObj.get("Birthday").toString();
//							String address = jsonObj.get("Address").toString();
//							String tel = jsonObj.get("Tel").toString();
////							String marriage = jsonObj.get("Marriage").toString();
////							String contact_title = jsonObj.get("EmergencyContactTitle").toString();
//							String contact_name = jsonObj.get("EmergencyContactName").toString();
//							String contact_address = jsonObj.get("EmergencyContactAddress").toString();
//							String contact_tel = jsonObj.get("EmergencyContactTel").toString();
//							
//							IDlist.add(uid);
//							
//							BundleEntryComponent bec = new BundleEntryComponent();
//							bec.setResource(func.setPatient(uid, pid, name, gender, birth, 
//									address, tel, contact_name, contact_address, contact_tel));
//							bec.setRequest(func.setBundleEntryRequestComponent("Patient"));	
//							becs.add(bec);
//							
//						}
//						
//						bundle.setEntry(becs);
//						bundle.setType(BundleType.TRANSACTION);
				        
				        exchange.getIn().setBody(cp.getFHIRBundles(body, IDlist, "P"));
						
					}
					
				})
//				.marshal().fhirJson(true) 
//				.to("file:src/test?fileName=patientfhir")
				.to("fhir://transaction/withBundle?"
			    		+ "inBody=bundle&"
			    		+ "fhirVersion=R4&"
			    		+ "serverUrl=" + privateData.fhirserverurl)
//				.marshal().fhirJson(true) 
//				.to("file:src/test?fileName=result")
		        .process(new Processor() {
					public void process(Exchange exchange) throws Exception {

						Bundle result = (Bundle) exchange.getIn().getBody();	
						String sql = func.getUpdateSql(privateData.patientTable, 
								"Uid", result, IDlist);

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
