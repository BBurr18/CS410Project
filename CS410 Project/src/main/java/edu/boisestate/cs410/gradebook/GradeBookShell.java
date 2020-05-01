package edu.boisestate.cs410.gradebook;

import com.budhash.cliche.Command;
import com.budhash.cliche.ShellFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;

public class GradeBookShell {
    private final Connection db;

    public GradeBookShell(Connection cxn) {
        db = cxn;
    }
    // I am not sure if this is all he is looking for in the select
    // Also I don't think the error handling is correct
    @Command
    public void classes (String course_number) throws SQLException {
        String query =
                "SELECT class_id, course_number\n" +
                        "FROM classes\n" +
                        "WHERE course_number = ?;";
        try (PreparedStatement stmt = db.prepareStatement(query)) {
            stmt.setString(1, course_number);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.err.format("%d: class does not exist%n", course_number);
                    return;
                }
                System.out.format("%s %s%n",
                        rs.getString("class_id"),
                        rs.getString("course_number"));
            }
        }
    }

    @Command
    public void classes (String course_number, String term) throws SQLException {
        String query =
                "SELECT class_id, course_number, term\n" +
                        "FROM classes\n" +
                        "WHERE course_number = ?" +
                        "and term = ?;";
        try (PreparedStatement stmt = db.prepareStatement(query)) {
            stmt.setString(1, course_number);
            stmt.setString(2, term);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.err.format("%d: class does not exist%n", course_number);
                    return;
                }
                System.out.format("%s %s %s %n",
                        rs.getString("class_id"),
                        rs.getString("course_number"),
                        rs.getString("term"));
            }
        }
    }

    @Command
    public void classes (String course_number, String term, int section_number) throws SQLException {
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
                if (!rs.next()) {
                    System.err.format("%d: class does not exist%n", course_number);
                    return;
                }
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
    public void items (String course_number) throws SQLException {
        String query =
                "SELECT course_number, cat_id, cat.name, \n" +
                        "       item_id, i.name, point_value\n" +
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
                        rs.getString("name"),
                        rs.getInt("point_value"));
                while (rs.next()) {
                    System.out.format("%s %s %s %s %s %d %n",
                            rs.getString("course_number"),
                            rs.getInt("cat_id"),
                            rs.getString("name"),
                            rs.getInt("item_id"),
                            rs.getString("name"),
                            rs.getInt("point_value"));
                }
            }
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
