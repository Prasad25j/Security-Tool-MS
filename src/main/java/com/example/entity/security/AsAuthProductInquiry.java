package com.example.entity.security;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import org.hibernate.annotations.Formula;

@Entity
@Table(name = "ASAUTHPRODUCTINQUIRY")
public class AsAuthProductInquiry {

    @Id
    @Column(name = "AUTHPRODUCTINQUIRYGUID")
    private String AUTHPRODUCTINQUIRYGUID;

    @Column(name = "AUTHPRODUCTGUID")
    private String AUTHPRODUCTGUID;

    @Column(name = "INQUIRYSCREENNAMEGUID")
    private String INQUIRYSCREENGUID;

    @Formula("(SELECT ac.SECURITYGROUPGUID FROM ASAUTHCOMPANY ac JOIN ASAUTHPRODUCT ap ON ac.AUTHCOMPANYGUID = ap.AUTHCOMPANYGUID WHERE ap.AUTHPRODUCTGUID = AUTHPRODUCTGUID)")
    private String SECURITYGROUPGUID;

    @Formula("(SELECT ac.COMPANYGUID FROM ASAUTHCOMPANY ac JOIN ASAUTHPRODUCT ap ON ac.AUTHCOMPANYGUID = ap.AUTHCOMPANYGUID WHERE ap.AUTHPRODUCTGUID = AUTHPRODUCTGUID)")
    private String COMPANYGUID;

    @Formula("(SELECT ap.PRODUCTGUID FROM ASAUTHPRODUCT ap WHERE ap.AUTHPRODUCTGUID = AUTHPRODUCTGUID)")
    private String PRODUCTGUID;

    @Formula("(SELECT i.SCREENNAME FROM ASINQUIRYSCREEN i WHERE i.INQUIRYSCREENGUID = INQUIRYSCREENNAMEGUID)")
    private String INQUIRYNAME;

    public String getAUTHPRODUCTINQUIRYGUID() { return AUTHPRODUCTINQUIRYGUID; }
    public void setAUTHPRODUCTINQUIRYGUID(String v) { this.AUTHPRODUCTINQUIRYGUID = v; }
    public String getAUTHPRODUCTGUID() { return AUTHPRODUCTGUID; }
    public void setAUTHPRODUCTGUID(String v) { this.AUTHPRODUCTGUID = v; }
    public String getINQUIRYSCREENGUID() { return INQUIRYSCREENGUID; }
    public void setINQUIRYSCREENGUID(String v) { this.INQUIRYSCREENGUID = v; }

    public String getSECURITYGROUPGUID() { return SECURITYGROUPGUID; }
    public void setSECURITYGROUPGUID(String v) { this.SECURITYGROUPGUID = v; }
    public String getCOMPANYGUID() { return COMPANYGUID; }
    public void setCOMPANYGUID(String v) { this.COMPANYGUID = v; }
    public String getPRODUCTGUID() { return PRODUCTGUID; }
    public void setPRODUCTGUID(String v) { this.PRODUCTGUID = v; }
    public String getINQUIRYNAME() { return INQUIRYNAME; }
    public void setINQUIRYNAME(String v) { this.INQUIRYNAME = v; }
}
