package unit;

import static org.junit.Assert.*;

import org.junit.Test;

import simulator.ReplayerDataRow;

public class ReplayerDataRowTests {
	
	@Test
	public void compareTo_greaterTimestamp_returns1() {
		
		//Arrange
		final ReplayerDataRow replayerDataRowWithGreaterTimestamp = new ReplayerDataRow(0, "", 200);
		final ReplayerDataRow replayerDataRowWithLesserTimestamp = new ReplayerDataRow(0, "", 100);
		
		//Act
		int result = replayerDataRowWithGreaterTimestamp.compareTo(replayerDataRowWithLesserTimestamp);
		
		//Assert
		assertEquals(1, result);
	}
	
	@Test
	public void compareTo_EqualTimestamp_returns0() {
		
		//Arrange
		final ReplayerDataRow replayerDataRowWithEqualTimestamp1 = new ReplayerDataRow(0, "", 200);
		final ReplayerDataRow replayerDataRowWithEqualTimestamp2 = new ReplayerDataRow(0, "", 200);
		
		//Act
		int result = replayerDataRowWithEqualTimestamp1.compareTo(replayerDataRowWithEqualTimestamp2);
		
		//Assert
		assertEquals(0, result);
	}
	
	@Test
	public void compareTo_LesserTimestamp_returnsMinus1() {
		
		//Arrange
		final ReplayerDataRow replayerDataRowWithGreaterTimestamp = new ReplayerDataRow(0, "", 200);
		final ReplayerDataRow replayerDataRowWithLesserTimestamp = new ReplayerDataRow(0, "", 100);
		
		//Act
		int result = replayerDataRowWithLesserTimestamp.compareTo(replayerDataRowWithGreaterTimestamp);
		
		//Assert
		assertEquals(-1, result);
	}
	
}
