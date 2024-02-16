package main;

import data.*;

public class WorkshopScheduler {
    public static void main(String[] args) throws Exception {
        long time = System.currentTimeMillis();
        Workshop workshop = Utils.fromFile("data/workshop_1.json", Workshop.class);
        assert workshop != null;
        System.out.println(workshop);
    }
}
