/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vpb.tts;

import java.util.Objects;

/**
 *
 * @author quynx
 */
public class CurvesModel {
    
    private String curve_name;
    
    private String tenor;
    
    private Double value;

    public CurvesModel() {
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.curve_name);
        hash = 89 * hash + Objects.hashCode(this.tenor);
        hash = 89 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CurvesModel other = (CurvesModel) obj;
        if (!Objects.equals(this.curve_name, other.curve_name)) {
            return false;
        }
        if (!Objects.equals(this.tenor, other.tenor)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }
    
    

    public CurvesModel(String curve_name, String tenor) {
        this.curve_name = curve_name;
        this.tenor = tenor;
    }

    
    
    public String getCurve_name() {
        return curve_name;
    }

    public void setCurve_name(String curve_name) {
        this.curve_name = curve_name;
    }

    public String getTenor() {
        return tenor;
    }

    public void setTenor(String tenor) {
        this.tenor = tenor;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
    
    
    
}
