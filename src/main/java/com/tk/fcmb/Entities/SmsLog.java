/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tk.fcmb.Entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author joel.eze
 */
@Entity
@Table(name = "sms_log")
@NamedQueries({
    @NamedQuery(name = "SmsLog.findAll", query = "SELECT s FROM SmsLog s")
    , @NamedQuery(name = "SmsLog.findById", query = "SELECT s FROM SmsLog s WHERE s.id = :id")
    , @NamedQuery(name = "SmsLog.findBySenderMobile", query = "SELECT s FROM SmsLog s WHERE s.senderMobile = :senderMobile")
    , @NamedQuery(name = "SmsLog.findByDestinationMobile", query = "SELECT s FROM SmsLog s WHERE s.destinationMobile = :destinationMobile")
    , @NamedQuery(name = "SmsLog.findByResponseCode", query = "SELECT s FROM SmsLog s WHERE s.responseCode = :responseCode")
    , @NamedQuery(name = "SmsLog.findByMessage", query = "SELECT s FROM SmsLog s WHERE s.message = :message")
    , @NamedQuery(name = "SmsLog.findByCreatedAt", query = "SELECT s FROM SmsLog s WHERE s.createdAt = :createdAt")})
public class SmsLog implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Size(max = 25)
    @Column(name = "sender_mobile")
    private String senderMobile;
    @Size(max = 25)
    @Column(name = "destination_mobile")
    private String destinationMobile;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 5)
    @Column(name = "response_code")
    private String responseCode;
    @Size(max = 255)
    @Column(name = "message")
    private String message;
    @Basic(optional = false)
    @NotNull
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public SmsLog() {
    }

    public SmsLog(Long id) {
        this.id = id;
    }

    public SmsLog(Long id, String responseCode, Date createdAt) {
        this.id = id;
        this.responseCode = responseCode;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSenderMobile() {
        return senderMobile;
    }

    public void setSenderMobile(String senderMobile) {
        this.senderMobile = senderMobile;
    }

    public String getDestinationMobile() {
        return destinationMobile;
    }

    public void setDestinationMobile(String destinationMobile) {
        this.destinationMobile = destinationMobile;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SmsLog)) {
            return false;
        }
        SmsLog other = (SmsLog) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.octacode.fcmbmobileprocessor.models.SmsLog[ id=" + id + " ]";
    }
    
}
