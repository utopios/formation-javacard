package com.example.demoservletapplet;

import java.io.*;
import javax.servlet.http.*;

public class HelloServlet extends HttpServlet {

    private short counter = 0;


    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
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