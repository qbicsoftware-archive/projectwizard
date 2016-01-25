/*******************************************************************************
 * QBiC Project Wizard enables users to create hierarchical experiments including different study conditions using factorial design.
 * Copyright (C) "2016"  Andreas Friedrich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package control;

import java.io.Serializable;
import java.util.Comparator;

import com.vaadin.data.util.DefaultItemSorter;

public class NumberStringItemSorter extends DefaultItemSorter {

  /**
   * Constructs a CaseInsensitiveItemSorter that uses a case-insensitive sorter for string property
   * values, and the default otherwise.
   * 
   */
  public NumberStringItemSorter() {
    super(new NumberStringValueComparator());
  }

  /**
   * Provides a case-insensitive comparator used for comparing string {@link Property} values. The
   * <code>NumberStringValueComparator</code> assumes all objects it compares can be cast to
   * Comparable.
   * 
   */
  public static class NumberStringValueComparator implements Comparator<Object>, Serializable {

    private final boolean isDigit(char ch) {
      return ch >= 48 && ch <= 57;
    }

    /** Length of string is passed in for improved efficiency (only need to calculate it once) **/
    private final String getChunk(String s, int slength, int marker) {
      StringBuilder chunk = new StringBuilder();
      char c = s.charAt(marker);
      chunk.append(c);
      marker++;
      if (isDigit(c)) {
        while (marker < slength) {
          c = s.charAt(marker);
          if (!isDigit(c))
            break;
          chunk.append(c);
          marker++;
        }
      } else {
        while (marker < slength) {
          c = s.charAt(marker);
          if (isDigit(c))
            break;
          chunk.append(c);
          marker++;
        }
      }
      return chunk.toString();
    }

    public int compare(Object o1, Object o2) {
      if (!(o1 instanceof String) || !(o2 instanceof String)) {
        return 0;
      }
      String s1 = (String) o1;
      String s2 = (String) o2;

      int thisMarker = 0;
      int thatMarker = 0;
      int s1Length = s1.length();
      int s2Length = s2.length();

      while (thisMarker < s1Length && thatMarker < s2Length) {
        String thisChunk = getChunk(s1, s1Length, thisMarker);
        thisMarker += thisChunk.length();

        String thatChunk = getChunk(s2, s2Length, thatMarker);
        thatMarker += thatChunk.length();

        // If both chunks contain numeric characters, sort them numerically
        int result = 0;
        if (isDigit(thisChunk.charAt(0)) && isDigit(thatChunk.charAt(0))) {
          // Simple chunk comparison by length.
          int thisChunkLength = thisChunk.length();
          result = thisChunkLength - thatChunk.length();
          // If equal, the first different number counts
          if (result == 0) {
            for (int i = 0; i < thisChunkLength; i++) {
              result = thisChunk.charAt(i) - thatChunk.charAt(i);
              if (result != 0) {
                return result;
              }
            }
          }
        } else {
          result = thisChunk.compareTo(thatChunk);
        }

        if (result != 0)
          return result;
      }

      return s1Length - s2Length;
    }
  }
}
