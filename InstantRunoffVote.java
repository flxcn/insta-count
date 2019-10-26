import java.io.*;
import java.sql.Array;
import java.sql.Time;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

public class InstantRunoffVote {

    private ArrayList<String> options;
    private ArrayList<Ballot> ballots;

    // getter and setter methods
    public InstantRunoffVote(){
        options = new ArrayList<>();
        ballots = new ArrayList<>();
    }
    public ArrayList<String> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<String> options) {
        this.options = options;
    }

    public ArrayList<Ballot> getDecisions() {
        return ballots;
    }

    public void setDecisions(ArrayList<Ballot> decisions) {
        this.ballots = decisions;
    }


    // processes the text file
    public void read(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));

        // read first line (header), which lists the options
        String header = reader.readLine();
        setOptions(new ArrayList<String>(Arrays.asList(header.split("\t"))));

        // read the ballots, line by line
        int id=0;
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            String[] lineArray = line.split("\t");
            String timeSubmitted = lineArray[0];
            String[] decisions = Arrays.copyOfRange(lineArray,1,lineArray.length);
            Ballot ballot = new Ballot(id++,timeSubmitted,decisions);
            ballots.add(ballot);
        }

        reader.close();

    }

    public ArrayList<ArrayList<Ballot>> buildInitialTally(){
        // initialize the ArrayList<Ballot> elements within the ArrayList
        ArrayList<ArrayList<Ballot>> tally = new ArrayList<>();
        for(int i=0; i<options.size(); i++){
            ArrayList<Ballot> option = new ArrayList<>();
            tally.add(option);
        }

        // add each ballot to the corresponding ArrayList<Ballot> element based on their first decision
        for(int i=0; i<ballots.size(); i++){
            String decision = ballots.get(i).getDecisions()[0];
            int decisionNumber = options.indexOf(decision);
            tally.get(decisionNumber).add(ballots.get(i));
        }

        return tally;
    }
int w=0;
    // get the winner
    public void getWinner(int round, ArrayList<ArrayList<Ballot>> tally){

        ArrayList<Integer> optionsToBeEliminated = new ArrayList<>();
        int leastPopularOption = 0;
        String winner = "";

        // calculate vote percentages for the round, finds least popular option
        System.out.println("\nRound " + (round+1));
        for(int i=0; i<tally.size();i++){
          double percentage = (double)tally.get(i).size()/ballots.size()*100;
          System.out.println(options.get(i) + ": " + percentage + "% with " + tally.get(i).size() + " out of " + ballots.size() + " votes cast");

          // check if an option has a majority of votes; if so, declare it the winner
          if(percentage > 50) {
              winner = "\nWINNER: " + options.get(i) + " with " + tally.get(i).size() + " out of " + ballots.size() + " votes cast " + "(" + percentage + "%" + ")" + " in Round #" + (round+1);
          }

          // find least popular option
          if(tally.get(i).size() < tally.get(leastPopularOption).size() && tally.get(i).size() > 0){
              leastPopularOption = i;
          }
        }

        // check if winner has been found; if so, print winner and return
        if(!winner.isEmpty()){
            System.out.println(winner);
            return;
        }

        // check for options that are tied with the least popular option
        optionsToBeEliminated.add(leastPopularOption);
        for(int i=0; i<tally.size();i++){
            if(tally.get(i).size() == tally.get(leastPopularOption).size() && tally.get(i).size() > 0 && i != leastPopularOption){
                optionsToBeEliminated.add(i);
            }
        }

        // check to make sure those tied for last are not the only options left
        if(optionsToBeEliminated.size()>1) {
            int totalBallotsToBeShifted = 0;
            for (int i = 0; i < optionsToBeEliminated.size(); i++) {
                totalBallotsToBeShifted += tally.get(i).size();
            }
            if (totalBallotsToBeShifted == ballots.size()) {
                String[] remainingOptions = new String[optionsToBeEliminated.size()];
                for (int i = 0; i < remainingOptions.length; i++) {
                    remainingOptions[i] = options.get(optionsToBeEliminated.get(i));
                }
                System.out.println("NO WINNER: Revote required between the following options:");
                System.out.println(optionsToBeEliminated.toString());
                return;
            }
        }

        // loop through each option that needs to be eliminated
        for(int l=0; l<optionsToBeEliminated.size(); l++){
            // shift ballots away from least popular option(s)
            while(tally.get(optionsToBeEliminated.get(l)).size() > 0){
                Ballot ballot = tally.get(optionsToBeEliminated.get(l)).remove(0);
                String nextDecision = ballot.getDecisions()[1];
                int nextDecisionNumber = options.indexOf(nextDecision);

                int i=2;
                while(i<options.size() && (tally.get(nextDecisionNumber).size()==0 || nextDecisionNumber == optionsToBeEliminated.get(l))){
                    nextDecision = ballot.getDecisions()[i];
                    nextDecisionNumber = options.indexOf(nextDecision);
                    i++;
                }
                tally.get(nextDecisionNumber).add(ballot);
            }
        }


        getWinner(++round,tally);
    }

    public static void main(String[] args){
        System.out.println("InstaCount. Created by Felix Chen. Copyright 2019. All rights reserved.");
        Date date = new Date();
        System.out.println("Generated at " + date.toString());
        InstantRunoffVote irv = new InstantRunoffVote();
        try {
            irv.read("ballots.in");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("\nOptions: " + irv.getOptions().toString());
        ArrayList<ArrayList<Ballot>> tally = irv.buildInitialTally();
        irv.getWinner(0, tally);
    }
}
