package com.anmark.grund;

import java.io.Serializable;

import com.google.api.client.util.Key;

/** 
* Enables passing PlaceDetails objects between activities
* */
public class PlaceDetails implements Serializable {

	@Key
	public String status;
	
	@Key
	public Place result;

	@Override
	public String toString() {
		if (result!=null) {
			return result.toString();
		}
		return super.toString();
	}
}
