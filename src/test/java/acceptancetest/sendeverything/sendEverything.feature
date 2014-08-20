Feature: subscribing for a signal
  As a developer
  I want the ability to send many signals at once
  So that I can develop more efficiently

  @shutdownNode
  Scenario: subscribing for a signal
    Given The dummy server is setup

    And The server subscribes for everything
    And After 2000 mSec have passed

    And The simulator is setup
    And After 2000 mSec have passed

    And Add a node to simulator on port 8251 and ip localhost
    And After 2000 mSec have passed

    And The simulator provides everything
    And After 2000 mSec have passed

    And The simulator sends 1 for all signals
    And After 2000 mSec have passed

    And The server should have received 1 for all signals