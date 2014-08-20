package acceptancetest.sendeverything;

import android.swedspot.scs.data.*;
import android.swedspot.sdp.configuration.Configuration;
import com.swedspot.vil.configuration.ConfigurationFactory;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import static acceptancetest.util.Util.staticServer;
import static acceptancetest.util.Util.staticSimulator;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SendEverythingSteps {

    private Set<Integer> idList = ConfigurationFactory.getConfiguration().getConfiguredSignals().keySet();

    public static SCSData createData(DataType type, int data) {
        SCSData createdData = null;

        switch (type) {
        case UINT8:
            createdData = new Uint8(data);
            break;
        case UINT16:
            createdData = new Uint16(data);
            break;
        case UINT32:
            createdData = new Uint32(data);
            break;
        case BLOB:
            createdData = new Blob(new byte[] { (byte) data });
            break;
        case STRING:
            createdData = new SCSString(String.valueOf(data));
            break;
        case IMAGE:
            createdData = new SCSImage(new byte[] { (byte) data });
            break;
        case BOOLEAN:
            createdData = new SCSBoolean(new byte[] { (byte) data });
            break;
        case DOUBLE:
            createdData = new SCSDouble(data);
            break;
        case FLOAT:
            createdData = new SCSFloat(data);
            break;
        case INTEGER:
            createdData = new SCSInteger(data);
            break;
        case LONG:
            createdData = new SCSLong(data);
            break;
        case SHORT:
            createdData = new SCSShort((short) data);
            break;
        case DATE:
            createdData = new SCSDate(new Date(data));
            break;
        default:
            fail();
        }
        return createdData;
    }

    @And("^The server subscribes for everything$")
    public void serverSubscribesForEverything() {

        Integer[] signalIDs = idList.toArray(new Integer[idList.size()]);

        for (Integer i : signalIDs) {
            staticServer.subscribe(new String[] { String.valueOf(i) });
        }
    }

    @Then("^The server should have received (\\d+) for all signals$")
    public void serverGetsEverything(int value) {
        idList.forEach(id -> {
            System.out.println("comparing data for: " + id);
            if (id != 513) {
                Configuration conf = ConfigurationFactory.getConfiguration();
                SCSData data = createData(conf.getSignalInformation(id).getDataType(), value);
                assertTrue("failed when comparing signal " + id + " data with received value.", staticServer.didReceiveValue(id, data));
            }
        });
    }

    @And("^The simulator provides everything$")
    public void simulatorProvidesEverything() {
        idList.forEach(id -> {
            if (id != 513) {
                staticSimulator.provideSignal(id);
                System.out.println("providing " + id);
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {}
        });
    }

    @And("^The simulator sends (\\d+) for all signals$")
    public void simulatorSendsAll(int value) {
        final Configuration conf = ConfigurationFactory.getConfiguration();

        idList.forEach(id -> {
            if (id == 320) {
                SCSData data = createData(conf.getSignalInformation(id).getDataType(), value);
                staticSimulator.sendValue(id, data);
                System.out.println("sent: " + Arrays.toString(data.getData()) + " for id: " + id);
            } else {
                SCSData data = createData(conf.getSignalInformation(id).getDataType(), value);
                staticSimulator.sendValue(id, data);
                System.out.println("sent: " + Arrays.toString(data.getData()) + " for id: " + id);
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {}
        });
    }
}
