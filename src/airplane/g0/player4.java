package airplane.g0;

import airplane.sim.Plane;
import airplane.sim.Player;
import airplane.sim.SimulationResult;
import org.apache.log4j.Logger;

import java.util.ArrayList;

public class player4 extends Player {
    private Logger logger = Logger.getLogger(this.getClass()); // for logging
    boolean[] priority;

    @Override
    public String getName() {
        return "player 4";
    }

    @Override
    public void startNewGame(ArrayList<Plane> planes) {
        logger.info("Starting new game!");
    }

    @Override
    public double[] updatePlanes(ArrayList<Plane> planes, int round, double[] bearings) {
        if(priority==null){
            priority = new boolean[planes.size()];
            priority[priority.length-1] = true;
        }

        loop:
        for (int i=0;i<planes.size();i++){
            if(round>=planes.get(i).getDepartureTime()){
                Plane curPlane=planes.get(i);
                if(curPlane.getBearing()==-1){
                    bearings[i] = calculateBearing(curPlane.getLocation(), curPlane.getDestination());
                    planes.get(i).setBearing(bearings[i]);
                }
            }
        }

        if (planes.size()==1){return bearings;}

        mainloop:
        for (int i=0;i<planes.size();i++){
            if(planes.get(i).getBearing()==-2 || planes.get(i).getBearing()==-1){
                continue;
            }

            SimulationResult simulation = this.startSimulation(planes,round);
            logger.info(simulation.getReason());
            if(simulation.getReason()==4 ){
                bearings[i]=(bearings[i]+10) % 360;
                planes.get(i).setBearing(bearings[i]);
            }
            if(simulation.getReason()!=4){
                double originalBearing = bearings[i];
                double newBearing=calculateBearing(planes.get(i).getLocation(), planes.get(i).getDestination());
                //if(Math.abs(newBearing-planes.get(i).getBearing())>180 && planes.get(i).getLocation().distance(planes.get(i).getDestination())<=10){
                if(Math.abs(newBearing-planes.get(i).getBearing()) / planes.get(i).getLocation().distance(planes.get(i).getDestination()) >13){
                    continue;
                }
                if (Math.abs(newBearing-planes.get(i).getBearing())>10){
                    bearings[i]=(planes.get(i).getBearing()+350)%360;
                    planes.get(i).setBearing(bearings[i]);
                }
                else{
                    bearings[i]=newBearing;
                    planes.get(i).setBearing(bearings[i]);
                }
                SimulationResult checkSimulation = this.startSimulation(planes,round);
                if(checkSimulation.getReason()==4){
                    bearings[i]=originalBearing;
                    planes.get(i).setBearing(bearings[i]);
                }
            }

        }


        return bearings;
    }

    public double distance(Plane plane1, Plane plane2){
        double out = plane1.getLocation().distance(plane2.getLocation());
        return out;
    }

    @Override
    protected double[] simulateUpdate(ArrayList<Plane> planes, int round, double[] bearings) {
        // not implemented
        for (Plane plane:planes){
            if(plane.getBearing()==-1){
                stopSimulation();
            }
        }
        return bearings;
    }


}
