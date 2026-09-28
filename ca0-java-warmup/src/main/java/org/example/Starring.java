package org.example;

public class Starring {
    private final String movie;
    private final Date from;
    private final Date to;
    private final int workDays;

    public Starring(String movie, Date from, Date to) {

        this.movie = movie;
        if (from.compareTo(to) > 0)
        {
            throw new IllegalArgumentException(Def.ERROR_INTERVAL);
        }
        this.from = from;
        this.to = to;
        this.workDays = calcWorkDays();
    }
    public boolean doesOverlap(Date from, Date to)
    {
        if(from.compareTo(to) > 0)
        {
            throw new IllegalArgumentException(Def.ERROR_INTERVAL);
        }
        if (this.to.compareTo(from) >= 0 && this.from.compareTo(to) <= 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    private int calcWorkDays()
    {
        int workDays = 1;
        Date tmp = this.from;
        while (!tmp.equals(this.to))
        {
            tmp = tmp.nextDay();
            workDays++;
        }
        return workDays;
    }
    public int getWorkDays()
    {
        return this.workDays;
    }
    public String getMovie()
    {
        return this.movie;
    }
    public Date getFrom()
    {
        return this.from;
    }
    public Date getTo()
    {
        return this.to;
    }

    @Override
    public String toString()
    {
        return movie + ": from " + from.toString() + " to " + to.toString();
    }
}