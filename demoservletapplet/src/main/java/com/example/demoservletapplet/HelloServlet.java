package com.example.demoservletapplet;

import java.io.*;
import javax.servlet.http.*;

public class HelloServlet extends HttpServlet {

    private short counter = 0;

    public void init() {

    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        String stringAmount = request.getParameter("amount");
        try {
            short amount = Short.parseShort(stringAmount);
        }catch (NumberFormatException e) {

        }
        switch (action) {
            case "increment":
                counter++;
                response.setContentType("text/plain");
                response.getWriter().write("content : "+ counter);
                break;
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {


    }

    public void destroy() {
    }
}