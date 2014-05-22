package simulator.steeringwheel.gxt27;

import com.swedspot.scs.data.Uint32;

import net.java.games.input.Component;
import net.java.games.input.Component.Identifier.Button;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import simulator.BasicModule;

public class GXT27Module extends BasicModule implements Runnable {
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
        if (controller == null) {
            System.out.println("Could not find the Trust GXT 27 steering wheel");
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

    private Controller getController() {
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        for (Controller cont : controllers) {
            if (cont.getName().equals("Steering Wheel"))
                return cont;
        }
        return null;
    }

    private void initDevice() {
        controller = getController();
        model = new ControllerModel();
    }

    private void updateControllerModel(boolean didPoll) {
        if (didPoll) {
            com = controller.getComponent(Button._0);
            if (com.getPollData() > 0) {
                model.up = true;
                System.out.println(Button._0);
            } else if (model.up) {
                model.up = false;
                hasChanged = true;
            }

            com = controller.getComponent(Button._1);
            if (com.getPollData() > 0) {
                model.right = true;
                System.out.println(Button._1);
            } else if (model.right) {
                model.right = false;
                hasChanged = true;
            }

            com = controller.getComponent(Button._2);
            if (com.getPollData() > 0) {
                model.down = true;
                System.out.println(Button._2);
            } else if (model.down) {
                model.down = false;
                hasChanged = true;
            }

            com = controller.getComponent(Button._3);
            if (com.getPollData() > 0) {
                model.left = true;
                System.out.println(Button._3);
            } else if (model.left) {
                model.left = false;
                hasChanged = true;
            }
        }
    }

    @Override
    public void startSimulation() throws Exception {
        isRunning = true;
        initDevice();
        gxt27InputThread = new Thread(this);
        gxt27InputThread.start();
        simulator.provideSignal(STEERING_WHEEL_ID);
    }

    @Override
    public void stopSimulation() throws Exception {
        isRunning = false;
        isPaused = false;
        gxt27InputThread.join();
        simulator.unprovideSignal(STEERING_WHEEL_ID);
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
        private boolean left = false;
        private boolean right = false;
        private boolean up = false;
        private boolean down = false;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof ControllerModel) {
                ControllerModel other = (ControllerModel) o;
                return left == other.left &&
                        right == other.right &&
                        up == other.up &&
                        down == other.down;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int leftInt = 0;
            int rightInt = 0;
            int upInt = 0;
            int downInt = 0;
            if (left) {
                leftInt = 1;
            }
            if (right) {
                rightInt = 1;
            }
            if (up) {
                upInt = 1;
            }
            if (down) {
                downInt = 1;
            }
            return leftInt * 1 + rightInt * 10 + upInt * 100 + downInt * 1000;
        }

        public int getFlags() {
            int leftInt = 0;
            int rightInt = 0;
            int upInt = 0;
            int downInt = 0;
            if (left) {
                leftInt = 1;
            }
            if (right) {
                rightInt = 1;
            }
            if (up) {
                upInt = 1;
            }
            if (down) {
                downInt = 1;
            }
            return (leftInt << 0) + (rightInt << 1) + (upInt << 2) + (downInt << 3);
        }
    }
}
