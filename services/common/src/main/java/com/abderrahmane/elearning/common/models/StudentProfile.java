package com.abderrahmane.elearning.common.models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "student_profil")
public class StudentProfile {
    @Id
    @Column(name = "account_id")
    private String id;

    @MapsId
    @OneToOne(targetEntity = Account.class, optional = false)
    @JoinColumn(name = "account_id")
    private Account account;
    
    
    @Column(name = "first_name", nullable = false)
    private String firstname;
    
    @Column(name = "last_name", nullable = false)
    private String lastname;
    
    @ManyToOne(targetEntity = City.class, optional = false)
    @JoinColumn(name = "location")
    private City location;
    
    @Temporal(TemporalType.DATE)
    @Column(name = "day_of_birth", nullable = false)
    private Calendar dayOfBirth;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", nullable = false)
    private Calendar createdDate = Calendar.getInstance();

    @OneToMany(targetEntity = RequestForConnection.class, orphanRemoval = true, mappedBy = "studentProfile")
    private List<RequestForConnection> requests = new ArrayList<>();

    @OneToMany(targetEntity = StudentTeacherConnection.class, orphanRemoval = true, mappedBy = "student")
    private List<StudentTeacherConnection> connections = new ArrayList<>();

    public StudentProfile () {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public City getLocation() {
        return location;
    }

    public void setLocation(City location) {
        this.location = location;
    }
    
    public Calendar getDayOfBirth() {
        return dayOfBirth;
    }

    public void setDayOfBirth(Calendar dayOfBirth) {
        this.dayOfBirth = dayOfBirth;
    }

    public Calendar getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Calendar createdDate) {
        this.createdDate = createdDate;
    }

    public List<RequestForConnection> getRequests() {
        return requests;
    }

    public void setRequests(List<RequestForConnection> requests) {
        this.requests = requests;
    }

    public List<StudentTeacherConnection> getConnections() {
        return connections;
    }

    public void setConnections(List<StudentTeacherConnection> connections) {
        this.connections = connections;
    }
}
