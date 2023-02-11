import java.util.Collections;
import java.util.Scanner;
import java.util.Random;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BattleshipApp {
    public static void main(String[] args) {
        new Game().play();
    }
}

class Game {
    private int numOfTries;

    private int shipsLeft = 3;
    private boolean gameOver;

    final ArrayList<ArrayList> SHIPS = new ArrayList<>();

    static ArrayList<String> bufferZoneLocations = new ArrayList<>();

    String[][] grid = {{}, {}, {}, {}, {}, {}, {}};

    void play() {
        generateThreeShips();
        generateGrid();
        printGrid();

        while(!gameOver) {
            final String userGuess = Helpers.promptUserForGuess();
            final int indexOfShipWhichHasBeenHit = whichShipHasBeenHit(userGuess); // If no ship was hit, returns -1.
            updateGameAndShipInformation(userGuess, indexOfShipWhichHasBeenHit);
            printResult(fetchResponseMessage(indexOfShipWhichHasBeenHit));;
        }
    }

    void generateThreeShips() {
        for (int i = 0; i < 3; ++i) {
            ArrayList ship = Ship.generateShip();
            if(!isShipAllowed(ship)) {
                i-= 1;
            } else {
                SHIPS.add(ship);
                generateBufferZone(ship);
            }
        }
    }

    boolean isShipAllowed(ArrayList ship) {
        for (int i = 0; i < Ship.SHIP_SIZE; ++i) {
            if (bufferZoneLocations.contains(ship.get(i))) {
                return false;
            }
        }
        return true;
    }

    void generateBufferZone(ArrayList<String> ship) {
        for (int i = 0; i < Ship.SHIP_SIZE; ++i) {
            String currentLocation = ship.get(i);
            char letter = currentLocation.charAt(0);
            int number = Character.getNumericValue(currentLocation.charAt(1));
            bufferZoneLocations.addAll(calculateBufferZoneForLocation(letter, number));
        }
    }

    static ArrayList<String> calculateBufferZoneForLocation(char letter, int number) {
        ArrayList<String> result = new ArrayList<>();
        Collections.addAll(result,
                Helpers.concatenateLetterAndNumber(letter, number - 1),
                Helpers.concatenateLetterAndNumber((char) (letter - 1), number - 1),
                Helpers.concatenateLetterAndNumber((char) (letter - 1), number),
                Helpers.concatenateLetterAndNumber((char) (letter - 1), number + 1),
                Helpers.concatenateLetterAndNumber(letter, number + 1),
                Helpers.concatenateLetterAndNumber((char) (letter + 1), number + 1),
                Helpers.concatenateLetterAndNumber((char) (letter + 1), number),
                Helpers.concatenateLetterAndNumber((char) (letter + 1), number - 1));
        return result;
    };

    void generateGrid() {
        for(int i = 0; i < 7; ++i) {
            grid[i] = new String[7];
            for (int j = 0; j < 7; ++j) {
                grid[i][j] = "\t☐";
            }
        }
    }

    void removeHitShipLocation(String locationHitByUser, int indexOfShip) {
        SHIPS.get(indexOfShip).remove(locationHitByUser);
    }

    int whichShipHasBeenHit(String userGuess) {
        for (int i = 0; i < shipsLeft; ++i) {
            if (SHIPS.get(i).contains(userGuess)) {
                return i;
            }
        }
        return -1; // If no ship was hit, returns -1.
    }

    void updateGameAndShipInformation(String userGuess, int indexOfShip) {
        numOfTries++;

        int row = userGuess.charAt(0) - 'A';
        int cell = Character.getNumericValue(userGuess.charAt(1));

        if (indexOfShip == -1) { // No ship was hit.
            grid[row][cell] = "\t";
        } else { // One of the ships was hit.
            grid[row][cell] = "\t▧";
            removeHitShipLocation(userGuess, indexOfShip);

            if (SHIPS.get(indexOfShip).isEmpty()) {
                shipsLeft-= 1;
                SHIPS.remove(indexOfShip);

                if(SHIPS.isEmpty()) {
                    gameOver = true;
                }
            }
        }
    }

    String fetchResponseMessage(int indexOfShip) {
        if(indexOfShip != -1) {
            if(gameOver) {
                return String.format("Game over! \nYou needed %s shots to sink them all!", numOfTries);
            } else {
                return String.format("Hit! There are still %d ships left", shipsLeft);
            }
        } else {
            return "Miss!";
        }
    }

    void printResult(String message) {
        System.out.println(message);
        printGrid();
    }

    void printGrid() {
        System.out.print(" ");
        for (int n = 0; n < 7; ++n) {
            System.out.print("\t" + n);
        }
        System.out.println();
        for (int i = 0; i < 7; ++i) {
            System.out.print((char) ('A' + i));
            for(int j = 0; j < 7; ++j) {
                System.out.print(grid[i][j]);
            }
            System.out.println();
        }
    }
}

class Ship {
    static final int SHIP_SIZE = 3;
    static final int UPPER_BOUND = 5;

    static int generateFirstNumber() {
        return ThreadLocalRandom.current().nextInt(0, UPPER_BOUND);
    }

    static char generateFirstLetter() {
        return (char) ThreadLocalRandom.current().nextInt('A', 'E' + 1);
    }

    static boolean willShipBeHorizontal(){
        return new Random().nextBoolean();
    }

    static ArrayList<String> generateShip() {
        char firstLetter = generateFirstLetter();
        int firstNumber = generateFirstNumber();
        return generateOneShip(firstLetter, firstNumber, willShipBeHorizontal());
    }

    static ArrayList<String> generateOneShip(char firstLetter, int firstNumber, boolean horizontal) {
        ArrayList<String> ship = new ArrayList<String>();
        for (int i = 0; i < SHIP_SIZE; ++i) {
            String location;
            if(horizontal) {
                location = Helpers.concatenateLetterAndNumber(firstLetter, firstNumber + i);
            } else {
                location = Helpers.concatenateLetterAndNumber((char) (firstLetter + i), firstNumber);
            }
            ship.add(location);
        }
        return ship;
    }
}

class Helpers {
    static String concatenateLetterAndNumber(char letter, int number) {
        return String.valueOf(letter) + number;
    }

    static String promptUserForGuess() {
        System.out.print("Your guess:\n");
        Scanner scanner = new Scanner(System.in);
        String userGuess = scanner.nextLine().toUpperCase();
        while(!validateUserInput(userGuess)) {
            System.out.println("Invalid guess!\nYour guess: ");
            userGuess = scanner.nextLine();
        }
        return userGuess;
    }

    static boolean validateUserInput(String userInput) {
        Pattern pattern = Pattern.compile("^[A-G][0-6]$");
        Matcher matcher = pattern.matcher(userInput);
        return matcher.find();
    }
}
