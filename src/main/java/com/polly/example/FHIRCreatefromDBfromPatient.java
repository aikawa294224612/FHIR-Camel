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
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.json.JSONArray;
import org.json.JSONObject;


/* 實作FHIR創建資料(POST)，資料來自MSSQL，資料參考花*病患資料欄位(多筆) */
public class FHIRCreatefromDBfromPatient {
	
	static PrivateData privateData = new PrivateData();
	static String serverName = privateData.serverName;
	static String instanceName = privateData.instanceName;
	static String port = privateData.port;
	static String dbname = privateData.dbname;
	static String table = privateData.table;
	static String fhirserver = privateData.fhirserverurl;
	
	static Functions func = new Functions();

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		

		String dbURL = "jdbc:sqlserver://"+ serverName +"\\"+ instanceName +":"+ port +";"
	    		+ "encrypt=true;databaseName=" + dbname + ";"
	    		+ "trustServerCertificate=true";
		
	    DataSource dataSource = func.setupDataSource(dbURL);
	    DefaultRegistry reg = new DefaultRegistry();
	    reg.bind("myDataSource", dataSource);
	        
	    CamelContext context = new DefaultCamelContext(reg);
	    
	    context.addRoutes(new RouteBuilder(){
			
			public void configure(){
				
				final List<String> IDlist = new ArrayList<String>();
				
				from("timer://foo?repeatCount=1")
				.setBody(constant("SELECT * FROM " + table + " WHERE Fid is NULL"))
		        .to("jdbc:myDataSource")
		        .marshal().json(true)
		        .to("file:src/test?fileName=patientdb")
				.process(new Processor() {

					public void process(Exchange exchange) throws Exception {
						
						String body = exchange.getIn().getBody(String.class);
						
						JSONArray jsonArr = new JSONArray(body);
						
						Bundle bundle = new Bundle();
						List<BundleEntryComponent> becs = new ArrayList<BundleEntryComponent>();
						
						for(int i=0; i< jsonArr.length(); i++) {
							
							JSONObject jsonObj = jsonArr.getJSONObject(i);	
							
							String pid = jsonObj.get("Pid").toString();
							String uid = jsonObj.get("Uid").toString();
							String name = jsonObj.get("Name").toString();
							String gender = jsonObj.get("Sex").toString();
							String birth = jsonObj.get("Birthday").toString();
							String address = jsonObj.get("Address").toString();
							String tel = jsonObj.get("Tel").toString();
//							String marriage = jsonObj.get("Marriage").toString();
//							String contact_title = jsonObj.get("EmergencyContactTitle").toString();
							String contact_name = jsonObj.get("EmergencyContactName").toString();
							String contact_address = jsonObj.get("EmergencyContactAddress").toString();
							String contact_tel = jsonObj.get("EmergencyContactTel").toString();
							
							IDlist.add(uid);
							
							BundleEntryComponent bec = new BundleEntryComponent();
							bec.setResource(func.setPatient(uid, pid, name, gender, birth, address, tel, contact_name, contact_address, contact_tel));
							BundleEntryRequestComponent berc = new BundleEntryRequestComponent();
							berc.setMethod(HTTPVerb.POST);
							berc.setUrl("Patient");
							bec.setRequest(berc);	
							becs.add(bec);
							
						}
						
						bundle.setEntry(becs);
						bundle.setType(BundleType.TRANSACTION);
				        
				        exchange.getIn().setBody(bundle);
						
					}
					
				})
//				.marshal().fhirJson(true) 
//				.to("file:src/test?fileName=patientfhir")
				.to("fhir://transaction/withBundle?"
			    		+ "inBody=bundle&"
			    		+ "fhirVersion=R4&"
			    		+ "serverUrl="+fhirserver)
//				.marshal().fhirJson(true) 
//				.to("file:src/test?fileName=result")
		        .process(new Processor() {
					public void process(Exchange exchange) throws Exception {
						
						String updateSql_front = "UPDATE "+ table
								+ " SET [Fid] "
								+ "= CASE [Uid]";
						
						String updateSql_end = "WHERE [Uid] IN(";
						
						Bundle result = (Bundle) exchange.getIn().getBody();					
						for(int i=0; i< result.getEntry().size(); i++) {
							String location = result.getEntry().get(i).getResponse().getLocation();
							String fhirId = location.split("/")[1];
							
							updateSql_front += " WHEN '"+ IDlist.get(i) +"' THEN '"+ fhirId + "'";
							updateSql_end += "'"+ IDlist.get(i) + "',";

						}
						
						String sql = updateSql_front + " END " + updateSql_end.substring(0, updateSql_end.length()-1) + ")";
						
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
