package jm.task.core.jdbc.dao;

import jm.task.core.jdbc.model.User;
import jm.task.core.jdbc.util.Util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDaoJDBCImpl implements UserDao {

    //declare singleton UserDao
    private static UserDao INSTANCE = null;

    //ddl
    private static final String CREATE_SQL = """
            CREATE TABLE IF NOT EXISTS users(
                id BIGINT UNSIGNED
                AUTO_INCREMENT PRIMARY KEY NOT NULL,
                name VARCHAR(100) NULL,
                lastname VARCHAR(100) NULL,
                age TINYINT NULL)
                CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
            """;

    //ddl
    private static final String DROP_SQL = """
            DROP TABLE IF EXISTS users;
            """;

    //dml
    private static final String SAVE_SQL = """
            INSERT INTO users(name, 
                              lastname, 
                              age) 
            VALUES (?, ?, ?);
            """;

    //dml
    private static final String DELETE_SQL = """
            DELETE FROM users
            WHERE id = ?
            """;
    // dml
    private static final String FIND_ALL_SQL = """
                    SELECT id,
                           name,
                           lastname,
                           age
                    FROM users
            """;
    // dml
    private static final String TRUNCATE_SQL = """
            TRUNCATE users
    """;

    // for singleton make private
//    public UserDaoJDBCImpl() {
//    }
    private UserDaoJDBCImpl() {
    }

    public void createUsersTable() {
        try (var connection = Util.getConnection();
             var statement = connection.createStatement();) {
            statement.execute(CREATE_SQL);
        } catch (SQLException throwable) {
            System.out.println("SQLException: " + throwable.getMessage());
        }
    }

    public void dropUsersTable() {
        try (var connection = Util.getConnection();
             var statement = connection.createStatement()) {
            statement.execute(DROP_SQL);
        } catch (SQLException throwable) {
            System.out.println("SQLException: " + throwable.getMessage());
        }
    }

    public void saveUser(String name, String lastName, byte age) {
        try (var connection = Util.getConnection();
             var preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, name);
            preparedStatement.setString(2, lastName);
            preparedStatement.setByte(3, age);

            preparedStatement.executeUpdate();

        } catch (SQLException throwable) {
            throw new DaoException(throwable);
        }
    }

    public void removeUserById(long id) {
        try (var connection = Util.getConnection();
        var preparedStatement = connection.prepareStatement(DELETE_SQL)){
            preparedStatement.setLong(1, id);
        } catch (SQLException throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public List<User> getAllUsers() {
        try (var connection = Util.getConnection();
             var preparedStatement = connection.prepareStatement(FIND_ALL_SQL)) {
            var resultSet = preparedStatement.executeQuery();

            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(buildUser(resultSet));
            }
            return users;

        } catch (SQLException throwable) {
            throw new DaoException(throwable);
        }
    }

    private static User buildUser(ResultSet resultSet) throws SQLException {
        User user = new User(
                resultSet.getString("name"),
                resultSet.getString("lastname"),
                resultSet.getByte("age")
        );
        user.setId(resultSet.getLong("id"));
        return user;
    }

    public void cleanUsersTable() {
        try (var connection = Util.getConnection();
            var statement = connection.createStatement()) {
            statement.execute(TRUNCATE_SQL);
        } catch (Exception throwable) {
            throw new RuntimeException(throwable);
        }
    }

    // create singleton UserDao
    public static UserDao getInstance() {
        if (INSTANCE == null) {
            synchronized (UserDaoJDBCImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UserDaoJDBCImpl();
                }
            }
        }
        return INSTANCE;
    }
}
