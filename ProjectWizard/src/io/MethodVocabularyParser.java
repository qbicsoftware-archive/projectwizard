package io;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import uicomponents.LabelingMethod;

public class MethodVocabularyParser {

  public List<LabelingMethod> parseQuantificationMethods(File file) {
    Scanner scanner = null;
    try {
      scanner = new Scanner(file);
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    List<LabelingMethod> methods = new ArrayList<LabelingMethod>();
    List<String> reagents = new ArrayList<String>();
    String name = "";
    while (scanner.hasNext()) {
      String line = scanner.nextLine();
      if (!line.startsWith("\t")) {
        if (!name.isEmpty()) {
          methods.add(new LabelingMethod(name, reagents));
          reagents = new ArrayList<String>();
        }
        name = line.trim();
      } else {
        reagents.add(line.trim());
      }
    }
    methods.add(new LabelingMethod(name, reagents));
    scanner.close();
    return methods;
  }

}
