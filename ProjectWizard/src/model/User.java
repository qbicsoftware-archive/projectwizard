package model;

public class User {
  
  private String name;
  private String email;
  private String phone;
  
  User(String name) {
    this.name = name;
  }
  
  User(String name, String email) {
    this(name);
    this.email = email;
  }
  
  User(String name, String email, String phone) {
    this(name, email);
    this.phone = phone;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getPhone() {
    return phone;
  }

}
