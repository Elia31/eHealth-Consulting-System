package main.java;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


/**
 * The type Database connector.
 */
public class DatabaseConnector
{
    private static DatabaseConnector databaseConnector = null;

    private final String url = "jdbc:mysql://localhost:3306/ehealth";
    private final String user = "<benutzer>";
    private final String password = "<passwort>";

    /**
     * The Connection.
     */
    Connection connection = null;
    /**
     * The Statement.
     */
    Statement statement = null;
    /**
     * The Result set.
     */
    ResultSet resultSet = null;

    private DatabaseConnector()
    {
        try {
            this.connection = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static DatabaseConnector getInstance()
    {
        if (databaseConnector == null) databaseConnector = new DatabaseConnector();
        return databaseConnector;
    }

    protected void finalize() throws Throwable
    {
        try {
            System.out.println("DatabaseConnector Objekt wird von der GC eingesammelt!");
            closeConnection();
        } catch (Throwable e) {
            throw e;
        } finally {
            super.finalize();
        }
    }

    /**
     * Close connection.
     */
    public void closeConnection()
    {
        try {
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets user.
     *
     * @param userID the user id
     * @return the user
     */
    public User getUser(int userID)
    {
        try {
            String sql = "SELECT * FROM user WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, userID);
            resultSet = preparedStatement.executeQuery();
            resultSet.first();

            String firstName = resultSet.getString("firstName");
            String lastName = resultSet.getString("lastName");
            String address = resultSet.getString("address");
            int plz = resultSet.getInt("plz");
            String city = resultSet.getString("city");
            String username = resultSet.getString("username");
            String email = resultSet.getString("email");
            LocalDate dateOfBirth = resultSet.getDate("dateOfBirth").toLocalDate();
            String insuranceName = resultSet.getString("insuranceName");
            boolean privateInsurance = resultSet.getBoolean("privateInsurance");
            String password = resultSet.getString("password");
            String salt = resultSet.getString("salt");

            return new User(firstName, lastName, dateOfBirth, address, plz, city, insuranceName
                    , privateInsurance, username, email, password, salt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Add user.
     *
     * @param user the user
     */
    public void addUser(User user)
    {
        try {
            String sql = "INSERT INTO `user` (`firstname`, `lastname`, `dateOfBirth`, `address`, " +
                    "`plz`, `city`, `username`, `email`, `insuranceName`, `privateInsurance`, `password`, `salt`, `isAdmin`) " +
                    "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, user.getFirstName());
            preparedStatement.setString(2, user.getLastName());
            preparedStatement.setDate(3, Date.valueOf(user.getDateOfBirth()));
            preparedStatement.setString(4, user.getAddress());
            preparedStatement.setInt(5, user.getPlz());
            preparedStatement.setString(6, user.getCity());
            preparedStatement.setString(7, user.getUsername().toLowerCase()); //username is always lowercase
            preparedStatement.setString(8, user.getEmail().toLowerCase()); //email is always lowercase
            preparedStatement.setString(9, user.getInsuranceName());
            preparedStatement.setBoolean(10, user.isPrivateInsurance());
            preparedStatement.setString(11, user.getPassword());
            preparedStatement.setString(12, user.getSalt());
            preparedStatement.setBoolean(13, false);

            preparedStatement.execute();
            nullifyHealthInfo(getID(user.getEmail()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Is username unique boolean.
     *
     * @param username the username
     * @return the boolean
     */
    public boolean isUsernameUnique(String username)
    {
        try {
            String sql = "SELECT username FROM user HAVING username=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);
            resultSet = preparedStatement.executeQuery();
            if (!resultSet.isBeforeFirst()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Is email unique boolean.
     *
     * @param email the email
     * @return the boolean
     */
    public boolean isEmailUnique(String email)
    {
        try {
            String sql = "SELECT email FROM user HAVING email=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, email);
            resultSet = preparedStatement.executeQuery();
            if (!resultSet.isBeforeFirst()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Is login correct boolean.
     *
     * @param login    the login
     * @param password the password
     * @return the boolean
     */
    public boolean isLoginCorrect(String login, String password)
    {
        boolean isEmail = login.contains("@");

        //if User uses Email to login
        if (isEmail) {
            try {
                String sql = "SELECT salt, password FROM user WHERE email=?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                preparedStatement.setString(1, login);
                resultSet = preparedStatement.executeQuery();

                if (!resultSet.isBeforeFirst()) {
                    System.out.println("Email resultSet is Empty");
                    return false;
                }

                resultSet.first();
                String salt = resultSet.getString("salt");
                String databasePassword = resultSet.getString("password");

                //Checks if password is correct
                if (Hash.isCorrect(password, databasePassword, salt)) {
                    return true;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //if User uses username to login
        else {
            try {
                String sql = "SELECT salt, password FROM user WHERE username=?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                preparedStatement.setString(1, login);
                resultSet = preparedStatement.executeQuery();

                if (!resultSet.isBeforeFirst()) {
                    System.out.println("Username resultSet is Empty");
                    return false;
                }

                resultSet.first();
                String salt = resultSet.getString("salt");
                String databasePassword = resultSet.getString("password");

                //Checks if password is correct
                if (Hash.isCorrect(password, databasePassword, salt)) {
                    return true;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Is admin boolean.
     *
     * @param userID the user id
     * @return the boolean
     */
    public boolean isAdmin(int userID)
    {
        try {
            String sql = "SELECT isAdmin FROM user WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, userID);
            resultSet = preparedStatement.executeQuery();
            resultSet.first();
            int isAdmin = resultSet.getInt("isAdmin");
            if (isAdmin == 1)
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Gets id.
     *
     * @param login the login
     * @return the id
     */
    public int getID(String login)
    {
        boolean isEmail = login.contains("@");
        //if User uses Email to login
        if (isEmail) {
            try {
                String sql = "SELECT id FROM user WHERE email=?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                preparedStatement.setString(1, login);
                resultSet = preparedStatement.executeQuery();
                resultSet.first();

                return resultSet.getInt("id");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                String sql = "SELECT id FROM user WHERE username=?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                preparedStatement.setString(1, login);
                resultSet = preparedStatement.executeQuery();
                resultSet.first();

                return resultSet.getInt("id");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * Nullify health info.
     *
     * @param userID the user id
     */
    public void nullifyHealthInfo(int userID)
    {
        try {
            String sql = "INSERT INTO healthInformation (userID, medication, allergies, other, preIllness)" +
                    " VALUES (?,'/','/','/','/')";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, userID);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get health info string [ ].
     *
     * @param userID the user id
     * @return the string [ ]
     */
    public String[] getHealthInfo(int userID)
    {
        try {
            String sql = "SELECT medication, allergies, other, preIllness FROM healthInformation" +
                    " WHERE userID=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, userID);
            resultSet = preparedStatement.executeQuery();
            resultSet.first();

            String medication = resultSet.getString("medication");
            String allergies = resultSet.getString("allergies");
            String other = resultSet.getString("other");
            String preIllness = resultSet.getString("preIllness");

            return new String[]{medication, allergies, other, preIllness};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Edit health info.
     *
     * @param healthInfo the health info
     * @param userID     the user id
     */
    public void editHealthInfo(String[] healthInfo, int userID)
    {
        String medication = healthInfo[0];
        String allergies = healthInfo[1];
        String other = healthInfo[2];
        String preIllness = healthInfo[3];

        try {
            String sql = "UPDATE healthInformation SET " +
                    "medication = ?," +
                    "allergies = ?," +
                    "other = ?," +
                    "preIllness = ?" +
                    " WHERE userID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, medication);
            preparedStatement.setString(2, allergies);
            preparedStatement.setString(3, other);
            preparedStatement.setString(4, preIllness);
            preparedStatement.setInt(5, userID);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets user email.
     *
     * @param userID the user id
     * @return the user email
     */
    public String getUserEmail(int userID)
    {
        try {
            String sql = "SELECT email FROM user" +
                    " WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, userID);
            resultSet = preparedStatement.executeQuery();
            resultSet.first();

            return resultSet.getString("email");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets user plz.
     *
     * @param userID the user id
     * @return the user plz
     */
    public int getUserPlz(int userID)
    {
        try {
            String sql = "SELECT plz FROM user" +
                    " WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, userID);
            resultSet = preparedStatement.executeQuery();
            resultSet.first();

            return resultSet.getInt("plz");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Gets user address.
     *
     * @param userID the user id
     * @return the user address
     */
    public String getUserAddress(int userID)
    {
        try {
            String sql = "SELECT address FROM user" +
                    " WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, userID);
            resultSet = preparedStatement.executeQuery();
            resultSet.first();

            return resultSet.getString("address");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets health problems.
     *
     * @return the health problems
     */
    public List<HealthProblem> getHealthProblems()
    {
        List<HealthProblem> healthProblems = new ArrayList<>();
        try {
            String sql = "SELECT * FROM healthproblem";
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                HealthProblem healthProblem = new HealthProblem(resultSet.getInt("id"),
                        resultSet.getString("name"), resultSet.getString("doctorType"),
                        resultSet.getInt("specialization"));
                healthProblems.add(healthProblem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return healthProblems;
    }

    /**
     * Gets doctors.
     *
     * @param sID the s id
     * @return the doctors
     */
//Doctor int id, String name, String address, int plz, int specializationId
    public List<Doctor> getDoctors(int sID)
    {
        List<Doctor> doctors = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM doctor WHERE specializationId=?");
            preparedStatement.setInt(1, sID);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Doctor doctor = new Doctor(resultSet.getInt("id"), resultSet.getString("name"),
                        resultSet.getString("address"),
                        resultSet.getInt("plz"), resultSet.getInt("specializationId"));
                doctors.add(doctor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doctors;
    }

    /**
     * Gets all doctors.
     *
     * @return the all doctors
     */
//brauchen wir net
    public ArrayList<Doctor> getAllDoctors()
    {
        ArrayList<Doctor> doctors = new ArrayList<Doctor>();
        try {
            String sql = "SELECT * FROM `doctor`";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.execute();

            //Safe result in resultSet by executing the query
            resultSet = preparedStatement.executeQuery();
            if (!resultSet.isBeforeFirst()) {
                System.out.println("No records found");
            } else {
                resultSet.first();
                do {
                    Doctor docTmp = new Doctor(resultSet.getInt(1), resultSet.getString("name"),
                            resultSet.getString("address"), resultSet.getInt("plz"),
                            resultSet.getInt("specializationId"));
                    doctors.add(docTmp);
                } while (resultSet.next());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doctors;
    }

    /**
     * Make appointment.
     *
     * @param appointment the appointment
     */
    public void makeAppointment(Appointment appointment)
    {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `appointment` (`date`,`doctorID`,`userID`) VALUES ( ?, ?, ?)");
            preparedStatement.setTimestamp(1, appointment.getAppointmentDate());
            preparedStatement.setInt(2, appointment.getDoctorID());
            preparedStatement.setInt(3, appointment.getUserID());

            preparedStatement.executeUpdate();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * Cancel appointment.
     *
     * @param id the id
     */
    public void cancelAppointment(int id)
    {
        try {
            String sql = "DELETE FROM `appointment` WHERE id=?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Shift appointment.
     *
     * @param id        the id
     * @param timestamp the timestamp
     */
    public void shiftAppointment(int id, java.sql.Timestamp timestamp)
    {
        try {
            String sql = "UPDATE appointment SET date = ? WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setTimestamp(1, timestamp);
            preparedStatement.setInt(2, id);
            preparedStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets appointments.
     *
     * @param userID the user id
     * @return the appointments
     */
    public List<Appointment> getAppointments(int userID)
    {
        List<Appointment> appointments = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `appointment` WHERE userID=?");
            preparedStatement.setInt(1, userID);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Appointment appointment = new Appointment(resultSet.getTimestamp("date"),
                        resultSet.getInt("userID"), resultSet.getInt("doctorID"));
                appointment.setId(resultSet.getInt("id"));
                appointments.add(appointment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return appointments;
    }

    /**
     * Gets doctor for appointment.
     *
     * @param appointmentID the appointment id
     * @return the doctor for appointment
     */
    public ResultSet getDoctorForAppointment(int appointmentID)
    {
        try {
            String sql = "SELECT doctorID FROM appointment WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, appointmentID);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.first();
            int doctorID = resultSet.getInt("doctorID");

            sql = "SELECT * FROM doctor WHERE id = ?";
            preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, doctorID);
            return preparedStatement.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets doctor type.
     *
     * @param specializationId the specialization id
     * @return the doctor type
     */
    public String getDoctorType(int specializationId)
    {
        try {
            String sql = "SELECT doctorType FROM healthproblem WHERE specialization = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, specializationId);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.first();
            return resultSet.getString("doctorType");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets result set all users.
     *
     * @return the result set all users
     */
//Show all users (Admin)
    public ResultSet getResultSetAllUsers()
    {
        try {
            String sql = "SELECT * FROM user";
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            return statement.executeQuery(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets result set user.
     *
     * @param userID the user id
     * @return the result set user
     */
    public ResultSet getResultSetUser(int userID)
    {
        try {
            String sql = "SELECT * FROM user WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, userID);
            return preparedStatement.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Update profile.
     *
     * @param userID           the user id
     * @param firstName        the first name
     * @param lastName         the last name
     * @param dateOfBirth      the date of birth
     * @param plz              the plz
     * @param address          the address
     * @param city             the city
     * @param insuranceName    the insurance name
     * @param privateInsurance the private insurance
     * @param password         the hashed password, null keeps the current password
     */
    public void updateProfile(int userID, String firstName, String lastName, String dateOfBirth, int plz,
                              String address, String city, String insuranceName, int privateInsurance, String password)
    {
        try {
            String sql = "UPDATE user SET " +
                    "firstname = ?, lastname = ?, dateOfBirth = ?, plz = ?, address = ?, city = ?, " +
                    "insuranceName = ?, privateInsurance = ?" +
                    (password != null ? ", password = ?" : "") +
                    " WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, dateOfBirth);
            preparedStatement.setInt(4, plz);
            preparedStatement.setString(5, address);
            preparedStatement.setString(6, city);
            preparedStatement.setString(7, insuranceName);
            preparedStatement.setInt(8, privateInsurance);
            int index = 9;
            if (password != null) {
                preparedStatement.setString(index++, password);
            }
            preparedStatement.setInt(index, userID);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update user (Admin).
     *
     * @param userID           the user id
     * @param firstName        the first name
     * @param lastName         the last name
     * @param dateOfBirth      the date of birth
     * @param address          the address
     * @param plz              the plz
     * @param city             the city
     * @param username         the username
     * @param email            the email
     * @param insuranceName    the insurance name
     * @param password         the hashed password
     * @param isAdmin          the is admin
     * @param privateInsurance the private insurance
     */
    public void updateUserByAdmin(int userID, String firstName, String lastName, String dateOfBirth, String address,
                                  int plz, String city, String username, String email, String insuranceName,
                                  String password, boolean isAdmin, boolean privateInsurance)
    {
        try {
            String sql = "UPDATE user SET firstName=?, lastName=?, dateOfBirth=?, address=?, plz=?, city=?, " +
                    "username=?, email=?, insuranceName=?, password=?, isAdmin=?, privateInsurance=? WHERE id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, dateOfBirth);
            preparedStatement.setString(4, address);
            preparedStatement.setInt(5, plz);
            preparedStatement.setString(6, city);
            preparedStatement.setString(7, username);
            preparedStatement.setString(8, email);
            preparedStatement.setString(9, insuranceName);
            preparedStatement.setString(10, password);
            preparedStatement.setBoolean(11, isAdmin);
            preparedStatement.setBoolean(12, privateInsurance);
            preparedStatement.setInt(13, userID);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete user (Admin).
     *
     * @param userID the user id
     */
    public void deleteUser(int userID)
    {
        try {
            String sql = "DELETE FROM user WHERE id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, userID);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

