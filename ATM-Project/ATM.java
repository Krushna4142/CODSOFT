import java.util.Scanner;

public class ATM {

    private BankAccount account;
    private Scanner scanner = new Scanner(System.in);

    public ATM(BankAccount account) {
        this.account = account;
    }

    private boolean authenticate() {
        int attempts = 0;

        while (attempts < 3) {
            System.out.print("🔐 Enter PIN: ");

            if (!scanner.hasNextInt()) {
                System.out.println("❌ PIN must be numeric.");
                scanner.next();
                attempts++;
                continue;
            }

            if (account.validatePin(scanner.nextInt())) {
                System.out.println("✅ Login successful.");
                return true;
            } else {
                System.out.println("❌ Incorrect PIN.");
                attempts++;
            }
        }

        System.out.println("🚫 Account locked due to 3 failed attempts.");
        return false;
    }

    public void start() {

        ATMTheme.header();
        ATMTheme.loading();

        if (!authenticate()) return;

       int choice = 0;

        do {
            ATMTheme.divider();
            System.out.println("1. Withdraw");
            System.out.println("2. Deposit");
            System.out.println("3. Check Balance");
            System.out.println("4. Mini Statement");
            System.out.println("5. Logout");
            System.out.print("Choose option: ");

            if (!scanner.hasNextInt()) {
                System.out.println("❌ Enter valid number.");
                scanner.next();
                continue;
            }

            choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.print("Amount: ₹");
                    account.withdraw(scanner.nextDouble());
                    break;

                case 2:
                    System.out.print("Amount: ₹");
                    account.deposit(scanner.nextDouble());
                    break;

                case 3:
                    System.out.println("💰 Balance: ₹" + account.getBalance());
                    break;

                case 4:
                    account.miniStatement();
                    break;

                case 5:
                    System.out.println("👋 Logged out successfully.");
                    break;

                default:
                    System.out.println("❌ Invalid choice.");
            }
        } while (choice != 5);
    }
}
