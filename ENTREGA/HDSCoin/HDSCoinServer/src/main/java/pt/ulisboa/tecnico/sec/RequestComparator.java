package pt.ulisboa.tecnico.sec;

import java.util.Comparator;

public class RequestComparator{
    public static final Comparator<Request> WTS = (Request r1, Request r2) -> r1.compareTo(r2);
}
