package org.example;

import java.util.ArrayList;

   public class Actor  {
       private final String name;
       private ArrayList<Starring>  starrings;
       public Actor(String name)
       {
            if (name == null) {
                throw new IllegalArgumentException("Actor name cannot be null");
            }
            this.name = name;
            this.starrings = new ArrayList<Starring>();
       }
       public void addStarring(Starring starring)
       {
           if (starring == null)
           {
               System.out.println(Def.IN_ACTOR + "Null starring");
               return;
           }
           for (int i = 0; i < this.starrings.size(); i++) {

               if (starring.doesOverlap(this.starrings.get(i).getFrom(),this.starrings.get(i).getTo()))
               {
                   System.out.println(Def.IN_ACTOR + "Not adding " + starring.getMovie() + " Overlapping " + this.starrings.get(i));
                   return;
               }
           }
           if (starring.getMovie()==null)
           {
               System.out.println(Def.IN_ACTOR + "Null movie name");
               return;
           }
           System.out.println(Def.IN_ACTOR + "adding " + starring);
           this.starrings.add(starring);
       }
        public int getMovieWorkDays(String movie) {
            if (movie == null) return 0;

            return this.starrings.stream()
                    .filter(starring -> starring.getMovie().equals(movie))
                    .mapToInt(starring -> starring.getWorkDays())
                    .sum();
       }
       public int getAllWorkDays() {

           return this.starrings.stream()
                   .mapToInt(starring -> starring.getWorkDays())
                   .sum();
       }
       public void logStarring()
       {
           for (int i = 0; i < this.starrings.size(); i++) {
               System.out.println(this.starrings.get(i));
           }
       }
       public String getName()
       {
           return this.name;
       }
   }