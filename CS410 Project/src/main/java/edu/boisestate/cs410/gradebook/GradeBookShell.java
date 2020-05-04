package edu.boisestate.cs410.gradebook;

import com.budhash.cliche.Command;
import com.budhash.cliche.ShellFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    @Command
    public void showClass () throws SQLException {
        String query =
                "SELECT class_id, course_number \n" +
                        "FROM classes\n" +
                        "WHERE class_id = ?;";
        try (PreparedStatement stmt = db.prepareStatement(query)) {
            stmt.setInt(1, active_class_pkey);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.err.format("%d: class does not exist%n", active_class_pkey);
                    return;
                }
                System.out.format("%s %s %n",
                        rs.getInt("class_id"),
                        rs.getString("course_number"));
            }
        }
    }

    // I am not sure if this is all he is looking for in the select
    // Also I don't think the error handling is correct
    @Command
public void selectClass (String course_number) throws SQLException {
    String query =
            "SELECT class_id, course_number,term\n" +
                    "FROM classes\n" +
                    "WHERE course_number = ?;";
    try (PreparedStatement stmt = db.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
            ResultSet.CONCUR_UPDATABLE)) {
        stmt.setString(1, course_number);
        try (ResultSet rs = stmt.executeQuery()) {
            if (!rs.next()) {
                System.err.format("%s: class does not exist%n", course_number);
                return;
            }else {
                String term = rs.getString("term");
                if(isCurrentTerm(term)){
                    System.err.format("Class is not in the current term%n");
                    return;
                }
            }
            rs.last();

            int rowCount = rs.getRow();

                if(rowCount == 1) {
                    active_class_pkey = rs.getInt("class_id");
                    System.out.format("%s %s %s %n",
                            rs.getString("class_id"),
                            rs.getString("course_number"),
                            rs.getString("term"));
                } else {
                    System.err.format("Too many %s classes to select one %n", course_number);
                    return;
                }

        }
    }
}



    public boolean isCurrentTerm(String term){
        String curr_term;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();
        String formatted_date = dateFormat.format(date);
        String month_string = formatted_date.substring(5,7);
        if(month_string.contains("0")){
            month_string = month_string.substring(1,2);
        }
        int month_num = Integer.parseInt(month_string);
        if(month_num <= 5){
            curr_term = "Sp";

        }else if(month_num > 5 && month_num < 8 ){
            curr_term = "Su";
        }else{
           curr_term = "Fa";
        }
        curr_term = curr_term + formatted_date.substring(2,4);
        if(curr_term == term){
            return true;
        }else{
            return false;
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
                if (!rs.next() ) {
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
                if (!rs.next()) {
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
    public void showItems () throws SQLException {
        String query =
                "SELECT course_number, cat_id, cat.name, \n" +
                        "       item_id, i.name as item_name, point_value\n" +
                        "FROM classes c\n" +
                        "join categories cat using (class_id)\n" +
                        "join items i using (cat_id)\n" +
                        "WHERE class_id = ?\n" +
                        "group by cat.name, course_number, item_id, cat_id\n" +
                        "order by cat_id;";
        try (PreparedStatement stmt = db.prepareStatement(query)) {
            stmt.setInt(1, active_class_pkey);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.err.format("%d: class does not exist%n", active_class_pkey);
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
            try (PreparedStatement stmt = db.prepareStatement(selectCatID)) {
                stmt.setString(1,cat_name);
                stmt.setInt(2,active_class_pkey);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        System.err.format("Nothing returned:%s%n");
                        return;
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

    @Command
    public void addStudent (String username, int student_id ,String name) throws SQLException {
        String insertStudent = "Insert into students (student_id,username, name, email_address) values (?,?,?,?)";
        String checkStudent = "select name from students where student_id = ?";
        String updateStudent = "Update students set name = ? where student_id = ?";
        int enroll_id = student_id;
        boolean needsAdded = false;
        boolean needsUpdate = false;
        int new_item_id;
        String ret_name;
        db.setAutoCommit(false);
        try {
            try (PreparedStatement stmt = db.prepareStatement(checkStudent)) {
                stmt.setInt(1,student_id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        needsAdded = true;
                        System.out.format("Adding the student...:%n");
                    }else{
                        ret_name = rs.getString("name");
                        if(ret_name != name){ needsUpdate = true; }
                    }

                }
            }
            if(needsAdded){
                try (PreparedStatement stmt = db.prepareStatement(insertStudent, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, student_id);
                    stmt.setString(2, username);
                    stmt.setString(3, name);
                    String email = username + "@gmail.com";
                    stmt.setString(4, email);
                    stmt.executeUpdate();
                    // fetch the generated class_id
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new RuntimeException("no generated key");
                        }
                        new_item_id = rs.getInt(1);
                        System.out.println("new_item_id: " + new_item_id);
                        System.out.format("Creating item %d%n", new_item_id);
                        enroll_id = new_item_id;
                        //enroll_in_active_class(new_item_id, active_class_pkey);
                    }
                }
            }else if(needsUpdate){
                try (PreparedStatement stmt = db.prepareStatement(updateStudent, Statement.RETURN_GENERATED_KEYS)) {

                    stmt.setString(1, name);
                    stmt.setInt(2, student_id);
                    stmt.executeUpdate();
                    // fetch the generated class_id
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new RuntimeException("Student not updated.%n");
                        }
                        new_item_id = rs.getInt(1);
                        System.out.format("Warning Updated name in student_id %d%n", new_item_id);
                        enroll_id = new_item_id;
                        //enroll_in_active_class(new_item_id, active_class_pkey);
                    }
                }

            }else {

            }

            db.commit();
        } catch (SQLException | RuntimeException e) {
            db.rollback();
            throw e;
        } finally {
            db.setAutoCommit(true);
        }
        enroll_in_active_class(enroll_id,active_class_pkey);
    }
    @Command
    public void addStudent (String username) throws SQLException {
        String checkStudent = "select name,student_id from students where username = ?";
        int new_item_id;
        String ret_name;
        int student_id;
        db.setAutoCommit(false);
        try {
            try (PreparedStatement stmt = db.prepareStatement(checkStudent)) {
                stmt.setString(1,username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new RuntimeException("no student by that username");
                    }else{
                        ret_name = rs.getString("name");
                        student_id = rs.getInt("student_id");
                    }

                }
            }
            db.commit();
        } catch (SQLException | RuntimeException e) {
            db.rollback();
            throw e;
        } finally {
            db.setAutoCommit(true);
        }
        enroll_in_active_class(student_id,active_class_pkey);
    }
    public void enroll_in_active_class (int student_id, int class_id) throws SQLException {
        String enroll = "insert into students_in_classes (student_id,class_id) values (?,?)";
        int s_i_c_id;
        db.setAutoCommit(false);
        try {

            try (PreparedStatement stmt = db.prepareStatement(enroll, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, student_id);
                stmt.setInt(2, active_class_pkey);
                stmt.executeUpdate();
                // fetch the generated class_id
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new RuntimeException("no enrollment id generated");
                    }
                    s_i_c_id = rs.getInt(1);
                    System.out.format("Creating enrollment %d%n", s_i_c_id);
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
    public void grade(String itemname,String username, int grade) throws SQLException{
        boolean needsUpdate = false;
        int[] grades_keys = getExistingGradeIDs(itemname,username);
        int p_value = getPointValue(itemname);
        int grade_id;
        String insertGrade = "insert into grades (item_id, student_id, grade) Values (?, ?,?)";
        String updateGrades = "Update grades set grade = ? where item_id = ? and student_id = ?";

        if(grade > p_value && p_value != -1){
            System.out.println("Nice try, grade value exceeds number of points configured for item: " + p_value);
            return;
        }
        if(grades_keys != null){
            needsUpdate = true;
        }else{
            grades_keys = getNewGradeIDs(itemname,username);
        }

        db.setAutoCommit(false);
        try {
            if(needsUpdate){
                try (PreparedStatement stmt = db.prepareStatement(updateGrades, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, grade);
                    stmt.setInt(2, grades_keys[0]);
                    stmt.setInt(3, grades_keys[1]);
                    stmt.executeUpdate();
                    // fetch the generated class_id
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new RuntimeException("no grade id generated");
                        }
                        grade_id = rs.getInt(1);
                        System.out.format("updated grade: %d%n", grade_id);
                    }
                }
            }else{
                try (PreparedStatement stmt = db.prepareStatement(insertGrade, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, grades_keys[0]);
                    stmt.setInt(2, grades_keys[1]);
                    stmt.setInt(3, grade);
                    stmt.executeUpdate();
                    // fetch the generated class_id
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new RuntimeException("no grade id generated");
                        }
                        grade_id = rs.getInt(1);
                        System.out.format("Creating grade: %d%n", grade_id);
                    }
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

    public int[] getExistingGradeIDs(String itemname, String username) throws SQLException{
        int[] retVal = new int[2];
        String selectItemId = "select g.item_id,g.student_id from grades g\n" +
                "Join items i using(item_id)\n" +
                "Join students s on g.student_id = s.student_id\n" +
                "where i.name = ?\n" +
                "and s.username = ?";
        try (PreparedStatement stmt = db.prepareStatement(selectItemId)) {
            stmt.setString(1, itemname);
            stmt.setString(2, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    //System.err.format("%s: item does not exist%n", itemname );
                    return null;
                }else{
                    retVal[0] = rs.getInt("item_id");
                    retVal[1] = rs.getInt("Student_id");
                    return retVal;
                }

            }
        }
    }

    public int getPointValue(String itemname) throws SQLException{
        String selectPointValue = "select i.point_value from items i\n" +
                "Join categories c on i.cat_id = c.cat_id\n" +
                "where i.name = ?\n" +
                "  and c.class_id = ?";
        try (PreparedStatement stmt = db.prepareStatement(selectPointValue)) {
            stmt.setString(1, itemname);
            stmt.setInt(2, active_class_pkey);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.err.format("point value for item does not exist %n");
                    return -1;
                } else {
                    return rs.getInt("point_value");
                }

            }
        }
    }

    public int[] getNewGradeIDs(String itemname, String username) throws SQLException{
        int[] retVal = new int[2];
        String selectPointValue = "select i.item_id, sic.student_id from items i\n" +
                "Join categories c using(cat_id)\n" +
                "Join students_in_classes sic on c.class_id = sic.class_id\n" +
                "Join students s using(student_id)\n" +
                "where c.class_id = ? and s.username = ? and i.name = ?";
        try (PreparedStatement stmt = db.prepareStatement(selectPointValue)) {
            stmt.setInt(1,active_class_pkey);
            stmt.setString(2, username);
            stmt.setString(3, itemname);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.err.format("No Id's returned %n");
                    return null;
                } else {
                    retVal[0] = rs.getInt("item_id");
                    retVal[1] = rs.getInt("student_id");
                    return retVal;
                }

            }
        }
    }

    @Command
    public void studentGrades(String username) throws SQLException{
        String selectGrades = "select i.name, g.grade from items i\n" +
                "Join grades g using(item_id)\n" +
                "Join students s using (student_id)\n" +
                "Join categories c using(cat_id)\n" +
                "where s.username = ? and c.class_id = ?\n" +
                "group by i.cat_id, i.name,g.grade,c.class_id";
        try (PreparedStatement stmt = db.prepareStatement(selectGrades)) {
            stmt.setString(1, username);
            stmt.setInt(2,active_class_pkey);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.err.format("no grades exist for student:%s %n",username);
                    return;
                } else {
                    System.out.format("%s %d %n",
                            rs.getString("name"),
                            rs.getInt("grade"));

                    while (rs.next()) {
                        System.out.format("%s %d %n",
                                rs.getString("name"),
                                rs.getInt("grade"));
                    }
                }
            }
        }
    }

    @Command
    public void showStudents () throws SQLException {
        String query =
                "SELECT course_number, student_id, name, username, email_address \n" +
                        "FROM students s\n" +
                        "join students_in_classes sic using (student_id)\n" +
                        "join classes c using (class_id)\n" +
                        "WHERE class_id = ?;";
        try (PreparedStatement stmt = db.prepareStatement(query)) {
            stmt.setInt(1, active_class_pkey);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.err.format("%d: class does not exist%n", active_class_pkey);
                    return;
                }
                System.out.format("%s %s %s %s %s %n",
                        rs.getString("course_number"),
                        rs.getInt("student_id"),
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getString("email_address"));
                while (rs.next()) {
                    System.out.format("%s %s %s %s %s %n",
                            rs.getString("course_number"),
                            rs.getInt("student_id"),
                            rs.getString("name"),
                            rs.getString("username"),
                            rs.getString("email_address"));
                }
            }
        }
    }

    @Command
    public void showStudents (String EKS) throws SQLException {
        String query =
                "SELECT course_number, student_id, name, username, email_address\n" +
                        "FROM students\n" +
                        "join students_in_classes sic using (student_id)\n" +
                        "join classes c using (class_id)\n" +
                        "WHERE class_id = ?\n" +
                        "and upper(name) LIKE upper(?) or upper(username) LIKE upper(?);";
        try (PreparedStatement stmt = db.prepareStatement(query)) {
            stmt.setInt(1, active_class_pkey);
            stmt.setString(2, "%" + EKS + "%");
            stmt.setString(3, "%" + EKS + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.err.format("%d: class does not exist%n", active_class_pkey, EKS);
                    return;
                }
                System.out.format("%s %s %s %s %s %n",
                        rs.getString("course_number"),
                        rs.getInt("student_id"),
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getString("email_address"));
                while (rs.next()) {
                    System.out.format("%s %s %s %s %s %n",
                            rs.getString("course_number"),
                            rs.getInt("student_id"),
                            rs.getString("name"),
                            rs.getString("username"),
                            rs.getString("email_address"));
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
