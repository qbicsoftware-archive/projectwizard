package control;

import main.OpenBisClient;

import com.vaadin.data.Validator;

public class ProjectNameValidator implements Validator {
  
  OpenBisClient openbis;
  
  public ProjectNameValidator(OpenBisClient openbis) {
    this.openbis = openbis;
  }
  
  @Override
  public void validate(Object value) throws InvalidValueException {
    String val = (String) value;
    if (val != null && !val.isEmpty())
      if (openbis.getProjectByCode(val.toUpperCase()) != null)
        throw new InvalidValueException("Project code already in use");
  }
}
