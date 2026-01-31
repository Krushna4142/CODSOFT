public class ATMTheme {

    public static void loading() {
        System.out.print("Loading ATM");
        for (int i = 0; i < 3; i++) {
            try {
                Thread.sleep(500);
                System.out.print(".");
            } catch (InterruptedException e) {}
        }
        System.out.println("\n");
    }

    public static void header() {
        System.out.println("=================================");
        System.out.println("      🏦 SECURE ATM MACHINE      ");
        System.out.println("=================================");
    }

    public static void divider() {
        System.out.println("---------------------------------");
    }
}
