package de.sgrad.yamahathreditor;

import java.io.Serializable;

public class SongPatchData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String firstPatch;
	private String firstPatchName;
	private int firstPatchTime;
	private int secondPatchTime;
	private String secondPatch;
	private String secondPatchName;

	public String getFirstPatchName() {
		return firstPatchName;
	}

	public void setFirstPatchName(String firstPatchName) {
		this.firstPatchName = firstPatchName;
	}

	public String getSecondPatchName() {
		return secondPatchName;
	}

	public void setSecondPatchName(String secondPatchName) {
		this.secondPatchName = secondPatchName;
	}

	SongPatchData(){
		firstPatch = "";
		secondPatch = "";
		firstPatchName = "";
		secondPatchName = "";
	}

	public String getFirstPatch() {
		return firstPatch;
	}

	public void setFirstPatch(String firstPatch) {
		this.firstPatch = firstPatch;
	}

	public int getFirstPatchTime() {
		return firstPatchTime;
	}

	public void setFirstPatchTime(int firstPatchTime) {
		this.firstPatchTime = firstPatchTime;
	}

	public int getSecondPatchTime() {
		return secondPatchTime;
	}

	public void setSecondPatchTime(int secondPatchTime) {
		this.secondPatchTime = secondPatchTime;
	}

	public String getSecondPatch() {
		return secondPatch;
	}

	public void setSecondPatch(String secondPatch) {
		this.secondPatch = secondPatch;
	}
	

	
}
