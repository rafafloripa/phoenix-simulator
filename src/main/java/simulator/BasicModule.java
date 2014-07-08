package simulator;

public abstract class BasicModule implements Runnable {

    protected SimulatorGateway gateway;
    protected SimulationModuleState state;
    protected Thread moduleThread;

    public BasicModule(SimulatorGateway gateway) {
        this.gateway = gateway;
    }

    public void startModule() {
        provide();
        state = SimulationModuleState.RUNNING;
        moduleThread = new Thread(this);
        moduleThread.start();
    }

    public void stopModule() {
        state = SimulationModuleState.STOPPED;
        unprovide();
        try {
            moduleThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setSimulator(SimulatorGateway simulator) {
        this.gateway = simulator;
    }

    public abstract int[] getProvidingSingals();

    protected void provide() {
        for (int signalID : getProvidingSingals())
            gateway.provideSignal(signalID);
    }

    protected void unprovide() {
        for (int signalID : getProvidingSingals())
            gateway.unprovideSignal(signalID);
    }

    public Thread getModuleThread() {
        return moduleThread;
    }
}
