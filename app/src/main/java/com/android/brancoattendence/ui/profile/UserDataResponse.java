package com.android.brancoattendence.ui.profile;

import com.google.gson.annotations.SerializedName;

public class UserDataResponse {

    @SerializedName("emp_id")
    private int employeeId;

    @SerializedName("f_name")
    private String firstName;

    @SerializedName("m_name")
    private  String middleName;

    @SerializedName("l_name")
    private  String lastName;

    private String email;

    @SerializedName("m_number")
    private String contactNo;

    @SerializedName("dept_name")
    private String department;

    private String designation;

    @SerializedName("j_date")
    private String dateOfJoining;



    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public  String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public  String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getContactNo() {
        return contactNo;
    }

    public String getDepartment() {
        return department;
    }

    public String getDesignation() {
        return designation;
    }

    public String getDateOfJoining() {
        return dateOfJoining;
    }
}
