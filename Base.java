package concurent.student.first;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.text.html.HTMLDocument.Iterator;

public class Base {

    private static final int STARTER_PEASANT_NUMBER = 5;
    private static final int PEASANT_NUMBER_GOAL = 10;

    // lock to ensure only one unit can be trained at one time
    private final ReentrantLock trainingLock = new ReentrantLock();

    private final String name;
    private final Resources resources = new Resources();
    private final List<Peasant> peasants = Collections.synchronizedList(new LinkedList<>());
    private final List<Building> buildings = Collections.synchronizedList(new LinkedList<>());

    private int miners = 0;
    private int lumbers = 0;

    public Base(String name) {
        this.name = name;
        Peasant current;
        for (int i = 0; i < STARTER_PEASANT_NUMBER; i++) {
            current = createPeasant();
            if (miners < 3) {
                current.startMining();
                miners++;
            } else if (lumbers < 1) {
                current.startCuttingWood();
                lumbers++;
            }
            peasants.add(current);
        }

        // TODO Create the initial 5 peasants - Use the STARTER_PEASANT_NUMBER constant
        // TODO 3 of them should mine gold
        // TODO 1 of them should cut tree
        // TODO 1 should do nothing
        // TODO Use the createPeasant() method
    }

    public void startPreparation() {

        // TODO Start the building and training preparations on separate threads
        // TODO Tip: use the hasEnoughBuilding method

        Thread Training = new Thread(new Runnable() {
            @Override
            public void run() {
                while (peasants.size() < PEASANT_NUMBER_GOAL) {
                    Peasant noob = createPeasant();
                    if (noob != null) {
                        if (miners < 5) {
                            noob.startMining();
                            miners++;
                        } else if (lumbers < 2) {
                            noob.startCuttingWood();
                            lumbers++;
                        }
                        peasants.add(noob);
                    }
                }
            }
        });
        Training.start();

        Thread Construction1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!hasEnoughBuilding(UnitType.FARM, 3)) {
                    Peasant builder = getFreePeasant();
                    if (builder != null) {
                        if(builder.tryBuilding(UnitType.FARM)){
                            sleepForMsec(100);
                        }
                    }
                }
            }
        });
        Construction1.start();

        Thread Construction2 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!hasEnoughBuilding(UnitType.LUMBERMILL, 1)) {
                    Peasant builder = getFreePeasant();
                    if (builder != null) {
                        if(builder.tryBuilding(UnitType.LUMBERMILL)){
                            sleepForMsec(100);
                        }
                    }
                }
            }
        });
        Construction2.start();

        Thread Construction3 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!hasEnoughBuilding(UnitType.BLACKSMITH, 1)) {
                    Peasant builder = getFreePeasant();
                    if (builder != null) {
                        if(builder.tryBuilding(UnitType.BLACKSMITH)){
                            sleepForMsec(100);
                        }
                    }
                }
            }
        });
        Construction3.start();
        try {
            Training.join();
        } catch (InterruptedException e) {
        }
        try {
            Construction1.join();
        } catch (InterruptedException e) {
        }
        try {
            Construction2.join();
        } catch (InterruptedException e) {
        }
        try {
            Construction3.join();
        } catch (InterruptedException e) {
        }

        synchronized (peasants) {
            for (Peasant current : peasants) {
                current.stopHarvesting();
            }
        }

        // TODO Build 3 farms - use getFreePeasant() method to see if there is a peasant
        // without any work

        // TODO Create remaining 5 peasants - Use the PEASANT_NUMBER_GOAL constant
        // TODO 5 of them should mine gold
        // TODO 2 of them should cut tree
        // TODO 3 of them should do nothing
        // TODO Use the createPeasant() method

        // TODO Build a lumbermill - use getFreePeasant() method to see if there is a
        // peasant without any work

        // TODO Build a blacksmith - use getFreePeasant() method to see if there is a
        // peasant without any work

        // TODO Wait for all the necessary preparations to finish

        // TODO Stop harvesting with the peasants once everything is ready
        System.out.println(this.name + " finished creating a base");
        System.out.println(this.name + " peasants: " + this.peasants.size());
        for (Building b : buildings) {
            System.out.println(this.name + " has a  " + b.getUnitType().toString());
        }

    }

    /**
     * Returns a peasants that is currently free.
     * Being free means that the peasant currently isn't harvesting or building.
     *
     * @return Peasant object, if found one, null if there isn't one
     */
    private Peasant getFreePeasant() {
        synchronized (peasants) {
            for (Peasant result : peasants) {
                if (result.isFree()) {
                    return result;
                }
            }
        }

        // TODO implement - use the peasant's isFree() method
        return null;
    }

    /**
     * Creates a peasant.
     * A peasant could only be trained if there are sufficient
     * gold, wood and food for him to train.
     *
     * At one time only one Peasant can be trained.
     *
     * @return The newly created peasant if it could be trained, null otherwise
     */
    private Peasant createPeasant() {
        Peasant result;
        trainingLock.lock();
        try {
            if (resources.canTrain(UnitType.PEASANT.goldCost, UnitType.PEASANT.woodCost, UnitType.PEASANT.foodCost)) {

                sleepForMsec(UnitType.PEASANT.buildTime);
                resources.removeCost(UnitType.PEASANT.goldCost, UnitType.PEASANT.woodCost);
                resources.updateCapacity(UnitType.PEASANT.foodCost);
                result = Peasant.createPeasant(this);
                return result;

                // TODO 1: Sleep as long as it takes to create a peasant - use sleepForMsec()
                // method
                // TODO 2: Remove costs
                // TODO 3: Update capacity
                // TODO 4: Use the Peasant class' createPeasant method to create the new Peasant

                // TODO Remember that at one time only one peasant can be trained
                // return result;
            }
            return null;
        } finally {
            trainingLock.unlock();
        }
    }

    public Resources getResources() {
        return this.resources;
    }

    public List<Building> getBuildings() {
        return this.buildings;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Helper method to determine if a base has the required number of a certain
     * building.
     *
     * @param unitType Type of the building
     * @param required Number of required amount
     * @return true, if required amount is reached (or surpassed), false otherwise
     */
    private boolean hasEnoughBuilding(UnitType unitType, int required) {
        int count = 0;
        synchronized (buildings) {
            for (Building current : buildings) {
                if (current.getUnitType() == unitType) {
                    count++;
                }
            }
        }
        if (count == required) {
            return true;
        } else {
            // TODO check in the buildings list if the type has reached the required amount
            return false;
        }
    }

    private static void sleepForMsec(int sleepTime) {
        try {
            TimeUnit.MILLISECONDS.sleep(sleepTime);
        } catch (InterruptedException e) {
        }
    }
}