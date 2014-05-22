package simulator.serialdevice;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Enumeration;

import simulator.BasicModule;

import com.swedspot.scs.data.Uint16;


public class SerialDevice extends BasicModule implements Runnable, SerialPortEventListener {
	private BufferedReader input;
	private SerialPort serialPort;
	private CommPortIdentifier portId = null;
	private Thread serialDeviceThread;
	private boolean isRunning = false;
	private static final int TIME_OUT = 2000;
	private static final int DATA_RATE = 9600;
	private static final int SIGNAL_ID = 515;

	public SerialDevice()
	{
	}
	
	@Override
	public void run() {
	    if (portId == null) {
	        System.out.println("Could not find COM port.");
	        return;
	    }
	    
	    if (!isRunning) return;
	    System.out.println("Reading from port " + portId.getName());

	    try {
	        serialPort = (SerialPort) portId.open(this.getClass().getName(),
	                TIME_OUT);
	        serialPort.setSerialPortParams(DATA_RATE,
	                SerialPort.DATABITS_8,
	                SerialPort.STOPBITS_1,
	                SerialPort.PARITY_NONE);

	        input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

	        serialPort.addEventListener(this);
	        serialPort.notifyOnDataAvailable(true);
	        
	        while (isRunning)
	        {
				Thread.sleep(1000);
	        }
	        
	    } catch (Exception e) {
	        System.err.println(e.toString());
	    }
	}

	@Override
	public synchronized void startSimulation() throws Exception {
		isRunning = true;
		serialDeviceThread = new Thread(this);
		simulator.provideSignal(SIGNAL_ID);
		serialDeviceThread.start();
	}

	@Override
	public synchronized void stopSimulation() throws Exception {
		isRunning = false;
	    if (serialPort != null) {
	        serialPort.removeEventListener();
	        serialPort.close();
	    }
	    serialDeviceThread.join();
	    simulator.unprovideSignal(SIGNAL_ID);
	}

	@Override
	public void pauseSimulation() throws Exception {		
	}

	@Override
	public void resumeSimulation() throws Exception {	
	}

	@Override
	public boolean equals(Object o) {
		return false;
	}

	@Override
	public int hashCode() {
		return 100;
	}
	
	public void initializeDevice()
	{
		initializeDevice("/dev/ttyACM");
	}
	public void initializeDevice(String portname)
	{
		
	    Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

	    while (portEnum.hasMoreElements()) {
	        CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
	            if (currPortId.getName().equals(portname)) {
	                portId = currPortId;
	                break;
	            }
	    }
   }

	@Override
	public synchronized void serialEvent(SerialPortEvent ev) {
		  if (ev.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
		        try {
		        	int data;
		            if (input.ready()) {
		                data = Integer.parseInt(input.readLine());
		                System.out.println(data);
		                simulator.sendValue(SIGNAL_ID, new Uint16(data));
		            }

		        } catch (Exception e) {
		            System.err.println(e.toString());
		        }
		    }
	}

}
