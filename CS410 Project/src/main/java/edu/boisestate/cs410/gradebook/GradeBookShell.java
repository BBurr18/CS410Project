package edu.boisestate.cs410.gradebook;

import com.budhash.cliche.Command;
import com.budhash.cliche.ShellFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;

public class GradeBookShell {
    private final Connection db;
    private String term = "Sp20";
    private int active_class_pkey;
    public GradeBookShell(Connection cxn) {
        db = cxn;
    }
    // I am not sure if this is all he is looking for in the select
    // Also I don't think the error handling is correct
    @Command
    public void selectClass (String course_number) throws SQLException {
        String query =
                "SELECT class_id, course_number\n" +
                        "FROM classes\n" +
                        "WHERE course_number = ?;";
        try (PreparedStatement stmt = db.prepareStatement(query)) {
            stmt.setString(1, course_number);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next() || rs.getRow() > 1) {
                    System.err.format("%d: class does not exist%n", course_number);
                    return;
                }
                active_class_pkey = rs.getInt("class_id");
                System.out.format("%s %s%n",
                        rs.getString("class_id"),
                        rs.getString("course_number"));
            }
        }
    }

    @Command
    public void selectClass (String course_number, String term) throws SQLException {

        String query =
                "SELECT class_id, course_number, term\n" +
                        "FROM classes\n" +
                        "WHERE course_number = ?" +
                        "and term = ?;";
        try (PreparedStatement stmt = db.prepareStatement(query)) {
            stmt.setString(1, course_number);
            stmt.setString(2, term);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next() || rs.getRow() > 1) {
                    System.err.format("%d: class does not exist%n", course_number);
                    return;
                }
                active_class_pkey = rs.getInt("class_id");
                System.out.format("%s %s %s %n",
                        rs.getString("class_id"),
                        rs.getString("course_number"),
                        rs.getString("term"));
            }
        }
    }

    @Command
    public void selectClass (String course_number, String term, int section_number) throws SQLException {

        String query =
                "SELECT class_id, course_number, term, section_number\n" +
                        "FROM classes\n" +
                        "WHERE course_number = ?" +
                        "and term = ?" +
                        "and section_number = ?;";
        try (PreparedStatement stmt = db.prepareStatement(query)) {
            stmt.setString(1, course_number);
            stmt.setString(2, term);
            stmt.setInt(3, section_number);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next() || rs.getRow() >1) {
                    System.err.format("%s: class does not exist%n", course_number);
                    return;
                }
                active_class_pkey = rs.getInt("class_id");
                System.out.format("%s %s %s %s %n",
                        rs.getString("class_id"),
                        rs.getString("course_number"),
                        rs.getString("term"),
                        rs.getString("section_number"));
            }
        }
    }
    // I don't know why the while loop was't getting the first category I will
    //try to fix it
    @Command
    public void categories (String course_number) throws SQLException {
        String query =
                "SELECT class_id, course_number, \n" +

                        "cat_id, name, weight\n" +
                        "FROM classes\n" +
                        "join categories using (class_id)\n" +
                        "WHERE course_number = ?;";
        try (PreparedStatement stmt = db.prepareStatement(query)) {
            stmt.setString(1, course_number);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.err.format("%d: class does not exist%n", course_number);
                    return;
                }
                System.out.format("%s %s %s %s %.2f %n",
                        rs.getInt("class_id"),
                        rs.getString("course_number"),
                        rs.getInt("cat_id"),
                        rs.getString("name"),
                        rs.getFloat("weight"));
                while (rs.next()) {
                    System.out.format("%s %s %s %s %.2f %n",
                            rs.getInt("class_id"),
                            rs.getString("course_number"),
                            rs.getInt("cat_id"),
                            rs.getString("name"),
                            rs.getFloat("weight"));
                }
            }
        }
    }


    @Command
    public void showItems (String course_number) throws SQLException {
        String query =
                "SELECT course_number, cat_id, cat.name, \n" +
                        "       item_id, i.name as item_name, point_value\n" +
                        "FROM classes c\n" +
                        "join categories cat using (class_id)\n" +
                        "join items i using (cat_id)\n" +
                        "WHERE course_number = ?\n" +
                        "group by cat.name, course_number, item_id, cat_id\n" +
                        "order by cat_id;";
        try (PreparedStatement stmt = db.prepareStatement(query)) {
            stmt.setString(1, course_number);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.err.format("%d: class does not exist%n", course_number);
                    return;
                }
                System.out.format("%s %s %s %s %s %d %n",
                        rs.getString("course_number"),
                        rs.getInt("cat_id"),
                        rs.getString("name"),
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getInt("point_value"));
                while (rs.next()) {
                    System.out.format("%s %s %s %s %s %d %n",
                            rs.getString("course_number"),
                            rs.getInt("cat_id"),
                            rs.getString("name"),
                            rs.getInt("item_id"),
                            rs.getString("item_name"),
                            rs.getInt("point_value"));
                }
            }
        }
    }
    //new-class command, also newClass = new-class in the run window when executing the command, dont ask me why
   @Command
    public void newClass (String course_num, String term, int section_num,String description) throws SQLException {
        String insertClass = "INSERT INTO classes (course_number, term,section_number,description) VALUES (?, ?,?,?)";
        int class_id;
        db.setAutoCommit(false);
        try {
            try (PreparedStatement stmt = db.prepareStatement(insertClass, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, course_num);
                stmt.setString(2, term);
                stmt.setInt(3, section_num);
                stmt.setString(4, description);
                stmt.executeUpdate();
                // fetch the generated class_id
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new RuntimeException("no generated key");
                    }
                    class_id = rs.getInt(1);
                    System.out.format("Creating class %d%n", class_id);
                }
            }
            db.commit();
        } catch (SQLException | RuntimeException e) {
            db.rollback();
            throw e;
        } finally {
            db.setAutoCommit(true);
        }
    }
    //usage: grades> list-classes
    @Command
    public void listClasses () throws SQLException {
        String query =
                "select course_number, count(student_id) as num_students from classes c\n" +
                        "join students_in_classes sic on sic.class_id = c.class_id\n" +
                        "group by c.class_id";
        try (PreparedStatement stmt = db.prepareStatement(query)) {

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.err.format("There are no Students in any classes%n");
                    return;
                }
                System.out.format("%s %d %n",
                        rs.getString("course_number"),
                        rs.getInt("num_students"));

                while (rs.next()) {
                    System.out.format("%s %d %n",
                            rs.getString("course_number"),
                            rs.getInt("num_students"));
                }
            }
        }
    }

    @Command
    public void showCategories () throws SQLException {
        String query =
                "SELECT  course_number,name, weight\n" +
                        " FROM classes\n" +
                        "join categories using (class_id)\n" +
                        "WHERE term = ?;";
        try (PreparedStatement stmt = db.prepareStatement(query)) {
            stmt.setString(1, term);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.err.format("No classes for this term:%s%n", term);
                    return;
                }
                System.out.format("%s %s %.2f %n",
                        rs.getString("course_number"),
                        rs.getString("name"),
                        rs.getFloat("weight"));
                while (rs.next()) {
                    System.out.format("%s %s %.2f %n",
                            rs.getString("course_number"),
                            rs.getString("name"),
                            rs.getFloat("weight"));
                }
            }
        }
    }

    @Command
    public void addCategory (String name, int weight,int class_id) throws SQLException {
        String insertCategory = "INSERT INTO categories (name, weight,class_id) VALUES (?, ?,?)";
        int cat_id;
        db.setAutoCommit(false);
        try {
            try (PreparedStatement stmt = db.prepareStatement(insertCategory, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, name);
                stmt.setInt(2, weight);
                stmt.setInt(3, active_class_pkey);
                stmt.executeUpdate();
                // fetch the generated class_id
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new RuntimeException("no generated key");
                    }
                    cat_id = rs.getInt(1);
                    System.out.format("Creating category %d%n", cat_id);
                }
            }
            db.commit();
        } catch (SQLException | RuntimeException e) {
            db.rollback();
            throw e;
        } finally {
            db.setAutoCommit(true);
        }
    }

    @Command
    public void addItems (String name, String cat_name ,String description,int point_value) throws SQLException {
        String insertItem = "INSERT INTO items (name, description,point_value,cat_id) VALUES (?, ?,?,?)";
        String selectCatID = "select cat_id from categories where name = ? and class_id = ?";
        int new_item_id;
        int ret_cat_id;
        db.setAutoCommit(false);
        try {
            try (PreparedStatement stmt = db.prepareStatement(selectCatID, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, cat_name);
                stmt.setInt(2, active_class_pkey);
                stmt.executeUpdate();
                // fetch the generated class_id
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new RuntimeException("no cat_id returned");
                    }
                   ret_cat_id = rs.getInt("cat_id");
                }
            }

            try (PreparedStatement stmt = db.prepareStatement(insertItem, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, name);
                stmt.setString(2, description);
                stmt.setInt(3, point_value);
                stmt.setInt(4, ret_cat_id);
                stmt.executeUpdate();
                // fetch the generated class_id
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new RuntimeException("no generated key");
                    }
                    new_item_id = rs.getInt(1);
                    System.out.format("Creating item %d%n", new_item_id);
                }
            }
            db.commit();
        } catch (SQLException | RuntimeException e) {
            db.rollback();
            throw e;
        } finally {
            db.setAutoCommit(true);
        }
    }

    public static void main(String[] args) throws IOException, SQLException {
        // First (and only) command line argument: database URL
        String dbUrl = args[0];
        try (Connection cxn = DriverManager.getConnection("jdbc:" + dbUrl)) {
            GradeBookShell shell = new GradeBookShell(cxn);
            ShellFactory.createConsoleShell("grades", "", shell)
                    .commandLoop();
        }
    }
}
