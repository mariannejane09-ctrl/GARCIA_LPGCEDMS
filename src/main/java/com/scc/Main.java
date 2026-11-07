package com.scc;
import java.util.Scanner;
import com.scc.dao.DatabaseInitializer;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;
import com.scc.dao.CylinderDao;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static void main(String[] args){

        DatabaseInitializer.initialize();

        CylinderDao cylinderDao = new CylinderDao();
        cylinderDao.initializeInventory();

        Scanner scanner = new Scanner(System.in);
        UserDao userDao = new UserDao();

        Authentication auth = new Authentication(userDao, scanner);
        auth.start();
        scanner.close();
    }


}
