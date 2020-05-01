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
