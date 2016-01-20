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
import model.Person;
import model.PersonWithAdress;

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

  public static void main(String[] args) {
    DBManager dbm =
        new DBManager(new DBConfig("portal-testing.am10.uni-tuebingen.de", "3306",
            "project_investigator_db", "mariadbuser", "dZAmDa9-Ysq_Zv1AGygQ"));
    dbm.printPeople();
  }

  private Connection login() {
    String DB_URL =
        "jdbc:mariadb://" + config.getHostname() + ":" + config.getPort() + "/"
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

  public void addProjectForPrincipalInvestigator(int pi_id, String projectCode) {
    logger.info("Trying to add project " + projectCode + " to the principal investigator DB");
    String sql = "INSERT INTO projects (pi_id, project_code) VALUES(?, ?)";
    Connection conn = login();
    try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setInt(1, pi_id);
      statement.setString(2, projectCode);
      statement.execute();
      logger.info("Successful.");
    } catch (SQLException e) {
      logger.error("SQL operation unsuccessful: " + e.getMessage());
      e.printStackTrace();
    }
    logout(conn);
  }

  public String getInvestigatorForProject(String projectCode) {
    String id_query = "SELECT pi_id FROM projects WHERE project_code = " + projectCode;
    String id = "";
    Connection conn = login();
    try (PreparedStatement statement = conn.prepareStatement(id_query)) {
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        id = Integer.toString(rs.getInt("pi_id"));
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    String sql = "SELECT first_name, last_name FROM project_investigators WHERE pi_id = " + id;
    String fullName = "";
    try (PreparedStatement statement = conn.prepareStatement(sql)) {
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        String first = rs.getString("first_name");
        String last = rs.getString("last_name");
        fullName = first + " " + last;
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    logout(conn);
    return fullName;
  }

  /**
   * add a new institute to the database. not in use yet since the schema is old
   * 
   * @param name
   * @param street
   * @param zip
   * @param city
   * @return
   */
  public int addNewInstitute(String name, String street, String zip, String city) {
    String sql = "insert into institutes (name, street, zip_code, city) " + "VALUES(?, ?, ?, ?)";
    Connection conn = login();
    try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, name);
      statement.setString(2, street);
      statement.setString(3, zip);
      statement.setString(4, city);
      statement.execute();
      ResultSet rs = statement.getGeneratedKeys();
      if (rs.next()) {
        return rs.getInt(1);
      }
      logger.info("Successful.");
    } catch (SQLException e) {
      logger.error("SQL operation unsuccessful: " + e.getMessage());
      e.printStackTrace();
    }
    logout(conn);
    return -1;
  }

  /**
   * add a person whose institude id is known. not in use yet since the schema is old
   * 
   * @return
   */
  public int addNewPersonWithInstituteID(Person p) {
    String sql =
        "insert into project_investigators (zdvID, first_name, last_name, email, phone, institute_id, active) "
            + "VALUES(?, ?, ?, ?, ?, ?, ?)";
    Connection conn = login();
    try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, p.getZdvID());
      statement.setString(2, p.getFirstName());
      statement.setString(3, p.getLastName());
      statement.setString(4, p.getEmail());
      statement.setString(5, p.getPhone());
      statement.setInt(6, p.getInstituteID());
      statement.setInt(7, 1);
      statement.execute();
      ResultSet rs = statement.getGeneratedKeys();
      if (rs.next()) {
        return rs.getInt(1);
      }
      logger.info("Successful.");
    } catch (SQLException e) {
      logger.error("SQL operation unsuccessful: " + e.getMessage());
      e.printStackTrace();
    }
    logout(conn);
    return -1;
  }

  public int addNewPerson(PersonWithAdress p) {
    String sql =
        "insert into project_investigators (zdvID, first_name, last_name, email, street, zip_code, city, phone, institute, active) "
            + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    Connection conn = login();
    try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, p.getZdvID());
      statement.setString(2, p.getFirstName());
      statement.setString(3, p.getLastName());
      statement.setString(4, p.getEmail());
      statement.setString(5, p.getStreet());
      statement.setString(6, p.getZipCode());
      statement.setString(7, p.getCity());
      statement.setString(8, p.getPhone());
      statement.setString(9, p.getInstitute());
      statement.setInt(10, 1);
      statement.execute();
      ResultSet rs = statement.getGeneratedKeys();
      if (rs.next()) {
        return rs.getInt(1);
      }
      logger.info("Successful.");
    } catch (SQLException e) {
      logger.error("SQL operation unsuccessful: " + e.getMessage());
      e.printStackTrace();
    }
    logout(conn);
    return -1;
  }

  /**
   * returns a map of principal investigator first+last names along with the pi_id. only returns
   * active investigators
   * 
   * @return
   */
  public Map<String, Integer> getPrincipalInvestigatorsWithIDs() {
    String sql = "SELECT pi_id, first_name, last_name FROM project_investigators WHERE active = 1";
    Map<String, Integer> res = new HashMap<String, Integer>();
    Connection conn = login();
    try (PreparedStatement statement = conn.prepareStatement(sql)) {
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        int pi_id = rs.getInt("pi_id");
        String first = rs.getString("first_name");
        String last = rs.getString("last_name");
        res.put(first + " " + last, pi_id);
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    logout(conn);
    return res;
  }

  public void printPeople() {
    String sql = "SELECT * FROM project_investigators";
    Connection conn = login();
    try (PreparedStatement statement = conn.prepareStatement(sql)) {
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        System.out.println(Integer.toString(rs.getInt(1)) + " " + rs.getString(2) + " "
            + rs.getString(3) + " " + rs.getString(4) + " " + rs.getString(5) + " "
            + rs.getString(6) + " " + rs.getString(7) + " " + rs.getString(8) + " "
            + rs.getString(9) + " " + rs.getString(10) + " " + rs.getString(11));
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void printProjects() {
    String sql = "SELECT pi_id, project_code FROM projects";
    Connection conn = login();
    try (PreparedStatement statement = conn.prepareStatement(sql)) {
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        int pi_id = rs.getInt("pi_id");
        String first = rs.getString("project_code");
        System.out.println(pi_id + first);
      }
      statement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

}
