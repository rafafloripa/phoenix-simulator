Feature: As an OEM
I want the SDK to have an interface layer
so that I can implement my own components to talk to the SDK

  @shutdownNode
  Scenario: Connect the Simulator SDP Node to the Driver Distraction level 
    Given The simulator is setup
    And The Driver Distraction is setup
    And Add a node to simulator on port 8126 and ip localhost
    And Add a node to simulator on port 9898 and ip localhost
    And After 1000 mSec have passed
    When The simulator connects to the driver distraction
    And After 1000 mSec have passed
    Then The connection should be accepted
