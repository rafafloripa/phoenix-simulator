package simulator.serialdevice;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Enumeration;
import simulator.BasicModule;
import simulator.SimulatorGateway;
import android.swedspot.scs.data.Uint16;
import static simulator.SimulationModuleState.RUNNING;
import static simulator.SimulationModuleState.STOPPED;

;

public class SerialDevice extends BasicModule implements
		SerialPortEventListener {
	private BufferedReader input;
	private SerialPort serialPort;
	private CommPortIdentifier portId = null;
	private static final int TIME_OUT = 2000;
	private static final int DATA_RATE = 9600;
	private static final int SIGNAL_ID = 515;

	public SerialDevice(SimulatorGateway gateway) {
		super(gateway);
	}

	@Override
	public void run() {
		if (portId == null) {
			System.out.println("Could not find COM port.");
			return;
		}

		if (state == STOPPED)
			return;
		System.out.println("Reading from port " + portId.getName());

		try {
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);
			serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			input = new BufferedReader(new InputStreamReader(
					serialPort.getInputStream()));

			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);

			while (state == RUNNING) {
				Thread.sleep(1000);
			}

		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	@Override
	public synchronized void startModule() {
		super.startModule();
	}

	@Override
	public synchronized void stopModule() {
		super.stopModule();
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	public void initializeDevice() {
		initializeDevice("/dev/ttyACM");
	}

	public void initializeDevice(String portname) {
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum
					.nextElement();
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
					gateway.sendValue(SIGNAL_ID, new Uint16(data));
				}

			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
	}

	@Override
	public int[] getProvidingSingals() {
		return new int[]{SIGNAL_ID};
	}

}
