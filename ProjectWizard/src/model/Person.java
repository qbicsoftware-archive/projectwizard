package model;

public class Person {
  private String zdvID;
  private String firstName;
  private String lastName;
  private String email;
  private String phone;
  private int instituteID;

  public Person(String zdvID, String firstName, String lastName, String email, String telephone,
      int instituteID) {
    super();
    this.zdvID = zdvID;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.phone = telephone;
    this.instituteID = instituteID;
  }

  public String getZdvID() {
    return zdvID;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getEmail() {
    return email;
  }

  public String getPhone() {
    return phone;
  }

  public int getInstituteID() {
    return instituteID;
  }

}
