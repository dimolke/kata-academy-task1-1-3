package jm.task.core.jdbc;

import jm.task.core.jdbc.service.UserService;
import jm.task.core.jdbc.service.UserServiceImpl;

public class Main {
    private static final UserService userService = new UserServiceImpl();

    public static void main(String[] args) {
        // реализуйте алгоритм здесь:

//         Из задания https://platform.kata.academy/user/courses/111/1/1/3
//         1. Создание таблицы User(ов)
        createUsersTask1();

//         2. Добавление 4 User(ов) в таблицу с данными на свой выбор.
//         После каждого добавления должен быть вывод в консоль
//         (User с именем — name добавлен в базу данных)
        addUsersTask2();

//        3. Получение всех User из базы и вывод в консоль
//        (должен быть переопределен toString в классе User)
        popUpAllUsersTask3();

//        4. Очистка таблицы User(ов)
        truncateUserTableTask4();

//        5. Удаление таблицы
        dropUserTableTask5();
    }

    private static void createUsersTask1() {
        userService.createUsersTable();
    }

    private static void addUsersTask2() {
        userService.saveUser("Mary", "Kogemaykina", (byte)10 );
        userService.saveUser("Ivana", "Fedora", (byte)30 );
        userService.saveUser("Ioan", "Great", (byte)70 );
        userService.saveUser("Natalya", "Tron", (byte)35 );
    }

    private static void popUpAllUsersTask3() {
        userService.getAllUsers();
    }

    private static void truncateUserTableTask4() {
        userService.cleanUsersTable();
    }

    private static void dropUserTableTask5() {
        userService.dropUsersTable();
    }
}
