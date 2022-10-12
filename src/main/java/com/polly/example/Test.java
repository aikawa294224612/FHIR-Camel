package com.polly.example;

public class Test {

	public static void main(String[] args) {
		String address = "臺北市大同區大有里19鄰承德路三段52巷6弄210號2樓B室";
		if(address.contains("區")) {
			  int index_district = address.indexOf("區");
			  int index_city = 0;
			  if(address.contains("市")) {
				  index_city = address.indexOf("市");
			  }else if(address.contains("縣")) {
				  index_city = address.indexOf("縣");
			  }
			  
			  System.out.println(address.substring(index_city+1, index_district+1));
			  
		  }
	}

}
