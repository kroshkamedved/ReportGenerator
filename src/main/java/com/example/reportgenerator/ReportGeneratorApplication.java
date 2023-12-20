package com.example.reportgenerator;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class ReportGeneratorApplication {

    public static void main(String[] args) throws IOException, InvocationTargetException, IllegalAccessException {
        // SpringApplication.run(ReportGeneratorApplication.class, args);
        List<User> users = new ArrayList<>();
        users.add(new User(1, "Dovbeny", "noPass", 999));
        users.add(new User(2, "Krasunchyk", "yesPass", 9999));


        TableGenerator<User> tableGenerator = new TableGenerator<>(users, User.class);
        tableGenerator.createTable("tableTest.pdf", PDRectangle.A4, 14);
    }

}
