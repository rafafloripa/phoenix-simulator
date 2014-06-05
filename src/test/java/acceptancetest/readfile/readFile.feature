Feature: 
  As a developer
  I want the simulator to read data files with signal data and time stamps
  So that I can get simulated values for my application

  @shutdownNode
  Scenario: reading an data file and sending the data
    Given The dummy application is setup and listening on port 8126
    And The simulator is setup
    And Add a node to simulator on port 8126 and ip localhost
    And The DummyApp has subscribed to signal 150
    And The DummyApp has subscribed to signal 151
    And The simulator reads ExampleData file to replay
    
    #The ExampleData file contains the signals from the table below
    
    When The simulator start replaying
    Then The DummyApp should have received all data
      | TimeStamp | SignalID | Value      |
      | 0         | 151      | 2147483647 |
      | 100       | 151      | 15         |
      | 200       | 151      | 16         |
      | 400       | 151      | 11         |
      | 800       | 151      | 10         |
      | 1600      | 150      | 555        |
      | 1601      | 150      | 90         |
