/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ossnoize.git.fastimport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.sync.Log;

/**
 *
 * @author steve
 */
public class GitAttributes {
  
  private final Map<String, List<GitAttributeKind>> fileContent = new TreeMap<String, List<GitAttributeKind>>(Collator.getInstance());
  private String topComment = new String();

  public void parse(InputStream stream) {
    BufferedReader parser = new BufferedReader(new InputStreamReader(stream));
    fileContent.clear();
    topComment = "";
    try {
      String line = parser.readLine();
      while (line != null)
      {
        if(line.startsWith("#")) {
          topComment += line + "\n";
        } else {
          ArrayList<GitAttributeKind> attrList = new ArrayList<GitAttributeKind>();
          line = line.trim();
          int pos = line.lastIndexOf(' ');
          while (pos >= 1) {
            String token = line.substring(pos + 1);
            GitAttributeKind attr = GitAttributeKind.fromRepresentation(token);
            if(attr == GitAttributeKind.NotAnAttribute)
            {
              break;
            }
            attrList.add(0, attr);
            line = line.substring(0, pos);
            pos = line.lastIndexOf(' ');
          }
          if(attrList.size() > 0)
          {
            fileContent.put(line, attrList);
          }
        }
        line = parser.readLine();
      }
    } catch (IOException ex) {
      Log.logf("Could not parse the git attributes: %s", ex);
    }
  }

  public void setTopLevelComment(String comment) {
    topComment = "#" + comment.replace("\n", "\n#");
  }
  
  public void addAttributeToPath(String path, GitAttributeKind ... allAttributes) {
    if(!fileContent.containsKey(path)) {
      fileContent.put(path, new ArrayList<GitAttributeKind>());
    }
    ArrayList<GitAttributeKind> tempList = new ArrayList(Arrays.asList(allAttributes));
    tempList.removeAll(fileContent.get(path));
    fileContent.get(path).addAll(tempList);
  }
  
  @Override
  public String toString() {
    String ret = topComment.trim();
    for(Map.Entry<String, List<GitAttributeKind>> entry : fileContent.entrySet()) {
      ret += "\n" + entry.getKey();
      for(GitAttributeKind attr : entry.getValue()) {
        ret += " " + attr.getAttributeType();
      }
    }
    return ret;
  }

  public void removePath(String path) {
    fileContent.remove(path);
  }

  public boolean pathHasAttributes(String path) {
    return fileContent.containsKey(path);
  }

}
