package com.example.entity;

import javax.persistence.*;

@Entity
@Table(name = "ASCODE")
@IdClass(AsCodeId.class)
public class AsCode {

    @Id
    private String CODENAME;

    @Id
    private String CODEVALUE;

    private String SHORTDESCRIPTION;
    private String LONGDESCRIPTION;

    public String getCODENAME() { return CODENAME; }
    public void setCODENAME(String v) { this.CODENAME = v; }

    public String getCODEVALUE() { return CODEVALUE; }
    public void setCODEVALUE(String v) { this.CODEVALUE = v; }

    public String getSHORTDESCRIPTION() { return SHORTDESCRIPTION; }
    public void setSHORTDESCRIPTION(String v) { this.SHORTDESCRIPTION = v; }

    public String getLONGDESCRIPTION() { return LONGDESCRIPTION; }
    public void setLONGDESCRIPTION(String v) { this.LONGDESCRIPTION = v; }
}
