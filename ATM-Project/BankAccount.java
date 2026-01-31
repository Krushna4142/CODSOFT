import java.util.ArrayList;

public class BankAccount {

    private double balance;
    private int pin;
    private int dailyWithdrawn = 0;
    private final int DAILY_LIMIT = 20000;

    private ArrayList<Transaction> transactions = new ArrayList<>();

    public BankAccount(double balance, int pin) {
        this.balance = balance;
        this.pin = pin;
    }

    public boolean validatePin(int enteredPin) {
        return pin == enteredPin;
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            System.out.println("❌ Invalid deposit amount.");
            return;
        }
        balance += amount;
        transactions.add(new Transaction("Deposit", amount));
        System.out.println("✅ Deposit successful.");
    }

    public void withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("❌ Invalid amount.");
        } else if (amount > balance) {
            System.out.println("❌ Insufficient balance.");
        } else if (dailyWithdrawn + amount > DAILY_LIMIT) {
            System.out.println("❌ Daily limit exceeded (₹" + DAILY_LIMIT + ").");
        } else {
            balance -= amount;
            dailyWithdrawn += amount;
            transactions.add(new Transaction("Withdraw", amount));
            System.out.println("✅ Collect your cash.");
        }
    }

    public double getBalance() {
        return balance;
    }

    // Mini statement (last 5)
    public void miniStatement() {
        System.out.println("\n📄 MINI STATEMENT:");
        int start = Math.max(0, transactions.size() - 5);
        for (int i = start; i < transactions.size(); i++) {
            System.out.println(transactions.get(i));
        }
    }
}
