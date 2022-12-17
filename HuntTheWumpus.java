import java.util.Scanner;

public class HuntTheWumpus {

    public static int collisionType = 0;
    public static boolean gameOn = true;
    static final Scanner sc = new Scanner(System.in);
    static char playAgainAnswer = 'N';
    static boolean playAgain = true;
    static boolean arrowHit = false;
    static boolean debugOn = false;

    public static void main(String[] args) {
        System.out.println("Welcome to Hunt The Wumpus!");

        runMenuOptions();

    }

    public static void runMenuOptions() {

        GameManager gm = new GameManager();
        Player player = new Player(gm.generateCoord(), gm.generateCoord());

        boolean runMenu = true;
        char menuAnswer = '0';

        do {
            System.out.println();
            System.out.println("What would you like to do?");
            System.out.println("[Play game (1)] [Instructions (2)] [Quit (3)] [Debug mode (4)]");

            try {
                menuAnswer = sc.next().charAt(0);
            } catch(Exception e) {
                e.printStackTrace();
                menuAnswer = '0';
                System.out.println("Please only use numbers!");
            }

            if(menuAnswer == '1' || menuAnswer == '3' || menuAnswer == '4') {
                runMenu = false;
            } else if (menuAnswer == '2') {
                gm.showInstructions();
            }

            System.out.println();

        } while (runMenu);

        if(menuAnswer == '1') {
            playGame(gm, player, false);
        } else if(menuAnswer == '4') {
            playGame(gm, player, true);
        } else {

            System.out.println("Thank you! Good bye!");
        }

        sc.close();

    }

    public static void playGame(GameManager gm, Player player, boolean debugOn) {

        Wumpus wumpus = new Wumpus(gm.generateCoord(), gm.generateCoord());

        char dir = 'N';
        char arrowDir = 'N';

        playAgain = true;

        //Play again loop (reset certain game values here)
        while(playAgain) {

            //Reset all values here before new game
            //Things that are generated at the beginning of playGame()
            //Player new start pos
            //Wumpus new start pos
            gameOn = true;
            arrowHit = false;
            player.setPlayerStartArrows(3);

            player.setPlayerSpot(gm.generateCoord(), gm.generateCoord());
            wumpus.setWumpusStartPosition(gm.generateCoord(), gm.generateCoord());

            gm.gameMap = gm.generateMap(player.getPlayerYCoordinate(), player.getPlayerXCoordinate(), wumpus);
            System.out.println("You start at: " + player.getPlayerYCoordinate() +":" + player.getPlayerXCoordinate());

            //Gameplay loop with step and check
            do {
                //Show current map
                gm.displayMap(gm.gameMap, debugOn);

                //Parse all info about the vicinity of the player spot
                gm.parsePlayerVicinity(player.getPlayerYCoordinate(), player.getPlayerXCoordinate());

                //Take player input for movement/shooting
                do {

                    try {
                        System.out.println("Please give direction or shoot:");
                        System.out.println("w=up, s=down, a=left, d=right, f=shoot");
                        dir = sc.next().charAt(0);
                    } catch(Exception e) {
                        System.out.println("Please give only single character![wasd]");
                        e.printStackTrace();
                        dir = 'N';
                    }

                } while(dir == 'N');

                //Check player shooting commands
                if(dir == 'f') {
                    //Take shooting direction and check if hit IF you have ammo left
                    if(player.getPlayerArrows() > 0) {
                        System.out.println("Arrows left: " + player.getPlayerArrows());

                        boolean isAiming = true;
                        //Player shooting loop
                        do {

                            try {
                                System.out.println("Give shooting direction:");
                                System.out.println("w=up, s=down, a=left, d=right, g = cancel");

                                //Take arrow direction
                                arrowDir = sc.next().charAt(0);

                            } catch(Exception e) {
                                System.out.println("Invalid input!");
                                isAiming = false;
                            }


                            isAiming = false;

                        } while(isAiming);

                        //Set shooting direction with int 1-4
                        int arrowStep = 0;

                        //Possible aim directions with aim limit checks
                        if(arrowDir == 'w') {
                            if(player.getPlayerYCoordinate() > 0) {
                                arrowStep = 1;
                            }
                        } else if (arrowDir == 's') {
                            if(player.getPlayerYCoordinate() < 4) {
                                arrowStep = 2;
                            }

                        } else if (arrowDir == 'a') {
                            if(player.getPlayerXCoordinate() > 0) {
                                arrowStep = 3;
                            }

                        } else if(arrowDir == 'd') {
                            if(player.getPlayerXCoordinate() < 4) {
                                arrowStep = 4;
                            }

                        } else {
                            System.out.println("Shooting cancelled!");
                            arrowDir = 'g';
                        }

                        if(!(arrowDir == 'g')) {
                            //Remove one arrow from inventory
                            player.setPlayerArrows(-1);
                            arrowHit = gm.checkArrowHit(arrowStep, wumpus, player.getPlayerYCoordinate(), player.getPlayerXCoordinate());

                        }

                    } else {
                        System.out.println("Sorry, you have no arrows left");
                    }

                } else {
                    //Don't shoot, move player instead
                    player.movePlayer(dir);
                }

                if(!arrowHit) {

                    //Check any collisions after moving
                    collisionType = gm.checkCollisionEvent(player.getPlayerYCoordinate(), player.getPlayerXCoordinate());

                    //IF we hit a bat spot
                    if(collisionType == 3) {
                        //Relocate player to a random spot, check collision again
                        System.out.println("Bats pick you up!");

                        //Relocate and check collisions type for the bat move
                        int batCol = gm.checkBatThrow(player);//goes to checkCollisionEvent()

                        //THIS SHOULD NOT GO TO BAT SPOT AGAIN!
                        respondToCollision(batCol, gm, player, wumpus);

                    } else {
                        respondToCollision(collisionType, gm, player, wumpus);
                    }

                }

                //YOU WIN
                if(arrowHit) {
                    gameOn = false;
                    gm.printPlayerWin();
                }

            } while(gameOn);

            //Ask play again
            do {

                try {
                    System.out.println("Play again?");
                    System.out.println("[y = yes], [n = no]");
                    playAgainAnswer = sc.next().charAt(0);
                } catch(Exception e) {
                    System.out.println("Invalid answer!");
                }

            } while(!(playAgainAnswer == 'y') && !(playAgainAnswer == 'n'));

            if(playAgainAnswer == 'y') {
                playAgain = true;
            } else if(playAgainAnswer == 'n') {
                playAgain = false;
                System.out.println("Thank you for playing!");
                runMenuOptions();
            }

        }

    }

    //Make a response based on collisions after player movement
    public static void respondToCollision(int collisionType, GameManager gm, Player player, Wumpus wumpus) {

        //Sets current updates to the map only after checking the collisions
        if(collisionType == 0) {
            //No collision, continue normally
            gm.updateMap(player.getPlayerXCoordinate(), player.getPlayerYCoordinate(), wumpus.getWumpusXCoordinate(), wumpus.getWumpusYCoordinate());

        } else if(collisionType == 3) {
            //Relocate player to a random spot, check collision again
            System.out.println("Bats pick you up!");

            //Relocate and check collisions type for the bat move
            int batCol = gm.checkBatThrow(player);//goes to checkCollisionEvent()

            //Only then update the map changes (might be safe spot or not)
            gm.updateMap(player.getPlayerXCoordinate(), player.getPlayerYCoordinate(), wumpus.getWumpusXCoordinate(), wumpus.getWumpusYCoordinate());

        } else if (collisionType == 4) {
            //Give player an arrow, remove arrow from spot
            System.out.println("You found an arrow!");
            player.setPlayerArrows(1);
            gm.updateMap(player.getPlayerXCoordinate(), player.getPlayerYCoordinate(), wumpus.getWumpusXCoordinate(), wumpus.getWumpusYCoordinate());

        } else if(collisionType == 1 || collisionType == 2) {
            gameOn = false;

            //Show death ASCII art
            if(collisionType == 1) {
                gm.printPlayerEaten();
            } else {
                gm.printPlayerFall();
            }
        }
    }
}
