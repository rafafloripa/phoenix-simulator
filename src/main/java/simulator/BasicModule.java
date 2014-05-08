package simulator;

public abstract class BasicModule {

    protected Simulator simulator;

    public abstract void startSimulation() throws Exception;

    public abstract void stopSimulation() throws Exception;

    public abstract void pauseSimulation() throws Exception;

    public abstract void resumeSimulation() throws Exception;

    public void setSimulator(Simulator simulator)
    {
        this.simulator = simulator;
    }
    
    @Override
    public abstract boolean equals(Object o);

	@Override
	public abstract int hashCode();
}
