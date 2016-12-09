/**
 * Created by thorstenjaeckel on 12/8/16.
 */

import java.util.Scanner;

public class loan_calc {

    public static void main(String[] args) {
        Scanner reader = new Scanner(System.in);
        System.out.println("Principal Amount: ");
        double principal = reader.nextDouble();

        System.out.println("Number of Payments: ");
        int n = reader.nextInt();

        System.out.println("Annual Interest Rate (%): ");
        double annual_rate = reader.nextDouble();
        double r = annual_rate / 12 / 100;

        System.out.println("Calculating monthly payment for loan with $" + principal + " principal, " + n + " payments and a " + annual_rate + "% interest rate:");

        double payment = (r * principal) / (1 - Math.pow(1+r, -n));
        double interest = payment * n - principal;

        System.out.println("Monthly Payment: $" + payment);
        System.out.println("Total Interest: $" + interest);

    }

}
