import java.util.Random;
import java.util.Scanner;

public class NumberGame {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Random random = new Random();

        boolean playAgain = true;

        while (playAgain) {
            int number = random.nextInt(100) + 1; // 1 to 100
            int guess = 0;
            int attempts = 0;
            int maxAttempts = 10;

            System.out.println("======================================");
            System.out.println("        WELCOME TO NUMBER GAME        ");
            System.out.println("======================================");
            System.out.println("I have picked a number between 1 and 100.");
            System.out.println("You have " + maxAttempts + " attempts.");
            System.out.println("Let’s begin!\n");

            while (guess != number && attempts < maxAttempts) {
                System.out.print("Enter your guess (1-100): ");
                guess = sc.nextInt();

                // Input validation
                if (guess < 1 || guess > 100) {
                    System.out.println(" [X] Invalid input! Enter 1 to 100 only.\n");
                    continue;
                }

                attempts++;
                int diff = Math.abs(guess - number);

                if (guess > number) {
                    System.out.print(" >>> Too HIGH! ");
                } else if (guess < number) {
                    System.out.print(" <<< Too LOW! ");
                }

                // Proximity hints
                if (diff == 0) {
                    System.out.println("\n======================================");
                    System.out.println(" 🎉 CONGRATULATIONS 🎉");
                    System.out.println(" You guessed the number in " + attempts + " attempts.");
                    System.out.println("======================================");
                } else if (diff <= 2) {
                    System.out.println("*** VERY CLOSE! Just a step away! ***\n");
                } else if (diff <= 5) {
                    System.out.println("--> Getting CLOSER! Keep trying!\n");
                } else if (diff <= 10) {
                    System.out.println("~ You’re warming up. ~\n");
                } else {
                    System.out.println("... Way OFF! Try again ...\n");
                }
            }

            if (guess != number) {
                System.out.println("\n======================================");
                System.out.println(" ❌ GAME OVER!");
                System.out.println(" The correct number was: " + number);
                System.out.println("======================================");
            }

            // Replay option
            System.out.print("\nDo you want to play again? (yes/no): ");
            String choice = sc.next().toLowerCase();
            playAgain = choice.equals("yes");
            System.out.println();
        }

        System.out.println("Thanks for playing. See you next time! 👋");
        sc.close();
    }
}
