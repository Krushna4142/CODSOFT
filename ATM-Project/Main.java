public class Main {

    public static void main(String[] args) {

        // Create user account
        BankAccount account = new BankAccount(10000, 1234);

        // Create ATM
        ATM atm = new ATM(account);

        // Start ATM
        atm.start();
    }
}
