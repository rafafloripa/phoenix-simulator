package simulator.steeringwheel.gxt27;

import static simulator.SimulationModuleState.RUNNING;

import java.net.URL;

import net.java.games.input.Component;
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
        String path = getClass().getClassLoader().getResource(".").getPath();
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
                gateway.sendValue(STEERING_WHEEL_ID, new Uint32(1 << 0));
                model.up = false;
            } else if (model.right) {
                gateway.sendValue(STEERING_WHEEL_ID, new Uint32(1 << 1));
                model.right = false;
            } else if (model.down) {
                gateway.sendValue(STEERING_WHEEL_ID, new Uint32(1 << 2));
                model.down = false;
            } else if (model.left) {
                gateway.sendValue(STEERING_WHEEL_ID, new Uint32(1 << 3));
                model.left = false;
            }
            com = controller.getComponent(Button._4);
            if (com.getPollData() > 0) {
                model.plus = true;
                // LOGGER.debug("plus");
            } else if (model.plus) {
                gateway.sendValue(STEERING_WHEEL_ID, new Uint32(1 << 4));
                model.plus = false;
            }

            com = controller.getComponent(Button._7);
            if (com.getPollData() > 0) {
                model.minus = true;
                // LOGGER.debug("minus");
            } else if (model.minus) {
                gateway.sendValue(STEERING_WHEEL_ID, new Uint32(1 << 5));
                model.minus = false;
            }

            com = controller.getComponent(Button._8);
            if (com.getPollData() > 0) {
                model.power  = true;
                // LOGGER.debug("power");
            } else if (model.power  ) {
                gateway.sendValue(STEERING_WHEEL_ID, new Uint32(1 << 6));
                model.power  = false;
            }

            com = controller.getComponent(Button._9);
            if (com.getPollData() > 0) {
                model.app = true;
                // LOGGER.debug("app");
            } else if (model.app) {
                gateway.sendValue(STEERING_WHEEL_ID, new Uint32(1 << 7));
                model.app = false;
            }

            com = controller.getComponent(Button._10);
            if (com.getPollData() > 0) {
                model.back = true;
                // LOGGER.debug("back");
            } else if (model.back) {
                gateway.sendValue(STEERING_WHEEL_ID, new Uint32(1 << 8));
                model.back = false;
            }
            
            com = controller.getComponent(Button._11);
            if (com.getPollData() > 0) {
                model.select = true;
                // LOGGER.debug("select");
            } else if (model.select) {
                gateway.sendValue(STEERING_WHEEL_ID, new Uint32(1 << 9));
                model.select = false;
            }
        }
    }

    class ControllerModel {
        private boolean up = false;
        private boolean right = false;
        private boolean down = false;
        private boolean left = false;
        private boolean app = false;
        private boolean power = false;
        private boolean back = false;
        private boolean select = false;
        private boolean minus = false;
        private boolean plus = false;

        public int getFlags() {
            int upInt = up ? 1 : 0;
            int rightInt = right ? 1 : 0;
            int downInt = down ? 1 : 0;
            int leftInt = left ? 1 : 0;
            int appInt = app ? 1 : 0;
            int powerInt = power ? 1 : 0;
            int backInt = back ? 1 : 0;
            int selectInt = select ? 1 : 0;
            int minusInt = minus ? 1 : 0;
            int plusInt = plus ? 1 : 0;
            return (upInt << 0) +
                    (rightInt << 1) +
                    (downInt << 2) +
                    (leftInt << 3) +
                    (plusInt << 4) +
                    (minusInt << 5) +
                    (powerInt << 6) +
                    (appInt << 7) +
                    (backInt << 8) +
                    (selectInt << 9);
        }
    }

    @Override
    public int[] getProvidingSingals() {
        return new int[] { STEERING_WHEEL_ID };
    }
}
