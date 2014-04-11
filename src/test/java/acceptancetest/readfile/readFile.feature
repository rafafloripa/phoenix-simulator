Feature: 
As a developer
I want the simulator to read data files with signal data and time stamps
So that I can get simulated values for my application

  Scenario: reading an data file and sending the data
    Given The simulator is setup and running
    And The DummyApp has subscribed to signal 0x0001
    And The simulator reads ExampleData file to replay
    #The ExampleData file contains the signals from the table below
    When The simulator start replaying
    Then DummyApp should get all changes
    |TimeStamp|SignalID|Value|
    |0		  |0x0001  |14   |
    |1		  |0x0001  |15   |
    |2		  |0x0001  |16   |
    |3		  |0x0001  |11   |
    |4		  |0x0001  |10   |
