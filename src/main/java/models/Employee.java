package models;

import java.time.LocalDate;

public class Employee extends User{
	private String employeeID;
	private AccessLevel access;
	private Role jobRole;
	private LocalDate hireDate;
	private LocalDate terminationDate;
	
	
	//ENUMS
	public enum AccessLevel{
		LOW, MIDDLE, HIGH, SUPERADMIN
	}
	
	public enum Role{
		GENERAL, ADMIN, GUEST_SUPPORT, SERVICE_COORDINATOR, SUPERVISOR, MANAGER
	}
	
	//CONSTRUCTORS
	public Employee() {
		super();
		this.employeeID ="";
		this.access = AccessLevel.LOW;
		this.jobRole = Role.GENERAL;
		this.hireDate = LocalDate.now();
		this.terminationDate = null;
	}
	
	public Employee(User u, String employeeID, AccessLevel access, Role jobRole, LocalDate hireDate, LocalDate terminationDate) {
		super(u);
		this.employeeID = employeeID;
		this.access = access;
		this.jobRole = jobRole;
		this.hireDate = hireDate;
		this.terminationDate = terminationDate;
	}
	
	public Employee(Employee employee) {
		super(employee);
		this.employeeID = employee.employeeID;
		this.access = employee.access;
		this.jobRole = employee.jobRole;
		this.hireDate = employee.hireDate;
		this.terminationDate = employee.terminationDate;
	}
	
	
	//ACCESSORS
	public String getEmployeeID() {
		return employeeID;
	}

	public AccessLevel getAccess() {
		return access;
	}

	public Role getJobRole() {
		return jobRole;
	}

	public LocalDate getHireDate(){
		return hireDate;
	}

	public LocalDate getTerminationDate(){
		return terminationDate;
	}

	
	//MUTATORS
	public void setEmployeeID(String employeeID) {
		this.employeeID = employeeID;
	}

	public void setAccess(AccessLevel access) {
		this.access = access;
	}

	public void setJobRole(Role jobRole) {
		this.jobRole = jobRole;
	}

	public void setHireDate(LocalDate hireDate){
		this.hireDate = hireDate;
	}

	public void setTerminationDate(LocalDate terminationDate){
		this.terminationDate = terminationDate;
	}
	
	
	//TO STRING METHOD
	@Override
	public String toString() {
		String output = super.toString();
		output += "Employee ID: " + employeeID + "\n";
		output += "Job Role:" + jobRole + "\n";
		output += "Access Level: " + access + "\n";
		output += "Hire Date: " + hireDate + "\n";
		output += "Termination Date: " + terminationDate + "\n";
		return output;
	}
	
	
	//DISPLAY METHOD
	public void displayStaff() {
		System.out.println(this);
	}
	
	
	
	

}
