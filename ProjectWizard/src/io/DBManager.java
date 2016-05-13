/*******************************************************************************
 * QBiC Project Wizard enables users to create hierarchical experiments including different study
 * conditions using factorial design. Copyright (C) "2016" Andreas Friedrich
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import logging.Log4j2Logger;

public class DBManager {
  private DBConfig config;

  logging.Logger logger = new Log4j2Logger(DBManager.class);

  public DBManager(DBConfig config) {
    this.config = config;
  }

  private void logout(Connection conn) {
    try {
      conn.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private Connection login() {
    String DB_URL = "jdbc:mariadb://" + config.getHostname() + ":" + config.getPort() + "/"
        + config.getSql_database();

    Connection conn = null;

    try {
      Class.forName("org.mariadb.jdbc.Driver");
      conn = DriverManager.getConnection(DB_URL, config.getUsername(), config.getPassword());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return conn;
  }

  public void addOrChangeSecondaryNameForProject(String projectCode, String secondaryName) {
    logger
        .info("Adding/Updating secondary name of project " + projectCode + " to " + secondaryName);
    String sql = "UPDATE projects SET secondary_name=? WHERE tutorial_id=?";
    // String sql = "INSERT INTO projects (pi_id, project_code) VALUES(?, ?)";
    Connection conn = login();
    try (PreparedStatement statement = conn.prepareStatement(sql)) {
      statement.setString(2, projectCode);
      statement.setString(3, secondaryName);
      statement.execute();
      logger.info("Successful.");
    } catch (SQLException e) {
      logger.error("SQL operation unsuccessful: " + e.getMessage());
      e.printStackTrace();
    }
    logout(conn);
  }

  public int isProjectInDB(String projectIdentifier) {
    logger.info("Looking for project " + projectIdentifier + " in the DB");
    String sql = "SELECT * from projects WHERE openbis_project_identifier = ?";
    int res = -1;
    Connection conn = login();
    try {
      PreparedStatement statement = conn.prepareStatement(sql);
      statement.setString(1, projectIdentifier);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        res = rs.getInt("id");
        logger.info("project found!");
      }
    } catch (SQLException e) {
      logger.error("SQL operation unsuccessful: " + e.getMessage());
      e.printStackTrace();
    }
    logout(conn);
    return res;
  }

  public int addProjectToDB(String projectIdentifier, String projectName) {
    int exists = isProjectInDB(projectIdentifier);
    if (exists < 0) {
      logger.info("Trying to add project " + projectIdentifier + " to the person DB");
      String sql = "INSERT INTO projects (openbis_project_identifier, short_title) VALUES(?, ?)";
      Connection conn = login();
      try (PreparedStatement statement =
          conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        statement.setString(1, projectIdentifier);
        statement.setString(2, projectName);
        statement.execute();
        ResultSet rs = statement.getGeneratedKeys();
        if (rs.next()) {
          logout(conn);
          logger.info("Successful.");
          return rs.getInt(1);
        }
      } catch (SQLException e) {
        logger.error("SQL operation unsuccessful: " + e.getMessage());
        e.printStackTrace();
      }
      logout(conn);
      return -1;
    }
    return exists;
  }

  public boolean hasPersonRoleInProject(int personID, int projectID, String role) {
    logger.info("Checking if person already has this role in the project.");
    String sql =
        "SELECT * from projects_persons WHERE person_id = ? AND project_id = ? and project_role = ?";
    boolean res = false;
    Connection conn = login();
    try {
      PreparedStatement statement = conn.prepareStatement(sql);
      statement.setInt(1, personID);
      statement.setInt(2, projectID);
      statement.setString(3, role);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        res = true;
        logger.info("person already has this role!");
      }
    } catch (SQLException e) {
      logger.error("SQL operation unsuccessful: " + e.getMessage());
      e.printStackTrace();
    }
    logout(conn);
    return res;
  }

  public void addPersonToProject(int projectID, int personID, String role) {
    if (!hasPersonRoleInProject(personID, projectID, role)) {
      logger.info("Trying to add person with role " + role + " to a project.");
      String sql =
          "INSERT INTO projects_persons (project_id, person_id, project_role) VALUES(?, ?, ?)";
      Connection conn = login();
      try (PreparedStatement statement =
          conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        statement.setInt(1, projectID);
        statement.setInt(2, personID);
        statement.setString(3, role);
        statement.execute();
        logger.info("Successful.");
      } catch (SQLException e) {
        logger.error("SQL operation unsuccessful: " + e.getMessage());
        e.printStackTrace();
      }
      logout(conn);
    }
  }

  /**
   * returns a map of principal investigator first+last names along with the pi_id. only returns
   * active investigators
   * 
   * @return
   */
  public Map<String, Integer> getPrincipalInvestigatorsWithIDs() {
    String sql = "SELECT id, first_name, family_name FROM persons WHERE active = 1";
    Map<String, Integer> res = new HashMap<String, Integer>();
    Connection conn = login();
    try (PreparedStatement statement = conn.prepareStatement(sql)) {
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        int pi_id = rs.getInt("id");
        String first = rs.getString("first_name");
        String last = rs.getString("family_name");
        res.put(first + " " + last, pi_id);
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    logout(conn);
    return res;
  }

  public int addExperimentToDB(String id) {
    int exists = isExpInDB(id);
    if (exists < 0) {
      logger.info("Trying to add experiment " + id + " to the person DB");
      String sql = "INSERT INTO experiments (openbis_experiment_identifier) VALUES(?)";
      Connection conn = login();
      try (PreparedStatement statement =
          conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        statement.setString(1, id);
        statement.execute();
        ResultSet rs = statement.getGeneratedKeys();
        if (rs.next()) {
          logout(conn);
          logger.info("Successful.");
          return rs.getInt(1);
        }
      } catch (SQLException e) {
        logger.error("SQL operation unsuccessful: " + e.getMessage());
        e.printStackTrace();
      }
      logout(conn);
      return -1;
    }
    return exists;
  }

  private int isExpInDB(String id) {
    logger.info("Looking for experiment " + id + " in the DB");
    String sql = "SELECT * from experiments WHERE openbis_experiment_identifier = ?";
    int res = -1;
    Connection conn = login();
    try {
      PreparedStatement statement = conn.prepareStatement(sql);
      statement.setString(1, id);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        logger.info("project found!");
        res = rs.getInt("id");
      }
    } catch (SQLException e) {
      logger.error("SQL operation unsuccessful: " + e.getMessage());
      e.printStackTrace();
    }
    logout(conn);
    return res;
  }

  public void addPersonToExperiment(int expID, int personID, String role) {
    logger.debug("exp id: " + expID);
    logger.debug("person id: " + personID);

    if (!hasPersonRoleInExperiment(personID, expID, role)) {
      logger.info("Trying to add person with role " + role + " to an experiment.");
      String sql =
          "INSERT INTO experiments_persons (experiment_id, person_id, experiment_role) VALUES(?, ?, ?)";
      Connection conn = login();
      try (PreparedStatement statement =
          conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        statement.setInt(1, expID);
        statement.setInt(2, personID);
        statement.setString(3, role);
        statement.execute();
        logger.info("Successful.");
      } catch (SQLException e) {
        logger.error("SQL operation unsuccessful: " + e.getMessage());
        e.printStackTrace();
      }
      logout(conn);
    }
  }

  private boolean hasPersonRoleInExperiment(int personID, int expID, String role) {
    logger.info("Checking if person already has this role in the experiment.");
    String sql =
        "SELECT * from experiments_persons WHERE person_id = ? AND experiment_id = ? and experiment_role = ?";
    boolean res = false;
    Connection conn = login();
    try {
      PreparedStatement statement = conn.prepareStatement(sql);
      statement.setInt(1, personID);
      statement.setInt(2, expID);
      statement.setString(3, role);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        res = true;
        logger.info("person already has this role!");
      }
    } catch (SQLException e) {
      logger.error("SQL operation unsuccessful: " + e.getMessage());
      e.printStackTrace();
    }
    logout(conn);
    return res;
  }

  //
  // public String getInvestigatorForProject(String projectCode) {
  // String id_query = "SELECT pi_id FROM projects WHERE project_code = " + projectCode;
  // String id = "";
  // Connection conn = login();
  // try (PreparedStatement statement = conn.prepareStatement(id_query)) {
  // ResultSet rs = statement.executeQuery();
  // while (rs.next()) {
  // id = Integer.toString(rs.getInt("pi_id"));
  // }
  // statement.close();
  // } catch (SQLException e) {
  // e.printStackTrace();
  // }
  //
  // String sql = "SELECT first_name, last_name FROM project_investigators WHERE pi_id = " + id;
  // String fullName = "";
  // try (PreparedStatement statement = conn.prepareStatement(sql)) {
  // ResultSet rs = statement.executeQuery();
  // while (rs.next()) {
  // String first = rs.getString("first_name");
  // String last = rs.getString("last_name");
  // fullName = first + " " + last;
  // }
  // statement.close();
  // } catch (SQLException e) {
  // e.printStackTrace();
  // }
  // logout(conn);
  // return fullName;
  // }

  // /**
  // * add a new institute to the database. not in use yet since the schema is old
  // *
  // * @param name
  // * @param street
  // * @param zip
  // * @param city
  // * @return
  // */
  // public int addNewInstitute(String name, String street, String zip, String city) {
  // String sql = "insert into institutes (name, street, zip_code, city) " + "VALUES(?, ?, ?, ?)";
  // Connection conn = login();
  // try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
  // {
  // statement.setString(1, name);
  // statement.setString(2, street);
  // statement.setString(3, zip);
  // statement.setString(4, city);
  // statement.execute();
  // ResultSet rs = statement.getGeneratedKeys();
  // if (rs.next()) {
  // return rs.getInt(1);
  // }
  // logger.info("Successful.");
  // } catch (SQLException e) {
  // logger.error("SQL operation unsuccessful: " + e.getMessage());
  // e.printStackTrace();
  // }
  // logout(conn);
  // return -1;
  // }

  // /**
  // * add a person whose institude id is known. not in use yet since the schema is old
  // *
  // * @return
  // */
  // public int addNewPersonWithInstituteID(Person p) {
  // String sql =
  // "insert into project_investigators (zdvID, first_name, last_name, email, phone, institute_id,
  // active) "
  // + "VALUES(?, ?, ?, ?, ?, ?, ?)";
  // Connection conn = login();
  // try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
  // {
  // statement.setString(1, p.getZdvID());
  // statement.setString(2, p.getFirstName());
  // statement.setString(3, p.getLastName());
  // statement.setString(4, p.getEmail());
  // statement.setString(5, p.getPhone());
  // statement.setInt(6, p.getInstituteID());
  // statement.setInt(7, 1);
  // statement.execute();
  // ResultSet rs = statement.getGeneratedKeys();
  // if (rs.next()) {
  // return rs.getInt(1);
  // }
  // logger.info("Successful.");
  // } catch (SQLException e) {
  // logger.error("SQL operation unsuccessful: " + e.getMessage());
  // e.printStackTrace();
  // }
  // logout(conn);
  // return -1;
  // }

  // public int addNewPerson(PersonWithAdress p) {
  // String sql =
  // "insert into project_investigators (zdvID, first_name, last_name, email, street, zip_code,
  // city, phone, institute, active) "
  // + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
  // Connection conn = login();
  // try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
  // {
  // statement.setString(1, p.getZdvID());
  // statement.setString(2, p.getFirstName());
  // statement.setString(3, p.getLastName());
  // statement.setString(4, p.getEmail());
  // statement.setString(5, p.getStreet());
  // statement.setString(6, p.getZipCode());
  // statement.setString(7, p.getCity());
  // statement.setString(8, p.getPhone());
  // statement.setString(9, p.getInstitute());
  // statement.setInt(10, 1);
  // statement.execute();
  // ResultSet rs = statement.getGeneratedKeys();
  // if (rs.next()) {
  // return rs.getInt(1);
  // }
  // logger.info("Successful.");
  // } catch (SQLException e) {
  // logger.error("SQL operation unsuccessful: " + e.getMessage());
  // e.printStackTrace();
  // }
  // logout(conn);
  // return -1;
  // }

  // public void printPeople() {
  // String sql = "SELECT * FROM project_investigators";
  // Connection conn = login();
  // try (PreparedStatement statement = conn.prepareStatement(sql)) {
  // ResultSet rs = statement.executeQuery();
  // while (rs.next()) {
  // System.out.println(Integer.toString(rs.getInt(1)) + " " + rs.getString(2) + " "
  // + rs.getString(3) + " " + rs.getString(4) + " " + rs.getString(5) + " "
  // + rs.getString(6) + " " + rs.getString(7) + " " + rs.getString(8) + " "
  // + rs.getString(9) + " " + rs.getString(10) + " " + rs.getString(11));
  // }
  // statement.close();
  // } catch (SQLException e) {
  // e.printStackTrace();
  // }
  // }
  //
  // public void printProjects() {
  // String sql = "SELECT pi_id, project_code FROM projects";
  // Connection conn = login();
  // try (PreparedStatement statement = conn.prepareStatement(sql)) {
  // ResultSet rs = statement.executeQuery();
  // while (rs.next()) {
  // int pi_id = rs.getInt("pi_id");
  // String first = rs.getString("project_code");
  // System.out.println(pi_id + first);
  // }
  // statement.close();
  // } catch (SQLException e) {
  // e.printStackTrace();
  // }
  // }

}
