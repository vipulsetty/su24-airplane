package airplane.g0;

import airplane.sim.Plane;
import airplane.sim.Player;
import airplane.sim.SimulationResult;
import org.apache.log4j.Logger;

import java.util.ArrayList;

public class optimizedPlayer extends Player {
    private Logger logger = Logger.getLogger(this.getClass()); // for logging
    boolean[] priority;
    int simulationRuns=0;

    @Override
    public String getName() {
        return "player optimized";
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
                    for(int j=0;j<planes.size();j++){
                        if(j==i){continue;}
                        if(round- curPlane.getDepartureTime()>=1000){break;}
                        if(planes.get(j).getBearing()==-1 || planes.get(j).getBearing()==-2){continue;}

                        if(curPlane.getLocation().distance(planes.get(j).getLocation())<=25){
                            continue loop;
                        }
                    }
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

            //simulationRuns=0;
            SimulationResult simulation = this.startSimulation(planes,round);
            if(simulation.getReason()==4 && checkCollision(i,simulation.getPlanes())){
                bearings[i]=(bearings[i]+10) % 360;
                planes.get(i).setBearing(bearings[i]);
                continue;
            }
            if(simulation.getReason()!=4){
                double originalBearing = bearings[i];
                double newBearing=calculateBearing(planes.get(i).getLocation(), planes.get(i).getDestination());
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
                //simulationRuns=0;
                SimulationResult checkSimulation = this.startSimulation(planes,round);
                if(checkSimulation.getReason()==4 && checkCollision(i,checkSimulation.getPlanes())){
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
        /*for (int i=0;i<planes.size();i++){
            Plane curPlane = planes.get(i);
            if(curPlane.getLocation().equals(curPlane.getDestination())){
                curPlane.setBearing(-2);
                bearings[i]=-2;
            }
        }*/
        simulationRuns+=1;
        if (simulationRuns>=1000){
            simulationRuns=0;
            stopSimulation();
        }
        return bearings;
    }

    private boolean checkCollision(int pos, ArrayList<Plane> planes){
        for(int i=0;i<planes.size();i++){
            if(i==pos){continue;}
            if(planes.get(i).getBearing()==-1 || planes.get(i).getBearing()==-2){continue;}
            if(planes.get(i).getLocation().distance(planes.get(pos).getLocation())<=5){
                return true;
            }
        }
        return false;
    }


}
