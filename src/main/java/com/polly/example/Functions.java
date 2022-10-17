package com.polly.example;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Identifier.IdentifierUse;
import org.hl7.fhir.r4.model.Patient.ContactComponent;

public class Functions {
	
	static PrivateData privateData = new PrivateData();
	static String username = privateData.username;
	static String pw = privateData.pw;
	static String className = privateData.className;
	
	static FHIRData fhirdata = new FHIRData();
	static CodeData codedata = new CodeData();
	
	// 資料庫連接資料
	public DataSource setupDataSource(String connectURI) {
	    BasicDataSource ds = new BasicDataSource();
	    ds.setDriverClassName(className);
	    ds.setUsername(username);
	    ds.setPassword(pw);
	    ds.setUrl(connectURI);
	    return ds;
	  }
	
	// set Identifier
	public Identifier setIdentifier(IdentifierUse use, String codesystem, String code, String system, String uid) {
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
  
	//set Human Name
	public List<HumanName> setNames(String name){
	  HumanName humanname = new HumanName();
      humanname.setText(name);
      List<HumanName> names = new ArrayList<HumanName>();
      names.add(humanname);
	  return names;
  }
  
	  
	public AdministrativeGender setGender(String gender){
		  if(gender.equals(codedata.female)) {
	        	return AdministrativeGender.FEMALE;
	        }else if(gender.equals(codedata.male)) {
	        	return AdministrativeGender.MALE;
	        }else {
	        	return AdministrativeGender.OTHER;
	        }	
	  }
  
	public Date setBirthdate(String birth) throws ParseException{
	  Date birthdate = new SimpleDateFormat("yyyy-MM-dd").parse(birth);
	  return birthdate;
		
  }
  
	public List<Address> setAddresses(String address){
	  Address addr = new Address();
	  addr.setText(address);
	  addr.setExtension(setAddressExtensions("三段", "210號", "大有里", "19鄰", "52巷", "6弄", "2樓", "B室"));
      List<Address> addrs = new ArrayList<Address>();
      addrs.add(addr);
	  return addrs;
  }
  
	public ContactPoint setTel(String tel){
		ContactPoint con =new ContactPoint();
		con.setUse(ContactPoint.ContactPointUse.MOBILE);	        
		con.setSystem(ContactPoint.ContactPointSystem.PHONE);
		con.setValue(tel);
		return con;
  }
  
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

}
