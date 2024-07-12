package airplane.g0;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.awt.geom.Point2D;
import java.lang.Math;

import airplane.sim.SimulationResult;
import org.apache.log4j.Logger;
import airplane.sim.Plane;
import airplane.sim.Player;

public class SamPlayer extends Player {
    private Logger logger = Logger.getLogger(this.getClass());
    private Map<Point2D.Double, Integer> lastTakeoffTime = new HashMap<>();

    @Override
    public String getName() {
        return "Sam player";
    }

    @Override
    public void startNewGame(ArrayList<Plane> planes) {
        logger.info("Starting new game!");
        lastTakeoffTime.clear();
    }

    @Override
    public double[] updatePlanes(ArrayList<Plane> planes, int round, double[] bearings) {
        // Handle takeoffs with staggering
        for (int i = 0; i < planes.size(); i++) {
            Plane plane = planes.get(i);
            if (round >= plane.getDepartureTime() && plane.getBearing() == -1) {
                Point2D.Double destination = plane.getDestination();
                boolean canTakeOff = true;

                // Check if there's another plane within 5 units of the takeoff point
                for (int j = 0; j < planes.size(); j++) {
                    if (i != j && planes.get(j).getLocation().distance(plane.getLocation()) < 5) {
                        canTakeOff = false;
                        break;
                    }
                }

                if (canTakeOff && (!lastTakeoffTime.containsKey(destination) || round >= lastTakeoffTime.get(destination) + 4)) {
                    // Allow takeoff
                    bearings[i] = calculateBearing(plane.getLocation(), destination);
                    plane.setBearing(bearings[i]);
                    lastTakeoffTime.put(destination, round);
                } else {
                    // Delay takeoff
                    bearings[i] = -1;
                }
            }
        }

        // Collision avoidance for planes in the air
        ArrayList<Plane> planesInAir = new ArrayList<>();
        ArrayList<Integer> planesInAirIndices = new ArrayList<>();
        for (int i = 0; i < planes.size(); i++) {
            if (planes.get(i).getBearing() >= 0) {
                planesInAir.add(planes.get(i));
                planesInAirIndices.add(i);
            }
        }

        if (planesInAir.size() > 1) {
            SimulationResult simulation = this.startSimulation(planesInAir, round);
            if (simulation.getReason() == SimulationResult.TOO_CLOSE) {
                // Collision predicted, take action based on priority
                for (int i = 0; i < planesInAir.size(); i++) {
                    int index = planesInAirIndices.get(i);
                    Plane plane = planesInAir.get(i);

                    // Higher priority planes (lower index) make smaller adjustments
                    double adjustmentFactor = (double)(i + 1) / planesInAir.size();
                    double adjustment = 30 * adjustmentFactor; // Up to 30 degrees for lowest priority plane

                    // Determine turn direction based on relative positions
                    Point2D.Double center = new Point2D.Double(50, 50); // Assuming 100x100 grid
                    double angleToCenter = calculateBearing(plane.getLocation(), center);
                    double angleDiff = (angleToCenter - plane.getBearing() + 360) % 360;

                    if (angleDiff > 180) {
                        bearings[index] = (plane.getBearing() + adjustment) % 360;
                    } else {
                        bearings[index] = (plane.getBearing() - adjustment + 360) % 360;
                    }

                    plane.setBearing(bearings[index]);
                }
            } else {
                // No immediate collision, make small adjustments towards destination
                for (int i = 0; i < planesInAir.size(); i++) {
                    int index = planesInAirIndices.get(i);
                    Plane plane = planesInAir.get(i);

                    double newBearing = calculateBearing(plane.getLocation(), plane.getDestination());
                    double difference = (newBearing - plane.getBearing() + 360) % 360;
                    if (difference > 180) difference -= 360;

                    double adjustment = Math.min(10, Math.abs(difference));
                    if (difference > 0) {
                        bearings[index] = (plane.getBearing() + adjustment) % 360;
                    } else {
                        bearings[index] = (plane.getBearing() - adjustment + 360) % 360;
                    }

                    plane.setBearing(bearings[index]);
                }
            }
        }

        // Ensure bearings are within 0-360 range
        for (int i = 0; i < bearings.length; i++) {
            if (bearings[i] >= 0 && bearings[i] != -1) {
                bearings[i] = bearings[i] % 360;
                planes.get(i).setBearing(bearings[i]);
            }
        }

        return bearings;
    }

    public double distance(Plane plane1, Plane plane2) {
        return plane1.getLocation().distance(plane2.getLocation());
    }

    @Override
    protected double[] simulateUpdate(ArrayList<Plane> planes, int round, double[] bearings) {
        // not implemented
        return bearings;
    }
}
