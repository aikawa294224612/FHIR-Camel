package com.polly.example;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.json.JSONArray;
import org.json.JSONObject;

public class CamelProcessor {
	
	static Functions func = new Functions();
	
	public Bundle getFHIRBundles(String body, List<String> IDlist, String category) throws Exception {
		
		JSONArray jsonArr = new JSONArray(body);		
		Bundle bundle = new Bundle();
		List<BundleEntryComponent> becs = new ArrayList<BundleEntryComponent>();
		
		if(category.equals("P")) {
			
			for(int i=0; i< jsonArr.length(); i++) {
				
				JSONObject jsonObj = jsonArr.getJSONObject(i);	
				
				String pid = jsonObj.get("Pid").toString();
				String uid = jsonObj.get("Uid").toString();
				String name = jsonObj.get("Name").toString();
				String gender = jsonObj.get("Sex").toString();
				String birth = jsonObj.get("Birthday").toString();
				String address = jsonObj.get("Address").toString();
				String tel = jsonObj.get("Tel").toString();
//				String marriage = jsonObj.get("Marriage").toString();
//				String contact_title = jsonObj.get("EmergencyContactTitle").toString();
				String contact_name = jsonObj.get("EmergencyContactName").toString();
				String contact_address = jsonObj.get("EmergencyContactAddress").toString();
				String contact_tel = jsonObj.get("EmergencyContactTel").toString();
				
				IDlist.add(uid);
				
				BundleEntryComponent bec = new BundleEntryComponent();
				bec.setResource(func.setPatient(uid, pid, name, gender, birth, 
						address, tel, contact_name, contact_address, contact_tel));
				bec.setRequest(func.setBundleEntryRequestComponent("Patient"));	
				becs.add(bec);
				
			}		
		
		}else if(category.equals("BW")) {
			
			for(int i=0; i< jsonArr.length(); i++) {
				
				JSONObject jsonObj = jsonArr.getJSONObject(i);	
				
				String uid = jsonObj.get("Pid").toString();  //身分證
				String pid = jsonObj.get("Id").toString();  //單號
				int weight = (Integer) jsonObj.get("BodyWeight"); //體重
				Timestamp t = new Timestamp((Long) jsonObj.get("MeasurementTime")); 
				Date measurementTime = new Date(t.getTime()); 							
				String Fid = func.getPatientFid(uid);  //FHIR ID
				
				IDlist.add(pid);
				
				BundleEntryComponent bec = new BundleEntryComponent();
				bec.setResource(func.setWeight(pid, Fid, weight, measurementTime));
				bec.setRequest(func.setBundleEntryRequestComponent("Observation"));	
				becs.add(bec);
				
			}
			
		}else if(category.equals("BH")) {
			
			for(int i=0; i< jsonArr.length(); i++) {
				
				JSONObject jsonObj = jsonArr.getJSONObject(i);	
				
				String uid = jsonObj.get("Pid").toString();  //身分證
				String pid = jsonObj.get("Id").toString();  //單號
				int height = (Integer) jsonObj.get("BodyHeight"); //身高
				Timestamp t = new Timestamp((Long) jsonObj.get("MeasurementTime")); 
				Date measurementTime = new Date(t.getTime()); 							
				String Fid = func.getPatientFid(uid);  //FHIR ID
				
				IDlist.add(pid);
				
				BundleEntryComponent bec = new BundleEntryComponent();
				bec.setResource(func.setHeight(pid, Fid, height, measurementTime));
				bec.setRequest(func.setBundleEntryRequestComponent("Observation"));	
				becs.add(bec);
				
			}
			
		}else if(category.equals("BP")) {
			
			for(int i=0; i< jsonArr.length(); i++) {
				
				JSONObject jsonObj = jsonArr.getJSONObject(i);	
				
				String uid = jsonObj.get("Pid").toString();  //身分證
				String pid = jsonObj.get("Id").toString();  //單號
				int systolic = (Integer) jsonObj.get("Systolic"); //收縮壓
//				float systolic_h = (Float) jsonObj.get("Systolic_H"); //收縮壓(高)
//				float systolic_l = (Float) jsonObj.get("Systolic_L"); //收縮壓(低)
				int diastolic = (Integer) jsonObj.get("Diastolic"); //舒張壓
//				float diastolic_h = (Float) jsonObj.get("Diastolic_H"); //舒張壓(高)
//				float diastolic_l = (Float) jsonObj.get("Diastolic_L"); //舒張壓(低)
				int heartrate = (Integer) jsonObj.get("HeartBeat"); //心律
				Timestamp t = new Timestamp((Long) jsonObj.get("MeasurementTime")); 
				Date measurementTime = new Date(t.getTime()); 							
				String Fid = func.getPatientFid(uid);  //FHIR ID
				
				IDlist.add(pid);
				
				BundleEntryComponent bec = new BundleEntryComponent();
				bec.setResource(func.setPressure(pid, Fid, systolic, diastolic, heartrate, measurementTime));
				bec.setRequest(func.setBundleEntryRequestComponent("Observation"));	
				becs.add(bec);
				
			}
			
		}
		
		bundle.setEntry(becs);
		bundle.setType(BundleType.TRANSACTION);
			
		return bundle;
		
	}

}
