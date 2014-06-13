package simulator.steeringwheel.gxt27;

import net.java.games.input.Component;
import static simulator.SimulationModuleState.*;
import net.java.games.input.Component.Identifier.Button;
import net.java.games.input.Controller;
import net.java.games.input.DirectAndRawInputEnvironmentPlugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simulator.BasicModule;
import simulator.SimulatorGateway;
import android.swedspot.scs.data.Uint32;

public class GXT27Module extends BasicModule {
    private final static Logger LOGGER = LoggerFactory.getLogger(GXT27Module.class);
    public final static int STEERING_WHEEL_ID = 514;
    private Controller controller;
    private ControllerModel model;
    private Component com;

    public GXT27Module(SimulatorGateway gateway) {
    	super(gateway);
    }

    @Override
    public void run() {
        initDevice();
        if (controller == null) {
            LOGGER.debug("Could not find the Trust GXT 27 steering wheel");
            return;
        }

        while (state == RUNNING)            
            updateControllerModel(controller.poll());
    }

    private Controller getController() throws Exception {
        LOGGER.debug("Looking for controller");
        System.setProperty("net.java.games.input.librarypath", "C:/workspace/simulator/libs/natives");
        DirectAndRawInputEnvironmentPlugin env = new DirectAndRawInputEnvironmentPlugin();
        Controller[] controllers = env.getControllers();
        LOGGER.debug("Environment found");
        for (Controller cont : controllers) {
            LOGGER.debug("Controller: " + cont.getName());
            if (cont.getName().equals("Steering Wheel")) {
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
                // LOGGER.debug("triangle");
            } else if (model.triangle) {
                gateway.sendValue(STEERING_WHEEL_ID, new Uint32(1 << 0));
                model.triangle = false;
            }

            com = controller.getComponent(Button._1);
            if (com.getPollData() > 0) {
                model.circle = true;
                // LOGGER.debug("circle");
            } else if (model.circle) {
                gateway.sendValue(STEERING_WHEEL_ID, new Uint32(1 << 1));
                model.circle = false;
            }

            com = controller.getComponent(Button._2);
            if (com.getPollData() > 0) {
                model.cross = true;
                // LOGGER.debug("cross");
            } else if (model.cross) {
                gateway.sendValue(STEERING_WHEEL_ID, new Uint32(1 << 2));
                model.cross = false;
            }

            com = controller.getComponent(Button._3);
            if (com.getPollData() > 0) {
                model.square = true;
                // LOGGER.debug("square");
            } else if (model.square) {
                gateway.sendValue(STEERING_WHEEL_ID, new Uint32(1 << 3));
                model.square = false;
            }
            com = controller.getComponent(Component.Identifier.Axis.POV);
            float pollData = com.getPollData();
            if (pollData > 0) {
                if (pollData == 0.25) {
                    model.up = true;
                    // LOGGER.debug("up");
                }
                if (pollData == 0.5) {
                    model.right = true;
                    // LOGGER.debug("right");
                }
                if (pollData == 0.75) {
                    model.down = true;
                    // LOGGER.debug("down");
                }
                if (pollData == 1.0) {
                    model.left = true;
                    // LOGGER.debug("left");
                }
            } else if (model.up) {
                gateway.sendValue(STEERING_WHEEL_ID, new Uint32(1 << 4));
                model.up = false;
            } else if (model.right) {
                gateway.sendValue(STEERING_WHEEL_ID, new Uint32(1 << 5));
                model.right = false;
            } else if (model.down) {
                gateway.sendValue(STEERING_WHEEL_ID, new Uint32(1 << 6));
                model.down = false;
            } else if (model.left) {
                gateway.sendValue(STEERING_WHEEL_ID, new Uint32(1 << 7));
                model.left = false;
            }
        }
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

	@Override
	public int[] getProvidingSingals() {
		return new int[]{STEERING_WHEEL_ID};
	}
}
