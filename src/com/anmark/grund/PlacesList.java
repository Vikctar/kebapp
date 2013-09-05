package com.anmark.grund;

import java.io.Serializable;
import java.util.List;

import com.google.api.client.util.Key;

/** 
* Enables passing PlaceList objects between activities
* */
public class PlacesList implements Serializable {

	@Key
	public String status;

	@Key
	public List<Place> results;

}