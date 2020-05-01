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

    @Command
    public void select (String course_number) throws SQLException {
        String query =
                "SELECT class_id, course_number,\n" +
                        " term, section_number\n" +
                        "FROM classes\n" +
                        "WHERE course_number = ?;";
        try (PreparedStatement stmt = db.prepareStatement(query)) {
            stmt.setString(1, course_number);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.err.format("%d: class does not exist%n", course_number);
                    return;
                }
                System.out.format("%s %s %s %s%n",
                        rs.getString("class_id"),
                        rs.getString("course_number"),
                        rs.getString("term"),
                        rs.getString("section_number"));
            }
        }
    }

//    @Command
//    public void echo(String... args) {
//        for (int i = 0; i < args.length; i++) {
//            if (i > 0) {
//                System.out.print(' ');
//            }
//            System.out.print(args[i]);
//        }
//        System.out.println();
//    }

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
