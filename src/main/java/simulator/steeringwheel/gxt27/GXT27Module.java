package simulator.steeringwheel.gxt27;

import net.java.games.input.Component;
import net.java.games.input.Component.Identifier.Button;
import net.java.games.input.Controller;
import net.java.games.input.DirectAndRawInputEnvironmentPlugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulator.BasicModule;

import com.swedspot.scs.data.Uint32;

public class GXT27Module extends BasicModule implements Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(GXT27Module.class);
    public final static int STEERING_WHEEL_ID = 514;
    private boolean isRunning = false;
    private boolean isPaused = false;
    private boolean hasChanged = false;
    private Thread gxt27InputThread;
    private Controller controller;
    private ControllerModel model;
    private Component com;

    public GXT27Module() {
    }

    @Override
    public void run() {
        initDevice();
        if (controller == null) {
            LOGGER.debug("Could not find the Trust GXT 27 steering wheel");
            return;
        }
        
        while (isRunning) {
            updateControllerModel(controller.poll());
            if (hasChanged) {
                simulator.sendValue(STEERING_WHEEL_ID, new Uint32(model.getFlags()));
                hasChanged = false;
            }
            while (isPaused) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private Controller getController() throws Exception {
        LOGGER.debug("Looking for controller");
        System.setProperty("net.java.games.input.librarypath", "C:/Users/Nine/workspace/simulator-gui/libs/natives");
        DirectAndRawInputEnvironmentPlugin env = new DirectAndRawInputEnvironmentPlugin();
        Controller[] controllers = env.getControllers();
        LOGGER.debug("Environment found");
        for (Controller cont : controllers) {
            LOGGER.debug("Controller: "+cont.getName());
            if (cont.getName().equals("Steering Wheel")){
                LOGGER.debug("Found Controller");
                return cont;
            }
        }
        LOGGER.debug("No controller found");
        return null;
    }

    private void initDevice() {
        try {
            controller = getController();
        } catch (Exception e) {
            LOGGER.debug("Exception with" + e.getLocalizedMessage());
            e.printStackTrace();
        }
        model = new ControllerModel();
    }

    private void updateControllerModel(boolean didPoll) {
        if (didPoll) {
            com = controller.getComponent(Button._0);
            if (com.getPollData() > 0) {
                model.triangle = true;
                LOGGER.debug("triangle");
            } else if (model.triangle) {
                model.triangle = false;
                hasChanged = true;
            }

            com = controller.getComponent(Button._1);
            if (com.getPollData() > 0) {
                model.circle = true;
                LOGGER.debug("circle");
            } else if (model.circle) {
                model.circle = false;
                hasChanged = true;
            }

            com = controller.getComponent(Button._2);
            if (com.getPollData() > 0) {
                model.cross = true;
                LOGGER.debug("cross");
            } else if (model.cross) {
                model.cross = false;
                hasChanged = true;
            }

            com = controller.getComponent(Button._3);
            if (com.getPollData() > 0) {
                model.square = true;
                LOGGER.debug("square");
            } else if (model.square) {
                model.square = false;
                hasChanged = true;
            }
            com = controller.getComponent(Component.Identifier.Axis.POV);
            float pollData = com.getPollData();
            if(pollData > 0){
                if (pollData == 0.25) {
                    model.up = true;
                    LOGGER.debug("up");
                } 
                if (pollData == 0.5) {
                    model.right = true;
                    LOGGER.debug("right");
                } 
                if (pollData == 0.75) {
                    model.down = true;
                    LOGGER.debug("down");
                }
                if (pollData == 1.0) {
                    model.left = true;
                    LOGGER.debug("left");
                }
            } else if (model.up) {
                model.up = false;
                hasChanged = true;
            } else if (model.right) {
                model.right = false;
                hasChanged = true;
            } else if (model.down) {
                model.down = false;
                hasChanged = true;
            } else if (model.left) {
                model.left = false;
                hasChanged = true;
            }
        }
    }

    @Override
    public void startSimulation() throws Exception {
        LOGGER.debug("\nStarting the simulation\n");
        isRunning = true;
        gxt27InputThread = new Thread(this);
        simulator.provideSignal(STEERING_WHEEL_ID);
        gxt27InputThread.start();
        LOGGER.debug("\nStarted the simulation\n");
    }

    @Override
    public void stopSimulation() throws Exception {
        isRunning = false;
        isPaused = false;
        simulator.unprovideSignal(STEERING_WHEEL_ID);
        gxt27InputThread.join();
        
    }

    @Override
    public void pauseSimulation() throws Exception {
        isPaused = true;
    }

    @Override
    public void resumeSimulation() throws Exception {
        isPaused = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof GXT27Module) {
            GXT27Module other = (GXT27Module) o;
            if (model.equals(other.model)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return model.hashCode() * STEERING_WHEEL_ID + gxt27InputThread.hashCode() * 1000;
    }

    class ControllerModel {
        private boolean square = false;
        private boolean circle = false;
        private boolean triangle = false;
        private boolean cross = false;
        private boolean up = false;
        private boolean right = false;
        private boolean down = false;
        private boolean left = false;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof ControllerModel) {
                ControllerModel other = (ControllerModel) o;
                return square == other.square &&
                        circle == other.circle &&
                        triangle == other.triangle &&
                        cross == other.cross &&
                        up == other.up &&
                        right == other.right &&
                        down == other.down &&
                        left == other.left;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int squareInt = 0;
            int circleInt = 0;
            int triangleInt = 0;
            int crossInt = 0;

            int upInt = 0;
            int rightInt = 0;
            int downInt = 0;
            int leftInt = 0;

            if (square) {
                squareInt = 1;
            }
            if (circle) {
                circleInt = 1;
            }
            if (triangle) {
                triangleInt = 1;
            }
            if (cross) {
                crossInt = 1;
            }

            if (up) {
                upInt = 1;
            }
            if (right) {
                rightInt = 1;
            }
            if (down) {
                downInt = 1;
            }
            if (left) {
                leftInt = 1;
            }

            return squareInt * 1 +
                    circleInt * 5 +
                    triangleInt * 10 +
                    crossInt * 15 +
                    upInt * 20 +
                    rightInt * 25 +
                    downInt * 30 +
                    leftInt * 35;
        }

        public int getFlags() {
            int squareInt = 0;
            int circleInt = 0;
            int triangleInt = 0;
            int crossInt = 0;
            int upInt = 0;
            int rightInt = 0;
            int downInt = 0;
            int leftInt = 0;
            if (square) {
                squareInt = 1;
            }
            if (circle) {
                circleInt = 1;
            }
            if (triangle) {
                triangleInt = 1;
            }
            if (cross) {
                crossInt = 1;
            }

            if (up) {
                upInt = 1;
            }
            if (right) {
                rightInt = 1;
            }
            if (down) {
                downInt = 1;
            }
            if (left) {
                leftInt = 1;
            }
            return (squareInt << 0) +
                    (circleInt << 1) +
                    (triangleInt << 2) +
                    (crossInt << 3) +
                    (upInt << 4) +
                    (rightInt << 5) +
                    (downInt << 6) +
                    (leftInt << 7);
        }
    }
}
