package com.polly.example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultRegistry;
import org.apache.commons.dbcp2.BasicDataSource;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Identifier.IdentifierUse;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Patient.ContactComponent;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.json.JSONArray;
import org.json.JSONObject;


/* 實作FHIR創建資料(POST)，資料來自MSSQL，資料參考花*病患資料欄位(多筆) */
public class FHIRTestDatabaseforMultiwithTWCore {
	
	static PrivateData privateData = new PrivateData();
	static String serverName = privateData.serverName;
	static String instanceName = privateData.instanceName;
	static String port = privateData.port;
	static String dbname = privateData.dbname;
	static String username = privateData.username;
	static String pw = privateData.pw;
	static String table = privateData.table;
	static String fhirserver = privateData.fhirserverurl;
	static String className = privateData.className;
	
	static FHIRData fhirdata = new FHIRData();
	static CodeData code = new CodeData();

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		

		String dbURL = "jdbc:sqlserver://"+ serverName +"\\"+ instanceName +":"+ port +";"
	    		+ "encrypt=true;databaseName=" + dbname + ";"
	    		+ "trustServerCertificate=true";
		
	    DataSource dataSource = setupDataSource(dbURL);
	    DefaultRegistry reg = new DefaultRegistry();
	    reg.bind("myDataSource", dataSource);
	        
	    CamelContext context = new DefaultCamelContext(reg);
	    
	    context.addRoutes(new RouteBuilder(){
			
			public void configure(){
				
				from("timer://foo?repeatCount=1")
				.setBody(constant("SELECT * FROM " + table))
		        .to("jdbc:myDataSource")
		        .marshal().json(true)
		        .to("file:src/test?fileName=fhirtestdb")
				.process(new Processor() {

					public void process(Exchange exchange) throws Exception {
						
						String body = exchange.getIn().getBody(String.class);
						
						System.out.println(body);
						
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
							String marriage = jsonObj.get("Marriage").toString();
							String contact_title = jsonObj.get("EmergencyContactTitle").toString();
							String contact_name = jsonObj.get("EmergencyContactName").toString();
							String contact_address = jsonObj.get("EmergencyContactAddress").toString();
							String contact_tel = jsonObj.get("EmergencyContactTel").toString();
							
							BundleEntryComponent bec = new BundleEntryComponent();
							bec.setResource(setPatient(uid, pid, name, gender, birth, address, tel, contact_name, contact_address, contact_tel));
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
				.marshal().fhirJson(true) 
				.to("file:src/test?fileName=fhirformat")
//				.to("fhir://transaction/withBundle?"
//			    		+ "inBody=bundle&"
//			    		+ "fhirVersion=R4&"
//			    		+ "serverUrl="+fhirserver)
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
	
	
	  private static DataSource setupDataSource(String connectURI) {
	    BasicDataSource ds = new BasicDataSource();
	    ds.setDriverClassName(className);
	    ds.setUsername(username);
	    ds.setPassword(pw);
	    ds.setUrl(connectURI);
	    return ds;
	  }
	  
	  private static Identifier setIdentifier(IdentifierUse use, String codesystem, String code, String system, String uid) {
		  	Identifier iden =new Identifier();
	        iden.setUse(use);
	        
	        Coding idencode = new Coding();
	        idencode.setSystem(codesystem);
	        idencode.setCode(code);
	        List<Coding> idencodings = new ArrayList<Coding>();
	        idencodings.add(idencode);
	        CodeableConcept iden_concept = new CodeableConcept();
	        iden_concept.setCoding(idencodings);
	        iden.setType(iden_concept);
	        
	        iden.setSystem(system);	
	        iden.setValue(uid);	
	        return iden;
	    }
	  
	  private static List<HumanName> setNames(String name){
		  HumanName humanname = new HumanName();
	      humanname.setText(name);
	      List<HumanName> names = new ArrayList<HumanName>();
	      names.add(humanname);
		  return names;
	  }
	  
	  private static AdministrativeGender setGender(String gender){
		  if(gender.equals(code.female)) {
	        	return AdministrativeGender.FEMALE;
	        }else if(gender.equals(code.male)) {
	        	return AdministrativeGender.MALE;
	        }else {
	        	return AdministrativeGender.OTHER;
	        }	
	  }
	  
	  private static Date setBirthdate(String birth) throws ParseException{
		  Date birthdate = new SimpleDateFormat("yyyy-MM-dd").parse(birth);
		  return birthdate;
			
	  }
	  
	  private static List<Address> setAddresses(String address){
		  Address addr = new Address();
		  addr.setText(address);
		  addr.setExtension(setAddressExtensions("三段", "210號", "大有里", "19鄰", "52巷", "6弄", "2樓", "B室"));
	      List<Address> addrs = new ArrayList<Address>();
	      addrs.add(addr);
		  return addrs;
	  }
	  
//	  private static String getSection(String address){
//		  if(address.contains("段")) {
//			  int index = address.indexOf("段");
//			  return Character.toString(address.charAt(index-1))+Character.toString(address.charAt(index));
//			  
//		  }else {
//			  return "";
//		  }
//	  }
	  
//	  private static String getNumber(String address){
//		  if(address.contains("號")) {
//			  int index_num = address.indexOf("號");
//			  int index_alley = address.indexOf("弄");
//			  return address.substring(index_alley+1, index_num+1);
//			  
//		  }else {
//			  return "";
//		  }
//	  }
	  
//	  private static String getVillage(String address){
//		  if(address.contains("里")) {
//			  int index_district = address.indexOf("區");
//			  int index_village = address.indexOf("里");
//			  return address.substring(index_district+1, index_village+1);
//			  
//		  }else {
//			  return "";
//		  }
//	  }
	  
//	  private static String getDistrict(String address){
//		  if(address.contains("區")) {
//			  int index_district = address.indexOf("區");
//			  int index_city = 0;
//			  if(address.contains("市")) {
//				  index_city = address.indexOf("市");
//			  }else if(address.contains("縣")) {
//				  index_city = address.indexOf("縣");
//			  }
//			  
//			  return address.substring(index_city+1, index_district+1);
//			  
//		  }else {
//			  return "";
//		  }
//	  }
	  
	  
	  private static ContactPoint setTel(String tel){
			ContactPoint con =new ContactPoint();
			con.setUse(ContactPoint.ContactPointUse.MOBILE);	        
			con.setSystem(ContactPoint.ContactPointSystem.PHONE);
			con.setValue(tel);
			return con;
	  }
	  
	  private static ContactComponent setContact(String name, String address, String tel){
		    //聯絡人姓名
		  	Patient.ContactComponent contact = new Patient.ContactComponent();
		    HumanName humanname = new HumanName();
	        humanname.setText(name);
	        contact.setName(humanname);
	        
	        // 聯絡人電話
	        List<ContactPoint> contacts = new ArrayList<ContactPoint>();
	        contacts.add(setTel(tel));	
	        contact.setTelecom(contacts);
	        
	       // 聯絡人地址
	        Address addr = new Address();
	        addr.setText(address);
	        contact.setAddress(addr);	        
	      
	        return contact;
	  }
	  
	  private static List<Extension> setAddressExtensions(String section, String number, String village, 
			  String neighborhood, String lane, String alley, String floor, String room){ 
		    List<Extension> extens = new ArrayList<Extension>(); 
	    
		    extens.add(setExtension(section, fhirdata.sectionUrl));
		    extens.add(setExtension(number, fhirdata.numberUrl));
		    extens.add(setExtension(village, fhirdata.villUrl));
		    extens.add(setExtension(neighborhood, fhirdata.neighUrl));
		    extens.add(setExtension(lane, fhirdata.laneUrl));
		    extens.add(setExtension(alley, fhirdata.alleyUrl));
		    extens.add(setExtension(floor, fhirdata.floorUrl));
		    extens.add(setExtension(room, fhirdata.roomUrl));
	      
	        return extens;
	  }
	  
	  private static Extension setExtension(String text, String url){ 
		  	Extension extension = new Extension();
		    StringType stringtype = new StringType(text);
		    extension.setUrl(url);
		    extension.setValue(stringtype);
		    
	        return extension;
	  }
	  
	  
	  private static Patient setPatient(String uid, String pid, String name, String gender, String birth,
			  String address, String tel, String contact_name, String contact_address, String contact_tel) throws ParseException{
	        Patient patient = new Patient();
	        List<Identifier> idens = new ArrayList<Identifier>();
	        
	        // 身分證字號 
	        idens.add(setIdentifier(IdentifierUse.OFFICIAL, 
	        		fhirdata.idenUrl, 
	        		code.idenCode,
	        		fhirdata.moiUrl,
	        		uid));
	        
	        // 病歷號
	        idens.add(setIdentifier(IdentifierUse.OFFICIAL, 
	        		fhirdata.idenUrl, 
	        		code.recordCode,
	        		fhirdata.tzuchiUrl,
	        		pid));
	            
	        patient.setIdentifier(idens); 				        
	        patient.setName(setNames(name));  //姓名		        
	        patient.setGender(setGender(gender));	//性別			         
	        patient.setBirthDate(setBirthdate(birth));  //出生年月日
	        patient.setAddress(setAddresses(address));  //地址
	        
	        //聯絡(電話)
	        List<ContactPoint> contacts = new ArrayList<ContactPoint>();
	        contacts.add(setTel(tel));	
	        patient.setTelecom(contacts);
	        
	        //聯絡人資料
	        List<ContactComponent> contactcomponents = new ArrayList<ContactComponent>();
	        contactcomponents.add(setContact(contact_name, contact_address, contact_tel));	
	        patient.setContact(contactcomponents);	 
	      
	        return patient;
	  }
	  
}
