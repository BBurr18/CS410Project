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
    public void select (String course_number) throws SQLException {
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
    public void select (String course_number, String term) throws SQLException {
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
    public void select (String course_number, String term, int section_number) throws SQLException {
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
