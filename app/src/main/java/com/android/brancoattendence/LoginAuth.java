// LoginAuth.java

// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.android.brancoattendence;
import java.util.List;

public class LoginAuth {
    private String message;
    private Employee employee;
    private long status;
    private String token;

    public String getMessage() { return message; }
    public void setMessage(String value) { this.message = value; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee value) { this.employee = value; }

    public long getStatus() { return status; }
    public void setStatus(long value) { this.status = value; }

    public String getToken() { return token; }
    public void setToken(String value) { this.token = value; }
}

// Employee.java

// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

class Employee {
    private String mNumber;
    private String lName;
    private String fName;
    private String deptName;
    private String designation;
    private String email;

    public String getMNumber() { return mNumber; }
    public void setMNumber(String value) { this.mNumber = value; }

    public String getLName() { return lName; }
    public void setLName(String value) { this.lName = value; }

    public String getFName() { return fName; }
    public void setFName(String value) { this.fName = value; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String value) { this.deptName = value; }

    public String getDesignation() { return designation; }
    public void setDesignation(String value) { this.designation = value; }

    public String getEmail() { return email; }
    public void setEmail(String value) { this.email = value; }
}
