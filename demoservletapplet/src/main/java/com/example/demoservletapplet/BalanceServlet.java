package com.example.demoservletapplet;

import javax.servlet.http.*;
import java.io.IOException;

public class BalanceServlet extends HttpServlet {
    private short balance = 0;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        response.getWriter().write("Current balance: " + balance);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        short amount = Short.parseShort(request.getParameter("amount"));

        if ("add".equalsIgnoreCase(action)) {
            balance += amount;
        } else if ("withdraw".equalsIgnoreCase(action)) {
            if (balance >= amount) {
                balance -= amount;
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Error: Insufficient balance.");
                return;
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid action. Use 'add' or 'withdraw'.");
            return;
        }

        response.setContentType("text/plain");
        response.getWriter().write("New balance: " + balance);
    }

}