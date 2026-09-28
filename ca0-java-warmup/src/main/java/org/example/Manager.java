package org.example;

import java.nio.file.Paths;
import java.util.ArrayList;

import com.opencsv.CSVReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Manager {
    private List<Actor> actors = new ArrayList<>();
    public Manager() {
        this.actors = new ArrayList<Actor>();
    }
    public void run() {

        try {
            List<String[]> input = readAllLines(Paths.get(Def.FILE_PATH));
            for (String[] row : input) {
                handleLine(row);
            }
            testNextDay();
            testWorkingDays();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static List<String[]> readAllLines(Path filePath) throws Exception {
        try (Reader reader = Files.newBufferedReader(filePath)) {
            try (CSVReader csvReader = new CSVReader(reader)) {
                return csvReader.readAll();
            }
        }
    }
    public void handleLine(String[] line)
    {
        String name = line[Def.ACTOR_NAME];
        String movie = line[Def.MOVIE];
        Date from = new Date(
                Integer.parseInt(line[Def.FROM_DAY]),
                Integer.parseInt(line[Def.FROM_MONTH]),
                Integer.parseInt(line[Def.FROM_YEAR]));
        Date to = new Date(
                Integer.parseInt(line[Def.TO_DAY]),
                Integer.parseInt(line[Def.TO_MONTH]),
                Integer.parseInt(line[Def.TO_YEAR]));

        Actor actor = this.find(name);
        if (actor == null) {
            actor = new Actor(name);
        }
        actors.add(actor);
        Starring starring = new Starring(movie, from, to);
        actor.addStarring(starring);
    }
    private Actor find(String name) {
        for (int i = 0; i<actors.size(); i++)
        {
            if(actors.get(i).getName().equals(name))
            {
                return actors.get(i);
            }
        }
        return null;
    }
    private void testNextDay()
    {
        System.out.println(Def.SECTION_SPLITTER);
        Date[] dates = {
                new Date(13, 12, 1403),
                new Date(29, 12, 1403),
                new Date(30, 12, 1403),
                new Date(29, 12, 1402)
        };

        for (Date date : dates) {
            System.out.println("The next day of " + date + " is " + date.nextDay());
        }
    }
    private  void testWorkingDays()
    {
        System.out.println(Def.SECTION_SPLITTER);
        Actor actor = find("James Stewart");
        System.out.println("All working days of 'James Stewart': " + actor.getAllWorkDays());
        System.out.println("Working days for <Rear Window>: " + actor.getMovieWorkDays("Rear Window"));
        System.out.println("Working days for <The Man Who Knew Too Much>: " + actor.getMovieWorkDays("The Man Who Knew Too Much"));
        System.out.println("Working days for <Vertigo>: " + actor.getMovieWorkDays("Vertigo"));
    }
}

