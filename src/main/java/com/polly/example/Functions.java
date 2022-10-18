package com.polly.example;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Identifier.IdentifierUse;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.Patient.ContactComponent;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;

public class Functions {
	
	static PrivateData privateData = new PrivateData();
	static FHIRData fhirdata = new FHIRData();
	static CodeData codedata = new CodeData();
	
	/*資料庫連接資料*/
	public DataSource setupDataSource(String connectURI) {
	    BasicDataSource ds = new BasicDataSource();
	    ds.setDriverClassName(privateData.className);
	    ds.setUsername(privateData.username);
	    ds.setPassword(privateData.pw);
	    ds.setUrl(connectURI);
	    return ds;
	  }
	
	/* 設置identifier */
	public Identifier setIdentifier(IdentifierUse use, String codesystem, String code, String system, String uid) {
	  	Identifier iden =new Identifier();
        iden.setUse(use);
        iden.setType(setCodeableConcept(setCoding(codesystem, code)));        
        iden.setSystem(system);	
        iden.setValue(uid);	
        return iden;
    }
  
	/* 設置姓名 */
	public List<HumanName> setNames(String name){
	  HumanName humanname = new HumanName();
      humanname.setText(name);
      List<HumanName> names = new ArrayList<HumanName>();
      names.add(humanname);
	  return names;
  }
  
	/* 設置性別 */  
	public AdministrativeGender setGender(String gender){
		  if(gender.equals(codedata.female)) {
	        	return AdministrativeGender.FEMALE;
	        }else if(gender.equals(codedata.male)) {
	        	return AdministrativeGender.MALE;
	        }else {
	        	return AdministrativeGender.OTHER;
	        }	
	  }
  
	/* 設置出生 */
	public Date setBirthdate(String birth) throws ParseException{
	  Date birthdate = new SimpleDateFormat("yyyy-MM-dd").parse(birth);
	  return birthdate;
		
  }
  
	/* 設置地址 */
	public List<Address> setAddresses(String address){
	  Address addr = new Address();
	  addr.setText(address);
	  addr.setExtension(setAddressExtensions("三段", "210號", "大有里", "19鄰", "52巷", "6弄", "2樓", "B室"));
      List<Address> addrs = new ArrayList<Address>();
      addrs.add(addr);
	  return addrs;
  }
  
	/* 設置聯絡方式(手機) */
	public ContactPoint setTel(String tel){
		ContactPoint con =new ContactPoint();
		con.setUse(ContactPoint.ContactPointUse.MOBILE);	        
		con.setSystem(ContactPoint.ContactPointSystem.PHONE);
		con.setValue(tel);
		return con;
  }
  
	/* 設置聯絡方式(聯絡人) */
	public ContactComponent setContact(String name, String address, String tel){
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
  
	public List<Extension> setAddressExtensions(String section, String number, String village, 
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
  
	public Extension setExtension(String text, String url){ 
	  	Extension extension = new Extension();
	    StringType stringtype = new StringType(text);
	    extension.setUrl(url);
	    extension.setValue(stringtype);
	    
        return extension;
  }
	
	public Coding setCoding(String system, String code){ 
		Coding coding = new Coding();
		coding.setSystem(system);
		coding.setCode(code);
	    
        return coding;
  }
	
	public Coding setCoding(String system, String code, String display){ 
		Coding coding = new Coding();
		coding.setSystem(system);
		coding.setCode(code);
		coding.setDisplay(display);
	    
        return coding;
  }
	
	public CodeableConcept setCodeableConcept(Coding coding){ 
		CodeableConcept concept = new CodeableConcept();
		List<Coding> codings = new ArrayList<Coding>();
        codings.add(coding); 
        concept.setCoding(codings);
	    
        return concept;
  }
	
	public List<CodeableConcept> setCodeableConcepts(CodeableConcept cc){ 
	    List<CodeableConcept> cons = new ArrayList<CodeableConcept>();    
	    cons.add(cc);    
        return cons;
  }

	public Reference setReference(String url){ 
	    Reference r = new Reference();
	    r.setReference(url);
	    return r;
  }
	
	public Quantity setQuantity(float value, String unit, String system, String code){ 
		Quantity q = new Quantity();
	    q.setValue(value);
	    q.setUnit(unit);
	    q.setSystem(system);
	    q.setCode(code);	    
	    return q;	    
  }
	
	public ObservationComponentComponent setOCC(String system, String code, String display, 
			float value, String unit, String u_system, String u_code) {
		ObservationComponentComponent occ = new ObservationComponentComponent();
		occ.setCode(setCodeableConcept(setCoding(system, code, display)));
		occ.setValue(setQuantity(value,unit, u_system, u_code));
		return occ;
	}

	
	public BundleEntryRequestComponent setBundleEntryRequestComponent(String url){ 
		BundleEntryRequestComponent berc = new BundleEntryRequestComponent();
		berc.setMethod(HTTPVerb.POST);
		berc.setUrl(url);
	    return berc;	    
  }
  
	public Patient setPatient(String uid, String pid, String name, String gender, String birth,
		  String address, String tel, String contact_name, String contact_address, String contact_tel) throws ParseException{
        Patient patient = new Patient();
        List<Identifier> idens = new ArrayList<Identifier>();
        
        // 身分證字號 
        idens.add(setIdentifier(IdentifierUse.OFFICIAL, 
        		fhirdata.idenUrl, 
        		codedata.idenCode,
        		fhirdata.moiUrl,
        		uid));
        
        // 病歷號
        idens.add(setIdentifier(IdentifierUse.OFFICIAL, 
        		fhirdata.idenUrl, 
        		codedata.recordCode,
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
	
	public Observation setWeight(String pid, String fid, int weight, Date mdate) throws ParseException{
	        Observation ob = new Observation();
	        List<Identifier> idens = new ArrayList<Identifier>();
	          
	        // 病歷號
	        idens.add(setIdentifier(IdentifierUse.OFFICIAL, 
	        		fhirdata.idenUrl, 
	        		codedata.recordCode,
	        		fhirdata.tzuchiUrl,
	        		pid));
	            
	        ob.setIdentifier(idens);
	        ob.setStatus(ObservationStatus.PRELIMINARY);
	        ob.setCategory(setCodeableConcepts(setCodeableConcept(setCoding(fhirdata.obCategoryUrl, 
	        		codedata.vitalsign, codedata.vs_display))));
	        ob.setCode(setCodeableConcept(setCoding(fhirdata.loincUrl, codedata.weight, 
	        		codedata.weight_display)));
	        ob.setSubject(setReference("Patient/" + fid));
	        DateTimeType d = new DateTimeType(mdate);
	        ob.setEffective(d);	        
	        ob.setIssued(mdate);
	        ob.setValue(setQuantity(weight, codedata.unit_weight, fhirdata.measureUrl, codedata.unit_weight));
	      
	        return ob;
	  }
	
	public Observation setHeight(String pid, String fid, int height, Date mdate) throws Exception{
        Observation ob = new Observation();
        List<Identifier> idens = new ArrayList<Identifier>();
          
        // 病歷號
        idens.add(setIdentifier(IdentifierUse.OFFICIAL, 
        		fhirdata.idenUrl, 
        		codedata.recordCode,
        		fhirdata.tzuchiUrl,
        		pid));
            
        ob.setIdentifier(idens);
        ob.setStatus(ObservationStatus.PRELIMINARY);
        ob.setCategory(setCodeableConcepts(setCodeableConcept(setCoding(fhirdata.obCategoryUrl, 
        		codedata.vitalsign, codedata.vs_display))));
        ob.setCode(setCodeableConcept(setCoding(fhirdata.loincUrl, codedata.height, 
        		codedata.height_display)));
        ob.setSubject(setReference("Patient/" + fid));
        DateTimeType d = new DateTimeType(mdate);
        ob.setEffective(d);	        
        ob.setIssued(mdate);
        ob.setValue(setQuantity(height, codedata.unit_height, fhirdata.measureUrl, codedata.unit_height));
      
        return ob;
  }
	
	public Observation setPressure(String pid, String fid, int systolic, 
			int diastolic, int heartrate, Date mdate) throws Exception{
        Observation ob = new Observation();
        List<Identifier> idens = new ArrayList<Identifier>();
          
        // 病歷號
        idens.add(setIdentifier(IdentifierUse.OFFICIAL, 
        		fhirdata.idenUrl, 
        		codedata.recordCode,
        		fhirdata.tzuchiUrl,
        		pid));
            
        ob.setIdentifier(idens);
        ob.setStatus(ObservationStatus.PRELIMINARY);
        ob.setCategory(setCodeableConcepts(setCodeableConcept(setCoding(fhirdata.obCategoryUrl, 
        		codedata.vitalsign, codedata.vs_display))));
        ob.setCode(setCodeableConcept(setCoding(fhirdata.loincUrl, codedata.bp, 
        		codedata.bp_display)));
        ob.setSubject(setReference("Patient/" + fid));
        DateTimeType d = new DateTimeType(mdate);
        ob.setEffective(d);	        
        ob.setIssued(mdate);
        
        List<ObservationComponentComponent> Occs = new ArrayList<ObservationComponentComponent>();
        //收縮壓
        Occs.add(setOCC(fhirdata.loincUrl, codedata.systolic, codedata.systolic_display, 
        		systolic, codedata.unit_bp, fhirdata.measureUrl, codedata.code_bp));
        //舒張壓
        Occs.add(setOCC(fhirdata.loincUrl, codedata.diastolic, codedata.diastolic_display, 
        		diastolic, codedata.unit_bp, fhirdata.measureUrl, codedata.code_bp));
        
        //心律
        Occs.add(setOCC(fhirdata.loincUrl, codedata.hr, codedata.hr_display, 
        		heartrate, codedata.unit_hr, fhirdata.measureUrl, codedata.unit_hr));
        
        ob.setComponent(Occs);
      
        return ob;
  }
	
	
	public String getID(List<Identifier> idens, String code) {
		String iden = "";
		for(int i=0; i<idens.size(); i++) {
			if(idens.get(i).getType().getCoding().get(0).getCode().equals(code)) {
				iden = idens.get(i).getValue();
				break;
			}
		}
		
		return iden;

	  }
	
	public String getGender(AdministrativeGender administrativeGender) {
		if(administrativeGender == AdministrativeGender.FEMALE) {
			return "2";
		}else if(administrativeGender == AdministrativeGender.MALE)  {
			return "1";
		}else {
			return "0";
		}


	  }
	
	public String getPhone(List<ContactPoint> contacts, ContactPoint.ContactPointUse use) {
		String value = "";
		for(int i=0; i<contacts.size(); i++) {
			if(contacts.get(i).getUse() == use) {
				value = contacts.get(i).getValue();
				break;
			}
		}
		
		return value;

	  }
	
	public String getMarrage(CodeableConcept code) {
		if(code.getCoding().get(0).getCode().equals(codedata.unmarried)) {
			return "未婚";
		}else if(code.getCoding().get(0).getCode().equals(codedata.married)){
			return "已婚";
		}else if(code.getCoding().get(0).getCode().equals(codedata.divorce)){
			return "離婚";
		}else {
			return "Unknown";
		}

	  }
	
	public String getBirth(Date birthday) {
		DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");  
		return dateFormat.format(birthday);
	}
  
	
	public String getContactRelation(ContactComponent contactcompoent) {
		if(contactcompoent.hasRelationship()) {
			if(contactcompoent.getRelationship().get(0).getCoding().get(0).getCode().equals(codedata.father)) {
				return "父";
			}else if(contactcompoent.getRelationship().get(0).getCoding().get(0).getCode().equals(codedata.mother)){
				return "母";
			}else if(contactcompoent.getRelationship().get(0).getCoding().get(0).getCode().equals(codedata.sprouse)){
				return "配偶";
			}else {
				return "Unknown";
			}
		}else {
			return null;
		}
		

	  }
	
	public String getContectPhone(ContactComponent contactcompoent, ContactPoint.ContactPointUse use) {
		if(contactcompoent.hasTelecom()) {
			String value = "";
			for(int i=0; i<contactcompoent.getTelecom().size(); i++) {
				if(contactcompoent.getTelecom().get(i).getUse() == use) {
					value = contactcompoent.getTelecom().get(i).getValue();
					break;
				}
			}
			return value;
		}else {
			return null;
		}			

	  }
	
	public String getPatientFid(String Uid) throws Exception {
		
		Connection conn = null;
		
		String value = "";
		 
		Class.forName(privateData.className);
        conn = DriverManager.getConnection(privateData.dbURL, privateData.username, privateData.pw);
        if (conn != null) {
            DatabaseMetaData dm = (DatabaseMetaData) conn.getMetaData();
//            System.out.println("Driver name: " + dm.getDriverName());
//            System.out.println("Driver version: " + dm.getDriverVersion());
//            System.out.println("Product name: " + dm.getDatabaseProductName());
//            System.out.println("Product version: " + dm.getDatabaseProductVersion());
            
            Statement selectStmt = conn.createStatement();
            ResultSet rs = selectStmt
              .executeQuery("SELECT [Fid] FROM Patient WHERE [Uid] = '"+ Uid +"'");
            
            while(rs.next())
            {
            	value =  rs.getString(1); 
            }            
        }
        return value;
	}
	
	public String getUpdateSql(String table, String column, Bundle result, List<String> IDlist){
		
		String updateSql_front = "UPDATE "+ table
				+ " SET [Fid] "
				+ "= CASE ["+column+"]";
		
		String updateSql_end = "WHERE ["+column+"] IN(";
				
		for(int i=0; i< result.getEntry().size(); i++) {
			String location = result.getEntry().get(i).getResponse().getLocation();
			String fhirId = location.split("/")[1];
			
			updateSql_front += " WHEN '"+ IDlist.get(i) +"' THEN '"+ fhirId + "'";
			updateSql_end += "'"+ IDlist.get(i) + "',";

		}						
		
		String sql = updateSql_front + " END " + updateSql_end.substring(0, updateSql_end.length()-1) + ")";
		
		return sql;				

		}

}
