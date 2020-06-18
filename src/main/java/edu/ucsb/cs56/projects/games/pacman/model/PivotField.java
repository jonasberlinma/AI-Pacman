package edu.ucsb.cs56.projects.games.pacman.model;

public class PivotField {
	private String fieldName;
	private int pivotLevel;
	
	public PivotField(String fieldName, int pivotLevel){
		this.fieldName = fieldName;
		this.pivotLevel = pivotLevel;
	}
	public String getFieldName() {
		return fieldName;
	}
	public int getPivotLevel() {
		return pivotLevel;
	}
}
