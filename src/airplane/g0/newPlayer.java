package airplane.g0;

import java.util.ArrayList;
import java.lang.Math;
import org.apache.log4j.Logger;
import airplane.sim.Plane;
import airplane.sim.Player;

public class newPlayer extends Player {
    private Logger logger = Logger.getLogger(this.getClass()); // for logging

    @Override
    public String getName() {
        return "new player";
    }

    @Override
    public void startNewGame(ArrayList<Plane> planes) {
        logger.info("Starting new game!");
    }

    @Override
    public double[] updatePlanes(ArrayList<Plane> planes, int round, double[] bearings) {

        // if any plane is in the air, then just keep things as-is
        for (int i=0;i<planes.size();i++){
            if(round>=planes.get(i).getDepartureTime()){
                Plane curPlane=planes.get(i);
                if(curPlane.getBearing()==-1){
                    bearings[i] = calculateBearing(curPlane.getLocation(), curPlane.getDestination());
                }

            }
        }

        if (planes.size()==1){return bearings;}

        if(distance(planes.get(0),planes.get(1))<15 ){
            bearings[0]+=10;
            bearings[1]+=10;
        }

        if(distance(planes.get(0),planes.get(1))>=15 ){
            double newBearing=calculateBearing(planes.get(0).getLocation(), planes.get(0).getDestination());
            if (Math.abs(newBearing-planes.get(0).getBearing())>10 && round>10){
                bearings[0]=planes.get(0).getBearing()-10;
            }
            else{
                bearings[0]=newBearing;
            }

            newBearing=calculateBearing(planes.get(1).getLocation(), planes.get(1).getDestination());
            if (Math.abs(newBearing-planes.get(1).getBearing())>10 && round>10){
                bearings[1]=planes.get(1).getBearing()-10;
            }
            else{
                bearings[1]=newBearing;
            }
        }

        if(bearings[0]>360){
            bearings[0]-=360;
        }
        if(bearings[1]>360){
            bearings[1]-=360;
        }

        if(bearings[0]<0 && round>10){
            bearings[0]+=360;
        }
        if(bearings[1]<0 && round>10){
            bearings[1]+=360;
        }

        return bearings;
    }

    public double distance(Plane plane1, Plane plane2){
        double out = plane1.getLocation().distance(plane2.getLocation());
        return out;
    }
}
