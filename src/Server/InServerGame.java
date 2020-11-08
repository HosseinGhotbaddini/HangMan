package Server;

import java.util.concurrent.Semaphore;

class InServerGame {
    InServerClient[] player = new InServerClient[2];
    private byte turn = 0, score[] = new byte[]{0, 0}, turnsSet = 0;
    private int gameID;
    private String chosenWord, triedWord;
    private Character triedChar;
    private String shownChars = "";
    private Semaphore controller = new Semaphore(1, true);
    private InServerGame.RunGame runGame = new InServerGame.RunGame();

    InServerGame(InServerClient p0, InServerClient p1, int gameID) {
        InServerClient.games.put(gameID, this);
        player[0] = p0;
        player[1] = p1;
        p0.sender.write("ing§" + p1.playerID + "§" + gameID + "\n");
        p0.sender.flush();
        p1.sender.write("ing§" + p0.playerID + "§" + gameID + "\n");
        p1.sender.flush();
        this.gameID = gameID;
        turn = 5;
//        setTurn();
//        while (turnsSet < 2)
//            try {
//                Thread.sleep(400);
//            } catch (InterruptedException e) {
//            }
        runGame.start();
    }

    private void setTurn() {
        turn = Byte.MAX_VALUE;
        player[0].requestTurn(gameID);
        player[1].requestTurn(gameID);
    }

    void endGame(InServerClient rival) {
        InServerClient.games.remove(gameID);
        rival.sender.write("eg§" + gameID + "\n");
        rival.sender.flush();
    }

    void setTurnByPlayer(byte turn) {
        this.turn = (byte) Math.min(this.turn, turn);
        ++turnsSet;
    }

    void setWord(String chosenWord) {
        this.chosenWord = chosenWord;
        controller.release();
    }

    void guessChar(char triedChar) {
        this.triedChar = triedChar;
        controller.release();
    }

    private boolean checkWord(int guesserPlayer) {
        triedWord = null;
        player[guesserPlayer].guessWord(gameID);
        try {
//            controller.acquire();//after this guessWord is called.
            while (triedWord == null)
                Thread.sleep(500);
            if (triedWord.equals(chosenWord))
                return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    void guessWord(String triedWord) {
        this.triedWord = triedWord;
        controller.release();
    }

    private class RunGame extends Thread {
        @Override
        public void run() {
            for (int i = 0; i < turn; ++i) {
                player[i % 2].setChooser(gameID);
                chosenWord = null;
//                try {
//                    controller.acquire();//after this setWord is called.
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                while (chosenWord == null)
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                System.out.println("Game [" + gameID + "] word: " + chosenWord);
                shownChars = "";
                for (int j = 0; j < chosenWord.length(); ++j)
                    shownChars += "" + (char) 0;
                player[0].informChars(shownChars, gameID);
                player[1].informChars(shownChars, gameID);
                boolean result = false;
                for (int j = 0; j < chosenWord.length(); ++j) {
                    triedChar = null;
                    player[(i + 1) % 2].guessChar(gameID);
                    while (triedChar == null)
                        try {
//                        controller.acquire();//after this tryChar is called.
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    for (int cnt = 0; cnt < chosenWord.length(); ++cnt)
                        if (chosenWord.charAt(cnt) == triedChar)
                            shownChars =
                                    shownChars.substring(0, cnt)
                                            + chosenWord.charAt(cnt)
                                            + shownChars.substring(cnt + 1);

                    for (int cnt = 0; cnt < 2; ++cnt)
                        player[cnt].informChars(shownChars, gameID);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    boolean chk = true;
                    for (int cnt = 0; cnt < chosenWord.length(); ++cnt)
                        if (shownChars.charAt(cnt) == 0) {
                            chk = false;
                            break;
                        }
                    if (chk) {
                        result = true;
                        break;
                    }
                }

                if (result || checkWord((i + 1) % 2)) {
                    score[(i + 1) % 2]++;
                    for (int cnt = 0; cnt < 2; ++cnt)
                        player[cnt].informResult(true, gameID);
                } else {
                    for (int cnt = 0; cnt < 2; ++cnt)
                        player[cnt].informResult(false, gameID);
                }
            }

            for (int cnt = 0; cnt < 2; ++cnt)
                endGame(player[cnt]);

            stop();
        }

    }


}
