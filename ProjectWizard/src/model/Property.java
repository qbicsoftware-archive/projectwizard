package model;

public class Property {

  private String name;
  private String type;
  private String unit;
  private String value;

  Property(String name, String type, String unit, String value) {
    this.name = name;
    this.type = type;
    this.unit = unit;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public String getUnit() {
    return unit;
  }

  public String getValue() {
    return value;
  }
}
