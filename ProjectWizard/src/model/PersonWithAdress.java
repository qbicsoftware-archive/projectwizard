package model;

public class PersonWithAdress {
  private String zdvID;
  private String firstName;
  private String lastName;
  private String email;
  private String street;
  private String zipCode;
  private String city;
  private String phone;
  private String institute;
  
  public PersonWithAdress(String zdvID, String firstName, String lastName, String email,
      String street, String zipCode, String city, String telephone, String institute) {
    super();
    this.zdvID = zdvID;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.street = street;
    this.zipCode = zipCode;
    this.city = city;
    this.phone = telephone;
    this.institute = institute;
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

  public String getStreet() {
    return street;
  }

  public String getZipCode() {
    return zipCode;
  }

  public String getCity() {
    return city;
  }

  public String getPhone() {
    return phone;
  }

  public String getInstitute() {
    return institute;
  }

}
